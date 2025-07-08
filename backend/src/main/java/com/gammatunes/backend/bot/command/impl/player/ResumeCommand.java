package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.command.PlayerCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Command to stop the audio player and clear the queue.
 * This command interacts with the backend service to perform the stop operation.
 */
@Component
public class ResumeCommand extends PlayerCommand {

    public ResumeCommand(AudioService audioService) {
        super(audioService);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("resume", "Resumes the paused player.");
    }


    @Override
    protected void executePlayerCommand(AudioPlayer player) {
        player.resume();
    }

    @Override
    protected String getSuccessMessage() {
        return "▶️ Player resumed.";
    }
}
