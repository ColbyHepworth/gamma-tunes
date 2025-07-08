package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.command.PlayerCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;


/* * Command to stop the audio player and clear the queue.
 * This command interacts with the audio service to perform the stop operation.
 */
@Component
public class StopCommand extends PlayerCommand {

    public StopCommand(AudioService audioService) {
        super(audioService);
    }

    @Override
    protected void executePlayerCommand(AudioPlayer player) {
        player.stop();
    }

    @Override
    protected String getSuccessMessage() {
        return "⏹️ Player stopped and queue cleared.";
    }

    @Override
    public CommandData getCommandData() { return Commands.slash("stop", "Stops the player and clears the queue."); }


}
