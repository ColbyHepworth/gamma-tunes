package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.presentation.bot.interaction.command.PlayerCommandHandler;
import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

/**
 * Command to pause the currently playing audio player.
 * This command interacts with the audio service to pause playback.
 */
@Component
public class PauseCommandHandler extends PlayerCommandHandler {


    public PauseCommandHandler(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("pause", "Pauses the current player.");
    }

    @Override
    protected PlayerOutcomeResult handle(Member member, SlashCommandInteractionEvent event) {
        return new PlayerOutcomeResult(discordPlayerController.pause(member), null);
    }
}
