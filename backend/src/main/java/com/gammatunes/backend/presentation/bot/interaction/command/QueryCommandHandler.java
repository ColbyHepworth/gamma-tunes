package com.gammatunes.backend.presentation.bot.interaction.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Represents a command that executes a query.
 * This interface extends the Command interface and provides a method to retrieve the query string.
 */
public interface QueryCommandHandler extends CommandHandler {

    void execute(SlashCommandInteractionEvent event);
    String getQuery(SlashCommandInteractionEvent event);
}
