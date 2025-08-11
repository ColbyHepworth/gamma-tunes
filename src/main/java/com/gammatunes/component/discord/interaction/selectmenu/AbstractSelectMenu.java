package com.gammatunes.component.discord.interaction.selectmenu;

import com.gammatunes.component.discord.interaction.InteractionErrorHandler;
import com.gammatunes.component.discord.interaction.InteractionMetrics;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public abstract class AbstractSelectMenu implements SelectMenu {

    @Autowired protected InteractionErrorHandler interactionErrorHandler;
    @Autowired protected InteractionMetrics interactionMetrics;

    @Override
    public final void handle(StringSelectInteractionEvent event, Member member) {
        if (member == null) {
            event.reply("❌ Guild only.").setEphemeral(true).queue();
            return;
        }

        // Silent ack like buttons; if source message vanished, fall back to ephemeral defer.
        event.deferEdit().queue(
            s -> {},
            err -> event.deferReply(true).queue(hook -> hook.deleteOriginal().queue(), t -> {})
        );

        List<String> selectedValues = event.getValues();
        long startNanos = System.nanoTime();

        handleWork(event, member, selectedValues)
            .then(resultAfterSuccess(event, member, selectedValues))
            .flatMap(result -> applyResult(event, result))
            .doOnSuccess(ignored ->
                interactionMetrics.recordSelect(id(), true, System.nanoTime() - startNanos)
            )
            .onErrorResume(throwable -> {
                interactionMetrics.recordSelect(id(), false, System.nanoTime() - startNanos);
                return interactionErrorHandler
                    .handleSelectError(event, id(), throwable)
                    .onErrorResume(handlerFailure -> Mono.empty());
            })
            .subscribe();
    }

    /** Perform the actual work (must be non-blocking). */
    protected abstract Mono<Void> handleWork(StringSelectInteractionEvent event, Member member, List<String> selectedValues);

    /** Optional post-success toast/followup. */
    protected Mono<SelectResult> resultAfterSuccess(StringSelectInteractionEvent event, Member member, List<String> selectedValues) {
        return Mono.just(SelectResult.none());
    }

    private Mono<Void> applyResult(StringSelectInteractionEvent event, SelectResult result) {
        if (result instanceof SelectResult.Toast(String message, boolean ephemeral)) {
            if (message == null || message.isBlank()) {
                return Mono.empty();
            }
            return Mono.fromFuture(event.getHook().sendMessage(message).setEphemeral(ephemeral).submit()).then();
        }
        return Mono.empty();
    }

    public sealed interface SelectResult permits SelectResult.Toast {
        static SelectResult none() { return new Toast(null, true); }
        static SelectResult toast(String message, boolean ephemeral) { return new Toast(message, ephemeral); }
        record Toast(String message, boolean ephemeral) implements SelectResult {}
    }
}
