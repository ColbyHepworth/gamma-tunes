package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.command.PlayerCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

/**
 * Command to pause the currently playing audio player.
 * This command interacts with the audio service to pause playback.
 */
@Component
public class PauseCommand extends PlayerCommand {

    public PauseCommand(AudioService audioService) {
        super(audioService);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("pause", "Pauses the current player.");
    }

    @Override
    protected void executePlayerCommand(AudioPlayer player) {
        player.pause();
    }

    @Override
    protected String getSuccessMessage() {
        return "⏸️ Player paused.";
    }
}
