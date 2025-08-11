package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Renders a dropdown menu for jumping to tracks in the player's queue or history.
 * This component allows users to quickly navigate to specific tracks in the playback history or queue.
 */
@Component
@Order(90)
public final class QueueDropdownRenderer implements ComponentRenderer {

    private static final String COMPONENT_ID = "player:queue-jump";
    private static final int MAX_HISTORY_OPTIONS = 10;
    private static final int MAX_QUEUE_OPTIONS   = 14;

    /**
     * Unique identifier for this component renderer.
     * Used to identify the component in interactions.
     */
    @Override
    public List<ActionRow> render(PlayerView playerView) {
        List<PlayerView.TrackView> historyCopy = new ArrayList<>(playerView.history());
        boolean hasContent = !historyCopy.isEmpty()
            || playerView.currentTrack().isPresent()
            || !playerView.queue().isEmpty();

        if (!hasContent) {
            return List.of(ActionRow.of(
                StringSelectMenu.create(COMPONENT_ID)
                    .setPlaceholder("No tracks in queue or history!")
                    .setDisabled(true)
                    .addOption("empty", "noop")
                    .build()
            ));
        }

        StringSelectMenu.Builder menu = StringSelectMenu.create(COMPONENT_ID)
            .setPlaceholder("Jump to a track...")
            .setMinValues(1)
            .setMaxValues(1);

        List<SelectOption> options = new ArrayList<>(25);

        // History (most recent first)
        Collections.reverse(historyCopy);
        for (int i = 0; i < Math.min(historyCopy.size(), MAX_HISTORY_OPTIONS); i++) {
            var t = historyCopy.get(i);
            String label = truncate(cleanTitle(t.title()), 95);
            String value = "h:" + i + ":" + safeValue(t.identifier());
            options.add(SelectOption.of(label, value).withDescription("⏪ Recently Played"));
        }

        playerView.currentTrack().ifPresent(t -> {
            String label = truncate(cleanTitle(t.title()), 95) + "  ↕️";
            String value = "c:" + safeValue(t.identifier());
            options.add(SelectOption.of(label, value).withDescription("▶️ Now Playing").withDefault(true));
        });

        for (int i = 0; i < Math.min(playerView.queue().size(), MAX_QUEUE_OPTIONS); i++) {
            var t = playerView.queue().get(i);
            String numbered = String.format("%d. %s", i + 1, cleanTitle(t.title()));
            String label = truncate(numbered, 100);
            String value = "q:" + i + ":" + safeValue(t.identifier());
            options.add(SelectOption.of(label, value).withDescription("⏩ Up Next"));
        }

        ensureUniqueValues(options);

        return List.of(ActionRow.of(menu.addOptions(options).build()));
    }

    /**
     * Ensures that all select options have unique values.
     * Throws an exception if any duplicate values are found.
     *
     * @param opts The list of select options to check for uniqueness.
     */
    private static void ensureUniqueValues(List<SelectOption> opts) {
        Set<String> seen = new HashSet<>();
        String dup = opts.stream()
            .map(SelectOption::getValue)
            .filter(v -> !seen.add(v))
            .findFirst()
            .orElse(null);
        if (dup != null) {
            throw new IllegalStateException("Duplicate select value produced: " + dup);
        }
    }

    /**
     * Truncates the given text to a maximum length, appending an ellipsis if truncated.
     * If the text is null, returns an empty string.
     *
     * @param text The text to truncate.
     * @param maxLength The maximum length of the text.
     * @return The truncated text, or an empty string if the input was null.
     */
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    /**
     * Cleans the title by removing common suffixes like "(official video)", "(audio)", etc.
     * This helps in displaying cleaner track titles in the dropdown.
     *
     * @param title The original title of the track.
     * @return The cleaned title, or an empty string if the input was null.
     */
    private static String cleanTitle(String title) {
        if (title == null) return "";
        return title.replaceAll("(?i)\\s*\\(official.*video\\)|\\s*\\(official.*audio\\)|\\s*\\[official.*]|\\s*\\(audio\\)|\\s*\\(4k.*\\)|\\s*\\(hd\\)", "")
            .trim();
    }


    /**
     * Safely formats the track identifier for use in select options.
     * If the identifier is null, returns "null". Otherwise, trims it and limits its length to 80 characters.
     *
     * @param id The track identifier to format.
     * @return A safe string representation of the identifier.
     */
    private static String safeValue(String id) {
        if (id == null) return "null";
        String v = id.trim();
        return v.length() <= 80 ? v : v.substring(0, 80);
    }
}
