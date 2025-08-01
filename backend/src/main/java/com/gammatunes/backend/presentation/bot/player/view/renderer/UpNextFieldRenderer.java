package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.model.QueueItem;
import com.gammatunes.backend.domain.model.Track;
import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Renders the "Up Next" field in the player embed, showing the next few tracks in the queue.
 * This renderer is ordered to appear after other fields like "Now Playing" and "Queue".
 */
@Component
@Order(10)
public class UpNextFieldRenderer implements FieldRenderer {

    private static final int TRACKS_TO_SHOW = 3;

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String status) {
        List<Track> queue = player.getQueue().stream()
            .map(QueueItem::track)
            .toList();

        if (queue.isEmpty()) {
            return;
        }

        StringBuilder upNext = new StringBuilder();
        for (int i = 0; i < Math.min(queue.size(), TRACKS_TO_SHOW); i++) {
            Track track = queue.get(i);
            upNext.append(String.format("`%d.` %s\n", i + 1, cleanTitle(track.title())));
        }

        if (queue.size() > TRACKS_TO_SHOW) {
            upNext.append(String.format("...and %d more", queue.size() - TRACKS_TO_SHOW));
        }

        eb.addField("Up Next", upNext.toString(), false);
    }

    private String cleanTitle(String title) {
        return title.replaceAll("(?i)\\s*\\(official.*video\\)|\\s*\\(official.*audio\\)|\\s*\\[official.*]|\\s*\\(audio\\)|\\s*\\(4k.*\\)|\\s*\\(hd\\)", "").trim();
    }
}
