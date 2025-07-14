package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordAudioController;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.interaction.command.PlayerCommandHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

/**
 * Command to skip the currently playing track in the voice channel.
 * This command interacts with the audio service to skip playback.
 */
@Component
public class SkipCommandHandler extends PlayerCommandHandler {


    public SkipCommandHandler(DiscordAudioController discordAudioController) {
        super(discordAudioController);
    }

    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) throws MemberNotInVoiceChannelException {
        /*
         TODO: Implement skip message handling logic
                if (skippedTrack.isPresent()) {
                    event.getHook().sendMessage("⏭️ Skipped to: `" + skippedTrack.get().title() + "`").queue();
                } else {
                    event.getHook().sendMessage("❌ Nothing to skip to! The queue is empty.").queue();
                }
        */
        discordAudioController.skip(member);
    }

    @Override
    protected String getSuccessMessage() {
        return "⏭️ Skipped to the next track.";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("skip", "Skips the current track.");
    }

}
