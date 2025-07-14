package com.gammatunes.backend.presentation.bot.player.view.factory;

import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.model.Track;
import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A utility class for building the rich MessageEmbed for the player.
 */
public class PlayerEmbedFactory {

    /**
     * Builds the MessageEmbed based on the current state of the player.
     * @param player The AudioPlayer instance.
     * @return The constructed MessageEmbed.
     */
    public static MessageEmbed build(AudioPlayer player) {
        EmbedBuilder eb = new EmbedBuilder();
        Optional<Track> currentTrackOpt = player.getCurrentlyPlaying();

        if (currentTrackOpt.isEmpty()) {
            eb.setTitle("No track playing");
            eb.setDescription("Use `/play` to add a song to the queue!");
            eb.setColor(0x3498db); // Blue
            return eb.build();
        }

        Track currentTrack = currentTrackOpt.get();
        eb.setTitle("Now Playing: " + currentTrack.title(), currentTrack.sourceUrl());
        eb.setDescription("by " + currentTrack.author());
        eb.setColor(0x2ecc71); // Green

        if (currentTrack.thumbnailUrl() != null) {
            eb.setThumbnail(currentTrack.thumbnailUrl());
        }

        // Add progress bar
        eb.addField("Progress", buildProgressBar(player), false);

        return eb.build();
    }


    public static List<Button> buildButtons(AudioPlayer player) {
        PlayerState state = player.getState();
        List<Button> buttons = new ArrayList<>();

        buttons.add(Button.secondary("player:previous", "⏮️"));

        if (state == PlayerState.PLAYING) {
            buttons.add(Button.primary("player:pause", "⏸️"));
        } else {
            buttons.add(Button.success("player:resume", "▶️"));
        }

        buttons.add(Button.secondary("player:skip", "⏭️"));
        buttons.add(Button.danger("player:stop", "⏹️"));

        return buttons;
    }

    /**
     * Creates a text-based progress bar for the currently playing track.
     */
    private static String buildProgressBar(AudioPlayer player) {
        Optional<Track> currentTrackOpt = player.getCurrentlyPlaying();
        if (currentTrackOpt.isEmpty()) {
            return "`[––––––––––––––––––––]`";
        }

        long currentPosition = player.getTrackPosition();
        long totalDuration = currentTrackOpt.get().duration().toMillis();

        if (totalDuration <= 0) {
            return "🔴 Live Stream";
        }

        int barLength = 20;
        long progress = (barLength * currentPosition) / totalDuration;

        StringBuilder bar = new StringBuilder();
        bar.append("`[");
        for (int i = 0; i < barLength; i++) {
            if (i == progress) {
                bar.append("🔘");
            } else {
                bar.append("▬");
            }
        }
        bar.append("]` `").append(formatDuration(currentPosition)).append("/").append(formatDuration(totalDuration)).append("`");

        return bar.toString();
    }

    private static String formatDuration(long millis) {
        Duration duration = Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
