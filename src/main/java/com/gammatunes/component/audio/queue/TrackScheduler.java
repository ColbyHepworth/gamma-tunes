package com.gammatunes.component.audio.queue;

import dev.arbjerg.lavalink.client.player.Track;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages a queue of tracks for playback, allowing operations like enqueue, push,
 * jump to specific tracks, and navigation through the queue.
 * Tracks are stored in a list, and the current track index is maintained.
 */
@Slf4j
public class TrackScheduler {

    private final List<Track> tracks = new ArrayList<>();
    private int currentIndex = -1;

    /**
     * Enqueues a track to the end of the queue.
     * If the queue is empty, sets the current index to 0.
     *
     * @param track The track to enqueue.
     */
    public synchronized void enqueue(Track track) {
        log.debug("Enqueuing track: {}", track.getInfo().getIdentifier());
        tracks.add(track);
        if (currentIndex == -1) currentIndex = 0;
    }

    /**
     * Pushes a track into the queue at the position after the current track.
     * If no current track is set, behaves like enqueue.
     *
     * @param track The track to push into the queue.
     */
    public synchronized void push(Track track) {
        log.debug("Pushing track: {}", track.getInfo().getIdentifier());
        if (currentIndex == -1) {
            enqueue(track);
            return;
        }
        tracks.add(currentIndex + 1, track);
    }

    /**
     * Moves to the next track in the queue.
     * If already at the last track, returns an empty Optional.
     *
     * @return The next track if available, otherwise an empty Optional.
     */
    public synchronized Optional<Track> next() {
        if (currentIndex + 1 < tracks.size()) {
            currentIndex++;
            log.debug("Next track: {}", tracks.get(currentIndex).getInfo().getIdentifier());
            return Optional.of(tracks.get(currentIndex));
        }
        log.debug("No next track available");
        currentIndex = tracks.size() - 1;
        return Optional.empty();
    }

    /**
     * Moves to the previous track in the queue.
     * If already at the first track, returns an empty Optional.
     *
     * @return The previous track if available, otherwise an empty Optional.
     */
    public synchronized Optional<Track> previous() {
        if (currentIndex - 1 >= 0) {
            currentIndex--;
            log.debug("Previous track: {}", tracks.get(currentIndex).getInfo().getIdentifier());
            return Optional.of(tracks.get(currentIndex));
        }
        log.debug("No previous track available");
        return Optional.empty();
    }

    /**
     * Jumps to a specific track in the queue by its index.
     * If the index is valid, updates the current index and returns the track.
     *
     * @param track The track to jump to.
     * @return The track if found, otherwise an empty Optional.
     */
    public synchronized Optional<Track> jumpToTrack(Track track) {
        log.debug("Jumping track: {}", track.getInfo().getIdentifier());
        int index = findTrackIndex(track);
        return jumpToIndex(index);
    }

    /**
     * Jumps to a specific track in the queue by its identifier.
     * Searches through the queue for a track with the matching identifier.
     *
     * @param identifier The identifier of the track to jump to.
     * @return The track if found, otherwise an empty Optional.
     */
    public Optional<Track> jumpToIdentifier(String identifier) {
        log.debug("Jumping to track with identifier: {}", identifier);
        return getQueue().stream()
            .filter(t -> Objects.equals(t.getInfo().getIdentifier(), identifier))
            .findFirst()
            .flatMap(this::jumpToTrack);
    }

    /**
     * Jumps to a track based on a prefixed identifier.
     * Supports queue jumps (q:), history jumps (h:), and current track (c:).
     * If the identifier is not recognized, falls back to legacy identifier lookup.
     *
     * @param prefixedIdentifier The prefixed identifier to jump to.
     * @return The track if found, otherwise an empty Optional.
     */
    public synchronized Optional<Track> jumpToPrefixedIdentifier(String prefixedIdentifier) {
        log.debug("Jumping to prefixed identifier: {}", prefixedIdentifier);
        if (prefixedIdentifier.startsWith("q:")) {
            String[] parts = prefixedIdentifier.split(":", 3);
            if (parts.length >= 2) {
                try {
                    int queueIndex = Integer.parseInt(parts[1]);
                    int absoluteIndex = currentIndex + 1 + queueIndex;
                    return jumpToIndex(absoluteIndex);
                } catch (NumberFormatException e) {
                    log.warn("Invalid queue index in identifier: {}", prefixedIdentifier);
                }
            }
        } else if (prefixedIdentifier.startsWith("h:")) {
            String[] parts = prefixedIdentifier.split(":", 3);
            if (parts.length >= 2) {
                try {
                    int historyIndex = Integer.parseInt(parts[1]);
                    return jumpToIndex(historyIndex);
                } catch (NumberFormatException e) {
                    log.warn("Invalid history index in identifier: {}", prefixedIdentifier);
                }
            }
        } else if (prefixedIdentifier.startsWith("c:")) {
            return getCurrentTrack();
        } else {
            return jumpToIdentifier(prefixedIdentifier);
        }
        log.warn("Unrecognized prefixed identifier: {}", prefixedIdentifier);
        return Optional.empty();
    }

    /**
     * Jumps to a specific index in the queue.
     * If the index is valid, updates the current index and returns the track at that index.
     *
     * @param index The index to jump to.
     * @return The track at the specified index if valid, otherwise an empty Optional.
     */
    public synchronized Optional<Track> jumpToIndex(int index) {
        log.debug("Jumping to index: {}", index);
        if (index >= 0 && index < tracks.size()) {
            currentIndex = index;
            return Optional.of(tracks.get(currentIndex));
        }
        log.warn("Attempted to jump to invalid index: {}", index);
        return Optional.empty();
    }

    /**
     * Retrieves the current track in the queue.
     * If the current index is valid, returns the track at that index.
     *
     * @return The current track if available, otherwise an empty Optional.
     */
    public synchronized Optional<Track> getCurrentTrack() {
        log.debug("Getting current track at index: {}", currentIndex);
        if (currentIndex >= 0 && currentIndex < tracks.size()) {
            return Optional.of(tracks.get(currentIndex));
        }
        log.warn("Current index is invalid: {}", currentIndex);
        return Optional.empty();
    }

    /**
     * Retrieves the next track in the queue without advancing the current index.
     * If there is no next track, returns an empty Optional.
     *
     * @return The next track if available, otherwise an empty Optional.
     */
    public synchronized List<Track> getQueue() {
        log.debug("Getting queue from index: {}", currentIndex);
        if (currentIndex + 1 >= tracks.size()) return List.of();
        return List.copyOf(tracks.subList(currentIndex + 1, tracks.size()));
    }

    /**
     * Retrieves the history of tracks played before the current track.
     * If no tracks have been played, returns an empty list.
     *
     * @return A list of tracks in the history.
     */
    public synchronized List<Track> getHistory() {
        log.debug("Getting history up to index: {}", currentIndex);
        if (currentIndex <= 0) return List.of();
        return List.copyOf(tracks.subList(0, currentIndex));
    }

    /**
     * Clears the entire queue, removing all tracks and resetting the current index.
     */
    public synchronized void clearAll() {
        log.debug("Clearing all tracks from the queue");
        tracks.clear();
        currentIndex = -1;
    }

    /**
     * Clears the queue of tracks that have been played, keeping only the current track.
     * If no current track is set, clears the entire queue.
     */
    public synchronized int findTrackIndex(Track track) {
        String id = track.getInfo().getIdentifier();
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getInfo().getIdentifier().equals(id)) return i;
        }
        return -1;
    }

    /**
     * Shuffles the queue of tracks starting from the current index + 1.
     * If there are no tracks to shuffle, does nothing.
     */
    public synchronized void shuffle() {
        log.debug("Shuffling tracks from index: {}", currentIndex + 1);
        int start = currentIndex + 1;
        if (start >= tracks.size()) return;
        java.util.Collections.shuffle(tracks.subList(start, tracks.size()));
    }

    /**
     * Checks if the queue is empty.
     * Returns true if there are no tracks in the queue, false otherwise.
     *
     * @return true if the queue is empty, false otherwise.
     */
    public synchronized boolean isEmpty() {
        return tracks.isEmpty();
    }

    /**
     * Returns the number of tracks currently in the queue.
     *
     * @return The size of the queue.
     */
    public synchronized int size() {
        return tracks.size();
    }
}
