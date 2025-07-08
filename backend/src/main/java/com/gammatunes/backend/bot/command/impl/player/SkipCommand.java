package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.command.PlayerCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

/**
 * Command to skip the currently playing track in the voice channel.
 * This command interacts with the audio service to skip playback.
 */
@Component
public class SkipCommand extends PlayerCommand {

    public SkipCommand(AudioService audioService) {
        super(audioService);
    }

    @Override
    public CommandData getCommandData() { return Commands.slash("skip", "Skips the current track."); }


    @Override
    protected void executePlayerCommand(AudioPlayer player) {
        player.skip();
    }

    @Override
    protected String getSuccessMessage() {
        return "⏭️ Skipped the current track.";
    }
}
