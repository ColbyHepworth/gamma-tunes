package com.gammatunes.backend.domain.player;

import com.gammatunes.backend.domain.model.QueueItem;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the music queue, including upcoming tracks and playback history.
 * This class encapsulates the logic for navigating forwards and backwards through the track list.
 */
public class TrackScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

    private final List<QueueItem> items = new ArrayList<>();
    private int currentIndex = -1;

    @Getter
    private boolean repeatEnabled = false;

    /**
     * Adds a new item to the end of the queue.
     * @param item The item to add.
     */
    public void enqueue(QueueItem item) {
        logger.debug("Enqueuing track: {}", item.track());
        items.add(item);
    }

    public void addNext(QueueItem item) {
        logger.debug("Adding track to the front of the queue: {}", item.track());
        items.add(currentIndex + 1, item);
    }

    /**
     * Moves to and returns the next item in the list.
     * @return An Optional containing the next queue item, or empty if at the end of the list.
     */
    public Optional<QueueItem> next() {
        if (currentIndex + 1 < items.size()) {
            logger.debug("Next track: {}", items.get(currentIndex + 1).track());
            currentIndex++;
            return Optional.of(items.get(currentIndex));
        }
        // We've reached the end of the queue.
        logger.debug("Reached the end of the track list, cannot go to next track.");
        currentIndex = items.size() - 1; // Keep index at the end
        return Optional.empty();
    }

    /**
     * Moves to and returns the previous item in the list.
     * @return An Optional containing the previous queue item, or empty if there is no history.
     */
    public Optional<QueueItem> previous() {
        if (currentIndex - 1 >= 0) {
            logger.debug("Previous track: {}", items.get(currentIndex - 1).track());
            currentIndex--;
            return Optional.of(items.get(currentIndex));
        }
        // We are at the beginning, can't go back further.
        logger.debug("At the beginning of the track list, cannot go to previous track.");
        return Optional.empty();
    }

    /**
     * Shuffles the list of upcoming tracks in the queue randomly.
     * This method does NOT affect the playback history or the currently playing track.
     */
    public void shuffle() {
        // The upcoming queue is everything after the current index.
        int upcomingTrackStartIndex = currentIndex + 1;

        // Check if there's anything to shuffle.
        if (upcomingTrackStartIndex >= items.size()) {
            logger.debug("No upcoming tracks to shuffle.");
            return;
        }

        // Get a view of the upcoming tracks.
        List<QueueItem> upcomingQueue = items.subList(upcomingTrackStartIndex, items.size());

        logger.debug("Shuffling {} upcoming tracks.", upcomingQueue.size());

        // Shuffle the sublist in place, which modifies the original list.
        Collections.shuffle(upcomingQueue);
    }

    /**
     * Toggles the repeat mode on or off.
     * @return The new state of repeat mode.
     */
    public boolean toggleRepeat() {
        repeatEnabled = !repeatEnabled;
        logger.debug("Repeat mode is now {}", repeatEnabled ? "enabled" : "disabled");
        return repeatEnabled;
    }

    /**
     * Gets the item that is currently playing.
     * @return An Optional containing the current queue item.
     */
    public Optional<QueueItem> getCurrentItem() {
        if (currentIndex >= 0 && currentIndex < items.size()) {
            return Optional.of(items.get(currentIndex));
        }
        return Optional.empty();
    }

    /**
     * Gets the list of upcoming items in the queue.
     * @return An unmodifiable list of the items that have not yet been played.
     */
    public List<QueueItem> getQueue() {
        if (currentIndex + 1 >= items.size()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(items.subList(currentIndex + 1, items.size()));
    }

    /**
     * Gets the list of items that have already been played.
     * @return An unmodifiable list of the items in the playback history.
     */
    public List<QueueItem> getHistory() {
        if (currentIndex <= 0) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(items.subList(0, currentIndex));
    }

    /**
     * Clears the list of upcoming tracks, but keeps the playback history.
     */
    public void clearQueue() {
        if (currentIndex + 1 < items.size()) {
            logger.debug("Clearing upcoming tracks from index {}", currentIndex + 1);
            items.subList(currentIndex + 1, items.size()).clear();
        } else {
            logger.debug("No upcoming tracks to clear, current index: {}", currentIndex);
        }
    }

    /**
     * Clears the entire track list and history.
     */
    public void clearAll() {
        logger.debug("Clearing all tracks and resetting current index.");
        items.clear();
        currentIndex = -1;
    }

    /**
     * Peeks at the next item without advancing the current index.
     * @return An Optional containing the next queue item, or empty if there is no next item.
     */
    public Optional<QueueItem> peekNext() {
        try {
            if (currentIndex + 1 < items.size()) {
                logger.debug("Peeking next track: {}", items.get(currentIndex + 1).track());
                return Optional.of(items.get(currentIndex + 1));
            } else {
                logger.debug("No next track available, current index: {}, total tracks: {}", currentIndex, items.size());
                return Optional.empty();
            }
        } catch (IndexOutOfBoundsException e) {
            logger.error("Error peeking next track: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Peeks at the previous item without changing the current index.
     * @return An Optional containing the previous queue item, or empty if there is no previous item.
     */
    public Optional<QueueItem> peekPrevious() {
        try {
            if (currentIndex - 1 >= 0) {
                logger.debug("Peeking previous track: {}", items.get(currentIndex - 1).track());
                return Optional.of(items.get(currentIndex - 1));
            } else {
                logger.debug("No previous track available, current index: {}", currentIndex);
                return Optional.empty();
            }
        } catch (IndexOutOfBoundsException e) {
            logger.error("Error peeking previous track: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Finds the index of a track by its identifier in the full track list.
     * @param identifier The unique identifier of the track.
     * @return The index of the track, or -1 if not found.
     */
    public int findTrackIndex(String identifier) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).track().identifier().equals(identifier)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Directly sets the current index. This is used for jumping to a specific track.
     * @param index The new index to set.
     */
    public void setCurrentIndex(int index) {
        if (index >= -1 && index < items.size()) {
            this.currentIndex = index;
        }
    }
}
