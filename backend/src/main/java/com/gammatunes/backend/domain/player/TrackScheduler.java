package com.gammatunes.backend.domain.player;

import com.gammatunes.backend.domain.model.Track;
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

    private final List<Track> tracks = new ArrayList<>();
    private int currentIndex = -1;

    @Getter
    private boolean repeatEnabled = false;

    /**
     * Adds a new track to the end of the queue.
     * @param track The track to add.
     */
    public void enqueue(Track track) {
        logger.debug("Enqueuing track: {}", track);
        tracks.add(track);
    }

    public void addNext(Track track) {
        logger.debug("Adding track to the front of the queue: {}", track);
        tracks.add(currentIndex + 1, track);
    }

    /**
     * Moves to and returns the next track in the list.
     * @return An Optional containing the next track, or empty if at the end of the list.
     */
    public Optional<Track> next() {
        if (currentIndex + 1 < tracks.size()) {
            logger.debug("Next track: {}", tracks.get(currentIndex + 1));
            currentIndex++;
            return Optional.of(tracks.get(currentIndex));
        }
        // We've reached the end of the queue.
        logger.debug("Reached the end of the track list, cannot go to next track.");
        currentIndex = tracks.size() - 1; // Keep index at the end
        return Optional.empty();
    }

    /**
     * Moves to and returns the previous track in the list.
     * @return An Optional containing the previous track, or empty if there is no history.
     */
    public Optional<Track> previous() {
        if (currentIndex - 1 >= 0) {
            logger.debug("Previous track: {}", tracks.get(currentIndex - 1));
            currentIndex--;
            return Optional.of(tracks.get(currentIndex));
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
        if (upcomingTrackStartIndex >= tracks.size()) {
            logger.debug("No upcoming tracks to shuffle.");
            return;
        }

        // Get a view of the upcoming tracks.
        List<Track> upcomingQueue = tracks.subList(upcomingTrackStartIndex, tracks.size());

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
     * Gets the track that is currently playing.
     * @return An Optional containing the current track.
     */
    public Optional<Track> getCurrentTrack() {
        if (currentIndex >= 0 && currentIndex < tracks.size()) {
            logger.debug("Current track: {}", tracks.get(currentIndex));
            return Optional.of(tracks.get(currentIndex));
        }
        logger.debug("No current track, index is out of bounds: {}", currentIndex);
        return Optional.empty();
    }

    /**
     * Gets the list of upcoming tracks in the queue.
     * @return An unmodifiable list of the tracks that have not yet been played.
     */
    public List<Track> getQueue() {
        if (currentIndex + 1 >= tracks.size()) {
            logger.debug("No upcoming tracks, current index: {}, total tracks: {}", currentIndex, tracks.size());
            return Collections.emptyList();
        }
        logger.debug("Upcoming tracks from index {}: {}", currentIndex + 1, tracks.subList(currentIndex + 1, tracks.size()));
        return Collections.unmodifiableList(tracks.subList(currentIndex + 1, tracks.size()));
    }

    /**
     * Clears the list of upcoming tracks, but keeps the playback history.
     */
    public void clearQueue() {
        if (currentIndex + 1 < tracks.size()) {
            logger.debug("Clearing upcoming tracks from index {}", currentIndex + 1);
            tracks.subList(currentIndex + 1, tracks.size()).clear();
        } else {
            logger.debug("No upcoming tracks to clear, current index: {}", currentIndex);
        }
    }

    /**
     * Clears the entire track list and history.
     */
    public void clearAll() {
    logger.debug("Clearing all tracks and resetting current index.");
        tracks.clear();
        currentIndex = -1;
    }

    /**
     * Peeks at the next track without advancing the current index.
     * @return An Optional containing the next track, or empty if there is no next track.
     */
    public Optional<Track> peekNext() {
        try {
            if (currentIndex + 1 < tracks.size()) {
                logger.debug("Peeking next track: {}", tracks.get(currentIndex + 1));
                return Optional.of(tracks.get(currentIndex + 1));
            } else {
                logger.debug("No next track available, current index: {}, total tracks: {}", currentIndex, tracks.size());
                return Optional.empty();
            }
        } catch (IndexOutOfBoundsException e) {
            logger.error("Error peeking next track: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Peeks at the previous track without changing the current index.
     * @return An Optional containing the previous track, or empty if there is no previous track.
     */
    public Optional<Track> peekPrevious() {
        try {
            if (currentIndex - 1 >= 0) {
                logger.debug("Peeking previous track: {}", tracks.get(currentIndex - 1));
                return Optional.of(tracks.get(currentIndex - 1));
            } else {
                logger.debug("No previous track available, current index: {}", currentIndex);
                return Optional.empty();
            }
        } catch (IndexOutOfBoundsException e) {
            logger.error("Error peeking previous track: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
