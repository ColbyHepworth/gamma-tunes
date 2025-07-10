package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.command.Command;
import com.gammatunes.backend.bot.util.CommandUtil;
import com.gammatunes.backend.common.model.Track;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Command to skip the currently playing track in the voice channel.
 * This command interacts with the audio service to skip playback.
 */
@Component
public class SkipCommand implements Command {

    private final AudioService audioService;

    public SkipCommand(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!CommandUtil.preliminaryChecks(event)) {
            return;
        }

        AudioPlayer player = CommandUtil.getPlayer(audioService, event);
        Optional<Track> skippedTrack = player.skip();

        if (skippedTrack.isPresent()) {
            event.getHook().sendMessage("⏭️ Skipped to: `" + skippedTrack.get().title() + "`").queue();
        } else {
            event.getHook().sendMessage("❌ Nothing to skip to! The queue is empty.").queue();
        }
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("skip", "Skips the current track.");
    }

}
