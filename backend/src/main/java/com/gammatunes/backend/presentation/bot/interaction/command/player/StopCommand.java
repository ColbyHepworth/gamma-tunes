package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.domain.exception.TrackLoadException;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.domain.exception.MemberNotInVoiceChannelException;
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
public class StopCommand extends PlayerCommand {

    public StopCommand(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("stop", "Stops the player, clears the queue, and disconnects the bot.");
    }

    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) throws TrackLoadException, MemberNotInVoiceChannelException {
        discordPlayerController.stop(member);
    }
}
