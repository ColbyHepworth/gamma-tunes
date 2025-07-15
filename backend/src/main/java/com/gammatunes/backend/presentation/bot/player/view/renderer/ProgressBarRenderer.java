package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Duration;

public final class ProgressBarRenderer implements FieldRenderer {

    private static final int BAR_LEN = 20;

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String ignored) {
        player.getCurrentlyPlaying().ifPresent(track -> {
            String bar = build(player.getTrackPosition(), track.duration().toMillis());
            eb.addField("Progress", bar, false);
        });
    }

    private String build(long pos, long total) {
        if (total <= 0) return "🔴 Live Stream";

        long p = (BAR_LEN * pos) / total;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < BAR_LEN; i++) sb.append(i == p ? "🔘" : "▬");
        sb.append("] ").append(formatDuration(pos)).append('/').append(formatDuration(total));
        return sb.toString();
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
