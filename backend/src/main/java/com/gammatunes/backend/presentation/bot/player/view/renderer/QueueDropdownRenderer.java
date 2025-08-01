package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.model.QueueItem;
import com.gammatunes.backend.domain.model.Track;
import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Renders a dropdown menu for jumping to tracks in the player's queue and history.
 * This component allows users to quickly navigate to recently played tracks, currently playing track,
 * and upcoming tracks in the queue.
 */
@Order(90)
@Component
public final class QueueDropdownRenderer implements ComponentRenderer {

    private static final String ID = "player:queue-jump";
    private static final int MAX_HISTORY_OPTIONS = 10;
    private static final int MAX_QUEUE_OPTIONS = 14;

    @Override
    public List<ActionRow> render(AudioPlayer player) {
        List<QueueItem> history = new ArrayList<>(player.getHistory());
        Optional<QueueItem> currentItem = player.getCurrentItem();
        List<QueueItem> queue = player.getQueue();

        boolean hasContent = !history.isEmpty() || currentItem.isPresent() || !queue.isEmpty();

        if (!hasContent) {
            return List.of(ActionRow.of(
                StringSelectMenu.create(ID)
                    .setPlaceholder("No tracks in queue or history!")
                    .setDisabled(true)
                    .addOption("empty", "empty")
                    .build()
            ));
        }

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(ID)
            .setPlaceholder("Jump to a track...");

        Collections.reverse(history);
        for (int i = 0; i < Math.min(history.size(), MAX_HISTORY_OPTIONS); i++) {
            Track track = history.get(i).track();
            menuBuilder.addOption(truncate(cleanTitle(track.title()), 95), track.identifier(), "⏪ Recently Played");
        }

        currentItem.ifPresent(item -> {
            Track track = item.track();
            String labelWithHint = truncate(cleanTitle(track.title()), 95) + "  ↕️";
            menuBuilder.addOption(labelWithHint, track.identifier(), "▶️ Now Playing");
            menuBuilder.setDefaultValues(track.identifier());
        });

        for (int i = 0; i < Math.min(queue.size(), MAX_QUEUE_OPTIONS); i++) {
            Track track = queue.get(i).track();
            String numberedTitle = String.format("%d. %s", i + 1, cleanTitle(track.title()));
            menuBuilder.addOption(truncate(numberedTitle, 100), track.identifier(), "⏩ Up Next");
        }

        if (menuBuilder.getOptions().isEmpty()) {
            return List.of(ActionRow.of(
                StringSelectMenu.create(ID)
                    .setPlaceholder("No tracks in queue or history!")
                    .setDisabled(true)
                    .addOption("empty", "empty")
                    .build()
            ));
        }

        return List.of(ActionRow.of(menuBuilder.build()));
    }

    private String truncate(String text, int maxLength) {
        return text.substring(0, Math.min(text.length(), maxLength));
    }

    private String cleanTitle(String title) {
        return title.replaceAll("(?i)\\s*\\(official.*video\\)|\\s*\\(official.*audio\\)|\\s*\\[official.*]|\\s*\\(audio\\)|\\s*\\(4k.*\\)|\\s*\\(hd\\)", "").trim();
    }
}
