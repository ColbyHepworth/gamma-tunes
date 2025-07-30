package com.gammatunes.backend.presentation.bot.interaction.command.player;


import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
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
public class SkipCommand extends PlayerCommand {

    protected SkipCommand(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) throws MemberNotInVoiceChannelException {
        discordPlayerController.skip(member);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("skip", "Skips the current track.");
    }
}
