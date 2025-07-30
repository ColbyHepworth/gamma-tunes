package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
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
public class PreviousCommand extends PlayerCommand {

    public PreviousCommand(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    /**
     * Contains the specific logic for the "previous" command.
     * This is called by the template `execute` method in the parent PlayerCommand.
     * @param member The member who initiated the command.
     * @param event The raw JDA event.
     */
    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) {
        discordPlayerController.previous(member);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("previous", "Moves to the previous track in the queue.");
    }
}
