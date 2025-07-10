package com.gammatunes.backend.bot.command;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.util.CommandUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Abstract base class for commands that interact with the audio player.
 * Provides common functionality for executing player commands and sending success messages.
 */
public abstract class PlayerCommand implements Command {

    protected final AudioService audioService;

    public PlayerCommand(AudioService audioService) {
        this.audioService = audioService;
    }

    /**
     * Runs the common checks and executes the player command.
     * This method is called when the command is invoked.
     * @param event The event containing the command interaction details.
     */
    @Override
    public final void execute(SlashCommandInteractionEvent event) {
        if (!CommandUtil.preliminaryChecks(event)) {
            return;
        }
        CommandUtil.connectToChannel(audioService, event);
        AudioPlayer player = CommandUtil.getPlayer(audioService, event);

        executePlayerCommand(player);
        event.getHook().sendMessage(getSuccessMessage()).queue();
    }

    /**
     * Returns the command data for this player command.
     * Subclasses should implement this method to provide specific command details.
     * @param player The audio player instance associated with the command.
     */
    protected abstract void executePlayerCommand(AudioPlayer player);

    /**
     * Returns the success message to be sent after executing the command.
     * Subclasses should implement this method to provide a specific success message.
     * @return The success message as a String.
     */
    protected abstract String getSuccessMessage();
}
