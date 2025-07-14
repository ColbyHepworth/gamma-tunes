package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.infrastructure.source.exception.TrackLoadException;
import com.gammatunes.backend.presentation.bot.control.DiscordAudioController;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.interaction.command.PlayerCommandHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;


/**
 * Command to stop the audio player, clear the queue, and disconnect the bot from the voice channel.
 * This command interacts with the audio service to manage playback and connection state.
 */
@Component
public class StopCommandHandler extends PlayerCommandHandler {

    public StopCommandHandler(DiscordAudioController discordAudioController) {
        super(discordAudioController);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("stop", "Stops the player, clears the queue, and disconnects the bot.");
    }


    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) throws TrackLoadException, MemberNotInVoiceChannelException {
        discordAudioController.stop(member);
    }

    @Override
    protected String getSuccessMessage() {
        return "⏹️ Player stopped and disconnected.";
    }
}
