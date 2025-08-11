package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Renders the queue information field for the player embed.
 * Shows the number of songs in the queue and the total queued duration.
 */
@Component
@Order(11)
public final class QueueInfoFieldRenderer implements FieldRenderer {

    /**
     * Unique identifier for this field renderer.
     * Used to identify the component in interactions.
     */
    @Override
    public void render(EmbedBuilder embedBuilder, PlayerView playerView, String statusText) {
        List<PlayerView.TrackView> queue = playerView.queue();
        if (queue == null || queue.isEmpty()) {
            return;
        }

        long totalDurationMillis = queue.stream()
            .mapToLong(trackView -> Math.max(0L, trackView.lengthMillis()))
            .sum();

        String formattedDuration = formatDuration(totalDurationMillis);
        String queueInfo = String.format("%d songs in queue • %s", queue.size(), formattedDuration);

        embedBuilder.addField("Queue", queueInfo, false);
    }

    /**
     * Formats the total duration in milliseconds into a human-readable string.
     * If the duration is zero or negative, returns "0:00".
     *
     * @param milliseconds The total duration in milliseconds.
     * @return A formatted string representing the duration.
     */
    private static String formatDuration(long milliseconds) {
        if (milliseconds <= 0) {
            return "0:00";
        }
        Duration duration = Duration.ofMillis(milliseconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return (hours > 0)
            ? String.format("%d:%02d:%02d", hours, minutes, seconds)
            : String.format("%d:%02d", minutes, seconds);
    }
}
