package com.gammatunes.component.discord.interaction.command;

import com.gammatunes.component.discord.interaction.InteractionErrorHandler;
import com.gammatunes.component.discord.interaction.InteractionMetrics;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractBotCommand implements BotCommand {

    @Autowired protected InteractionErrorHandler interactionErrorHandler;
    @Autowired protected InteractionMetrics interactionMetrics;

    @Override
    public final Mono<Void> execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (requireGuild() && guild == null) {
            return Mono.fromFuture(
                event.reply("❌ This command can only be used in a server.")
                    .setEphemeral(true)
                    .submit()
            ).then();
        }

        boolean deferredEphemeral = ephemeralByDefault();
        long startNanos = System.nanoTime();

        return Mono.fromFuture(event.deferReply(deferredEphemeral).submit())
            .doOnSubscribe(s -> log.debug("[{}] deferred reply", name()))
            .then(Mono.defer(() -> {
                log.debug("[{}] handleWork start", name());
                return handleWork(event)
                    .doOnSuccess(v -> log.debug("[{}] handleWork completed", name()))
                    .doOnError(e -> log.warn("[{}] handleWork error: {}", name(), e.toString()));
            }))
            .then(Mono.defer(() -> {
                log.debug("[{}] resultAfterSuccess start", name());
                return resultAfterSuccess(event)
                    .doOnSuccess(r -> log.debug("[{}] resultAfterSuccess completed: {}", name(), r));
            }))
            .flatMap(result -> {
                log.debug("[{}] applyResult start: {}", name(), result);
                return applyResult(event, result, deferredEphemeral)
                    .doOnSuccess(v -> log.debug("[{}] applyResult completed", name()));
            })
            .doOnSuccess(ignored -> interactionMetrics.recordCommand(name(), true, System.nanoTime() - startNanos))
            .onErrorResume(throwable -> {
                interactionMetrics.recordCommand(name(), false, System.nanoTime() - startNanos);
                log.warn("[{}] execute onErrorResume: {}", name(), throwable.toString());
                return interactionErrorHandler.handleCommandError(event, name(), throwable)
                    .onErrorResume(handlerFailure -> Mono.empty());
            })
            .then();
    }

    protected abstract Mono<Void> handleWork(SlashCommandInteractionEvent event);

    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.none());
    }

    protected boolean ephemeralByDefault() { return true; }
    protected boolean requireGuild() { return true; }

    private Mono<Void> applyResult(SlashCommandInteractionEvent event, CommandResult result, boolean deferredEphemeral) {
        if (result instanceof CommandResult.Toast(String message, boolean wantEphemeral)) {
            if (message == null || message.isBlank()) {
                return Mono.fromFuture(event.getHook().editOriginal("\u200B").submit()).then();
            }

            if (deferredEphemeral == wantEphemeral) {
                return Mono.fromFuture(event.getHook().editOriginal(message).submit())
                    .onErrorResume(err ->
                        Mono.fromFuture(event.getHook().editOriginal("\u200B").submit()).then(Mono.empty())
                    ).then();
            } else {
                Mono<?> followup = Mono.fromFuture(event.getHook().sendMessage(message).setEphemeral(wantEphemeral).submit());
                Mono<?> clear    = Mono.fromFuture(event.getHook().editOriginal("\u200B").submit());
                return Mono.when(followup, clear).then();
            }
        }

        // None → just clear spinner
        return Mono.fromFuture(event.getHook().editOriginal("\u200B").submit()).then();
    }

    public sealed interface CommandResult permits CommandResult.Toast {
        static CommandResult none() { return new Toast(null, true); }
        static CommandResult toast(String message, boolean ephemeral) { return new Toast(message, ephemeral); }
        record Toast(String message, boolean ephemeral) implements CommandResult {}
    }
}
