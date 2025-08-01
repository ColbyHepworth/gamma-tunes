package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.model.QueueItem;
import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Renders the queue information field for the audio player embed.
 * This includes the number of songs in the queue and the total duration.
 */
@Component
@Order(11)
public class QueueInfoFieldRenderer implements FieldRenderer {

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String status) {
        List<QueueItem> queue = player.getQueue();
        if (queue.isEmpty()) {
            return;
        }

        long totalDurationMillis = queue.stream()
            .mapToLong(item -> item.track().duration().toMillis())
            .sum();

        String formattedDuration = format(totalDurationMillis);
        String queueInfo = String.format("%d songs in queue • %s", queue.size(), formattedDuration);

        eb.addField("Queue", queueInfo, false);
    }

    private static String format(long millis) {
        if (millis <= 0) return "0:00";
        Duration d = Duration.ofMillis(millis);
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        return h > 0 ? String.format("%d:%02d:%02d", h, m, s)
            : String.format("%d:%02d", m, s);
    }
}
