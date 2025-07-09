package com.gammatunes.backend.audio.api;

import com.gammatunes.backend.common.model.PlayerState;
import com.gammatunes.backend.common.model.Session;
import com.gammatunes.backend.common.model.Track;

import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for a single audio player tied to a specific session.
 * <p>
 * This interface controls the playback, queue, and state for one session (e.g., one Discord server).
 */
public interface AudioPlayer {

    /**
     * Adds a track to the queue. If nothing is currently playing, it starts playing this track immediately.
     *
     * @param track The track to enqueue and potentially play.
     */
    void enqueue(Track track);


    /**
     * Clears the entire queue of tracks.
     * <p>
     * This does not stop the currently playing track; it only clears the queue.
     */
    void clearQueue();

    /**
     * Pauses the currently playing track.
     */
    void pause();

    /**
     * Resumes playback of a paused track.
     */
    void resume();

    /**
     * Stops the player completely, clears the queue, and disconnects from the voice channel.
     */
    void stop();

    /**
     * Skips the currently playing track and starts playing the next one in the queue, if available.
     *
     * @return The track that was skipped, or an empty Optional if nothing was playing.
     */
    Optional<Track> skip();

    /**
     * Goes back to the previous track in the queue, if available.
     *
     * @return The track that was played before the current one, or an empty Optional if there is no previous track.
     */
    Optional<Track> previous();

    /**
     * Retrieves an immutable list of the tracks currently in the queue.
     *
     * @return A list of tracks in the queue.
     */
    List<Track> getQueue();

    /**
     * Gets the track that is currently playing.
     *
     * @return An Optional containing the currently playing track, or empty if nothing is playing.
     */
    Optional<Track> getCurrentlyPlaying();

    /**
     * Gets the current state of the player.
     *
     * @return The current {@link PlayerState}.
     */
    PlayerState getState();

    /**
     * Gets the session this player is associated with.
     *
     * @return The player's {@link Session}.
     */
    Session getSession();
}
