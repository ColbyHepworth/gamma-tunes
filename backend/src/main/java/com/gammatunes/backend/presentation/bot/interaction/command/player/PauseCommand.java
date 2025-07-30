package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
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
public class PauseCommand extends PlayerCommand {


    public PauseCommand(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) {
       discordPlayerController.pause(member);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("pause", "Pauses the current player.");
    }

}
