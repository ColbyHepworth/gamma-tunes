package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.infrastructure.source.exception.TrackLoadException;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.presentation.bot.interaction.command.PlayerQueryCommandHandler;
import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;


/**
 * Command to play a song or add it to the queue in the voice channel.
 * This command interacts with the audio service to handle track loading and playback.
 */
@Component
public class PlayCommandHandler extends PlayerQueryCommandHandler {

    public PlayCommandHandler(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("play", "Plays a song or adds it to the queue.")
            .addOption(OptionType.STRING, "query", "The song URL or search term.", true);
    }

    @Override
    protected PlayerOutcomeResult handle(Member member, SlashCommandInteractionEvent event) throws TrackLoadException {
        String query = getQuery(event);
        return new PlayerOutcomeResult(discordPlayerController.play(member, query), query);
    }
}

