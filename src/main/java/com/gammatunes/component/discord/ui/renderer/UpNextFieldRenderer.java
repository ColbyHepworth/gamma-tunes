package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Renders the "Up Next" field in the player embed, showing the next tracks in the queue.
 * This field displays a list of upcoming tracks, allowing users to see what will play next.
 */
@Component
@Order(10)
public final class UpNextFieldRenderer implements FieldRenderer {

    private static final int TRACKS_TO_SHOW = 3;

    /**
     * Unique identifier for this field renderer.
     * Used to identify the field in the embed.
     */
    @Override
    public void render(EmbedBuilder embedBuilder, PlayerView playerView, String statusText) {
        if (playerView.queue().isEmpty()) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < Math.min(playerView.queue().size(), TRACKS_TO_SHOW); index++) {
            PlayerView.TrackView track = playerView.queue().get(index);
            builder.append(String.format("`%d.` %s%n", index + 1, cleanTitle(track.title())));
        }

        if (playerView.queue().size() > TRACKS_TO_SHOW) {
            builder.append(String.format("...and %d more", playerView.queue().size() - TRACKS_TO_SHOW));
        }

        embedBuilder.addField("Up Next", builder.toString(), false);
    }

    /**
     * Cleans the track title by removing common suffixes like "(official video)", "(audio)", etc.
     * This helps in displaying a cleaner and more readable title in the embed.
     *
     * @param title The original track title.
     * @return The cleaned track title.
     */
    private String cleanTitle(String title) {
        return title.replaceAll("(?i)\\s*\\(official.*video\\)|\\s*\\(official.*audio\\)|\\s*\\[official.*]|\\s*\\(audio\\)|\\s*\\(4k.*\\)|\\s*\\(hd\\)", "")
            .trim();
    }
}
