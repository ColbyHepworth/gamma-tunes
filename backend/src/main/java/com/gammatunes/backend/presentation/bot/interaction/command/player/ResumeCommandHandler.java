package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.presentation.bot.control.DiscordAudioController;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.interaction.command.PlayerCommandHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

/**
 * Command to stop the audio player and clear the queue.
 * This command interacts with the backend service to perform the stop operation.
 */
@Component
public class ResumeCommandHandler extends PlayerCommandHandler {


    public ResumeCommandHandler(DiscordAudioController discordAudioController) {
        super(discordAudioController);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("resume", "Resumes the paused player.");
    }


    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) throws MemberNotInVoiceChannelException {
        discordAudioController.resume(member);
    }

    @Override
    protected String getSuccessMessage() {
        return "▶️ Player resumed.";
    }
}
