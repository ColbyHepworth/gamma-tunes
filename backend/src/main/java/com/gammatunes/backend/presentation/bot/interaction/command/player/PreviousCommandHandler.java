package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.interaction.command.PlayerCommandHandler;
import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;
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

    public PreviousCommandHandler(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }


    @Override
    protected PlayerOutcomeResult handle(Member member, SlashCommandInteractionEvent event) throws MemberNotInVoiceChannelException {

        return new PlayerOutcomeResult(discordPlayerController.previous(member), null);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("previous", "Moves to the previous track in the queue.");
    }
}
