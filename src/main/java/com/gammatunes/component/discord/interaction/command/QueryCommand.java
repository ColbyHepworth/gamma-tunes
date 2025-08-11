package com.gammatunes.component.discord.interaction.command;

import com.gammatunes.exception.player.NoQueryFoundException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * An interface for commands that accept a 'query' argument.
 * Provides a default method to easily retrieve the query.
 */
public interface QueryCommand extends BotCommand {

    /**
     * A default helper method for commands that need a "query" option.
     *
     * @param event The command event.
     * @return The required query string.
     * @throws NoQueryFoundException if the 'query' option is missing.
     */
    default String getQuery(SlashCommandInteractionEvent event) {
        OptionMapping queryOption = event.getOption("query");
        if (queryOption == null) {
            throw new NoQueryFoundException();
        }
        return queryOption.getAsString();
    }
}
