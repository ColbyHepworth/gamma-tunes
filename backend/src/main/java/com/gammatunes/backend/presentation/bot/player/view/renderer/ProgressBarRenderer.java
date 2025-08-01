package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Renders a progress bar for the currently playing track in the Discord embed.
 * The progress bar shows the elapsed time, remaining time, and a play-head indicator.
 */
@Component
@Order(99)
public final class ProgressBarRenderer implements FieldRenderer {

    private static final int    BAR_LEN   = 20;
    private static final String UNIT      = "▬";          // thin horizontal bar unit
    private static final String HEAD      = "🔘";          // play‑head
    private static final String URL       = "https://google.com"; // any stable link

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String ignored) {
        player.getCurrentlyPlaying().ifPresent(track -> {
            String bar = build(player.getTrackPosition(), track.duration().toMillis());
            eb.addField("Progress", bar, false);
        });
    }

    private String build(long pos, long total) {
        if (total <= 0) return "🔴 Live Stream";

        int headIdx = (int) (BAR_LEN * pos / total); // 0‑based index where play‑head sits
        if (headIdx >= BAR_LEN) headIdx = BAR_LEN - 1; // safety cap on last tick

        String elapsed   = UNIT.repeat(headIdx);
        String remaining = UNIT.repeat(Math.max(0, BAR_LEN - headIdx - 1));

        // Wrap the elapsed part in a markdown link to turn it blue.
        String linkedElapsed = elapsed.isEmpty() ? "" : "[" + elapsed + "]( " + URL + " )";

        return linkedElapsed +
            HEAD +
            remaining +
            " " +
            format(pos) +
            " / " +
            format(total);
    }

    private static String format(long millis) {
        Duration d = Duration.ofMillis(millis);
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        return h > 0 ? String.format("%02d:%02d:%02d", h, m, s)
            : String.format("%02d:%02d", m, s);
    }
}
