package com.gammatunes.backend.presentation.bot.interaction.command;

import com.gammatunes.backend.presentation.bot.control.DiscordAudioController;
import com.gammatunes.backend.presentation.bot.exception.NoQueryFoundException;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public abstract class PlayerQueryCommandHandler extends PlayerCommandHandler implements QueryCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PlayerQueryCommandHandler.class);

    public PlayerQueryCommandHandler(DiscordAudioController discordAudioController) {
        super(discordAudioController);
    }

    /**
     * Gets the command data for this player query command.
     * Subclasses should implement this method to provide specific command details.
     * @return CommandData containing the command information.
     */
    @Override
    public final String getQuery(SlashCommandInteractionEvent event) {
        OptionMapping queryOption = event.getOption("query");
        if (queryOption == null) {
            logger.warn("No query option provided in the command event.");
            throw new NoQueryFoundException();
        }
        return queryOption.getAsString();
    }
}
