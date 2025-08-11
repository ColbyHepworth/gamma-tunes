package com.gammatunes.component.discord.interaction;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Handles errors that occur during Discord interactions such as slash commands, buttons, and select menus.
 * Provides methods to send error messages back to the user in a user-friendly format.
 */
@Component
public class InteractionErrorHandler {

    /**
     * Handles errors that occur during slash command interactions.
     *
     * @param event The event representing the slash command interaction.
     * @param commandName The name of the command that caused the error.
     * @param throwable The exception that was thrown during command execution.
     * @return A Mono that completes when the error message is sent.
     */
    public Mono<Void> handleCommandError(SlashCommandInteractionEvent event, String commandName, Throwable throwable) {
        return Mono.fromFuture(
            event.getHook()
                .editOriginal("⚠️ " + pretty(throwable))
                .submit()
        ).then();
    }

    /**
     * Handles errors that occur during button interactions.
     *
     * @param event The event representing the button interaction.
     * @param buttonId The ID of the button that caused the error.
     * @param throwable The exception that was thrown during button execution.
     * @return A Mono that completes when the error message is sent.
     */
    public Mono<Void> handleButtonError(ButtonInteractionEvent event, String buttonId, Throwable throwable) {
        return Mono.fromFuture(
            event.getHook()
                .sendMessage("⚠️ " + pretty(throwable))
                .setEphemeral(true)
                .submit()
        ).then();
    }

    /**
     * Handles errors that occur during select menu interactions.
     *
     * @param event The event representing the select menu interaction.
     * @param selectId The ID of the select menu that caused the error.
     * @param throwable The exception that was thrown during select execution.
     * @return A Mono that completes when the error message is sent.
     */
    public Mono<Void> handleSelectError(StringSelectInteractionEvent event, String selectId, Throwable throwable) {
        return Mono.fromFuture(
            event.getHook()
                .sendMessage("⚠️ " + pretty(throwable))
                .setEphemeral(true)
                .submit()
        ).then();
    }

    /**
     * Formats the throwable message for user-friendly display.
     * If the message is null or blank, it returns the class name of the throwable.
     *
     * @param throwable The throwable to format.
     * @return A string representation of the throwable for display.
     */
    private static String pretty(Throwable throwable) {
        String message = throwable.getMessage();
        return (message == null || message.isBlank()) ? throwable.getClass().getSimpleName() : message;
    }
}
