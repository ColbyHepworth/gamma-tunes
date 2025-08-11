package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Renders a progress bar for the currently playing track in the Discord player.
 * The progress bar visually represents the playback position and duration of the track.
 */
@Component
@Order(20)
public final class ProgressBarRenderer implements FieldRenderer {

    private static final int BAR_LENGTH = 15;
    private static final String BAR_UNIT = "▬";
    private static final String BAR_HEAD = "🔘";

    @Override
    public void render(EmbedBuilder embedBuilder, PlayerView playerView, String ignored) {
        playerView.currentTrack().ifPresent(trackView -> {
            long durationMillis = trackView.lengthMillis();
            long positionMillis = Math.max(0, playerView.positionMillis());

            String bar = buildBar(positionMillis, durationMillis);
            String time = format(positionMillis) + " / " + format(durationMillis);

            embedBuilder.addField("\u200B", bar + "\n" + time, false);
        });
    }

    /**
     * Builds a progress bar string based on the current playback position and total duration.
     *
     * @param positionMillis The current playback position in milliseconds.
     * @param totalMillis    The total duration of the track in milliseconds.
     * @return A string representing the progress bar.
     */
    private String buildBar(long positionMillis, long totalMillis) {
        if (totalMillis <= 0) {
            return "🔴 Live Stream";
        }
        int headIndex = (int) Math.min(
            BAR_LENGTH - 1,
            Math.max(0, BAR_LENGTH * positionMillis / totalMillis)
        );
        String elapsed = BAR_UNIT.repeat(headIndex);
        String remaining = BAR_UNIT.repeat(Math.max(0, BAR_LENGTH - headIndex - 1));
        return elapsed + BAR_HEAD + remaining;
    }

    /**
     * Formats a duration in milliseconds into a human-readable string.
     * The format is HH:MM:SS for durations over an hour, or MM:SS for shorter durations.
     *
     * @param millis The duration in milliseconds.
     * @return A formatted string representing the duration.
     */
    private static String format(long millis) {
        Duration duration = Duration.ofMillis(Math.max(0, millis));
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return hours > 0
            ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
            : String.format("%02d:%02d", minutes, seconds);
    }
}
