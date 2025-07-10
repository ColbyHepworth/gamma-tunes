package com.gammatunes.backend.audio.player;

import com.gammatunes.backend.common.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the music queue, including upcoming tracks and playback history.
 * This class encapsulates the logic for navigating forwards and backwards through the track list.
 */
public class TrackScheduler {

    private final List<Track> tracks = new ArrayList<>();
    private int currentIndex = -1;

    /**
     * Adds a new track to the end of the queue.
     * @param track The track to add.
     */
    public void enqueue(Track track) {
        tracks.add(track);
    }

    public void addNext(Track track) {
        tracks.add(currentIndex + 1, track);
    }

    /**
     * Moves to and returns the next track in the list.
     * @return An Optional containing the next track, or empty if at the end of the list.
     */
    public Optional<Track> next() {
        if (currentIndex + 1 < tracks.size()) {
            currentIndex++;
            return Optional.of(tracks.get(currentIndex));
        }
        // We've reached the end of the queue.
        currentIndex = tracks.size() - 1; // Keep index at the end
        return Optional.empty();
    }

    /**
     * Moves to and returns the previous track in the list.
     * @return An Optional containing the previous track, or empty if there is no history.
     */
    public Optional<Track> previous() {
        if (currentIndex - 1 >= 0) {
            currentIndex--;
            return Optional.of(tracks.get(currentIndex));
        }
        // We are at the beginning, can't go back further.
        return Optional.empty();
    }

    /**
     * Gets the track that is currently playing.
     * @return An Optional containing the current track.
     */
    public Optional<Track> getCurrentTrack() {
        if (currentIndex >= 0 && currentIndex < tracks.size()) {
            return Optional.of(tracks.get(currentIndex));
        }
        return Optional.empty();
    }

    /**
     * Gets the list of upcoming tracks in the queue.
     * @return An unmodifiable list of the tracks that have not yet been played.
     */
    public List<Track> getQueue() {
        if (currentIndex + 1 >= tracks.size()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(tracks.subList(currentIndex + 1, tracks.size()));
    }

    /**
     * Clears the list of upcoming tracks, but keeps the playback history.
     */
    public void clearQueue() {
        if (currentIndex + 1 < tracks.size()) {
            tracks.subList(currentIndex + 1, tracks.size()).clear();
        }
    }

    /**
     * Clears the entire track list and history.
     */
    public void clearAll() {
        tracks.clear();
        currentIndex = -1;
    }
}
