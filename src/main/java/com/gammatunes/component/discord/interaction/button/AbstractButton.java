package com.gammatunes.component.discord.interaction.button;

import com.gammatunes.component.discord.interaction.InteractionErrorHandler;
import com.gammatunes.component.discord.interaction.InteractionMetrics;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

/**
 * Abstract base class for handling Discord button interactions.
 * <p>
 * This class provides a template for handling button interactions in a non-blocking manner,
 * including error handling and metrics recording.
 */
@Slf4j
public abstract class AbstractButton implements Button {

    @Autowired protected InteractionErrorHandler interactionErrorHandler;
    @Autowired protected InteractionMetrics interactionMetrics;

    @Override
    public final void handle(ButtonInteractionEvent event, Member member) {
        if (member == null) {
            event.reply("❌ Guild only.").setEphemeral(true).queue();
            return;
        }
        event.deferEdit().queue(
            s -> {},
            err -> event.deferReply(true).queue(hook -> hook.deleteOriginal().queue(), t -> {})
        );

        long started = System.nanoTime();
        handleWork(event, member)
            .then(resultAfterSuccess(event, member))
            .flatMap(result -> applyResult(event, result))
            .doOnSuccess(ignored ->
                interactionMetrics.recordButton(id(), true, System.nanoTime() - started)
            )
            .onErrorResume(throwable ->
                interactionErrorHandler.handleButtonError(event, id(), throwable)
                    .onErrorResume(ignored -> Mono.empty())
            )
            .subscribe();
    }

    /**
     * The main work to be done when the button is pressed.
     * <p>
     * This method should contain the core logic for handling the button interaction.
     *
     * @param event  the button interaction event
     * @param member the member who pressed the button
     * @return a Mono that completes when the work is done
     */
    protected abstract Mono<Void> handleWork(ButtonInteractionEvent event, Member member);

    /**
     * Called after the main work is done successfully.
     * <p>
     * This method can be overridden to perform additional actions after the button interaction is handled.
     *
     * @param event  the button interaction event
     * @param member the member who pressed the button
     * @return a Mono that emits the result of the interaction
     */
    protected Mono<ButtonResult> resultAfterSuccess(ButtonInteractionEvent event, Member member) {
        return Mono.just(ButtonResult.none());
    }

    /**
     * Applies the result of the button interaction.
     * <p>
     * This method can be overridden to customize how the result is applied, such as sending a message or updating the UI.
     *
     * @param event  the button interaction event
     * @param result the result of the interaction
     * @return a Mono that completes when the result is applied
     */
    private Mono<Void> applyResult(ButtonInteractionEvent event, ButtonResult result) {
        if (result instanceof ButtonResult.Toast(String message, boolean ephemeral)) {
            if (message == null || message.isBlank()) {
                return Mono.empty();
            }
            return Mono.fromFuture(event.getHook().sendMessage(message).setEphemeral(ephemeral).submit()).then();
        }
        return Mono.empty();
    }

    /**
     * Represents the result of a button interaction.
     * <p>
     * This interface allows for different types of results, such as sending a toast message or doing nothing.
     */
    public sealed interface ButtonResult permits ButtonResult.None, ButtonResult.Toast {
        static ButtonResult none() { return new None(); }
        static ButtonResult toast(String message, boolean ephemeral) { return new Toast(message, ephemeral); }
        record None() implements ButtonResult {}
        record Toast(String message, boolean ephemeral) implements ButtonResult {}
    }
}
