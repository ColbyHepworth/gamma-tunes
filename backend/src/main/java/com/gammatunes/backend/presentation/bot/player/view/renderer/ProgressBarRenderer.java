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
@Order(20)
public final class ProgressBarRenderer implements FieldRenderer {

    private static final int BAR_LEN = 20;
    private static final String UNIT = "▬";
    private static final String HEAD = "🔘";
    private static final String URL = "https://google.com";

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String ignored) {
        player.getCurrentItem().ifPresent(item -> {
            var track = item.track();

            String bar = buildBar(player.getTrackPosition(), track.duration().toMillis());
            String time = format(player.getTrackPosition()) + " / " + format(track.duration().toMillis());

            eb.addField("\u200B", bar + "\n" + time, false);

        });
    }

    private String buildBar(long pos, long total) {
        if (total <= 0) return "🔴 Live Stream";
        int headIdx = (int) (BAR_LEN * pos / total);
        if (headIdx >= BAR_LEN) headIdx = BAR_LEN - 1;
        String elapsed = UNIT.repeat(headIdx);
        String remaining = UNIT.repeat(Math.max(0, BAR_LEN - headIdx - 1));
        String linkedElapsed = elapsed.isEmpty() ? "" : "[" + elapsed + "](" + URL + ")";
        return linkedElapsed + HEAD + remaining;
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
