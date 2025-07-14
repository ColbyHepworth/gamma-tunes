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
 * Command to skip to the previous track in the audio player queue.
 * This command interacts with the DiscordAudioController to perform the action.
 */
@Component
public class PreviousCommandHandler extends PlayerCommandHandler {

    public PreviousCommandHandler(DiscordAudioController discordAudioController) {
        super(discordAudioController);
    }


    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) throws MemberNotInVoiceChannelException {
        /*
         TODO: Implement the message handling logic
                event.getHook().sendMessage("⏮️ Skipped to previous track: " + skippedTrack.get().title()).queue();
                event.getHook().sendMessage("❌ No previous track available.").queue();
        */
        discordAudioController.previous(member);
    }

    @Override
    protected String getSuccessMessage() {
        return "⏮️ Skipped to previous track.";
    }


    @Override
    public CommandData getCommandData() {
        return Commands.slash("previous", "Moves to the previous track in the queue.");
    }
}
