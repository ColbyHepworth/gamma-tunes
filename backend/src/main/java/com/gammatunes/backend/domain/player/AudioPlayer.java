package com.gammatunes.backend.domain.player;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.model.Track;

import java.util.List;
import java.util.Optional;

/**
 * Pure domain-level abstraction for an audio player tied to a single {@link Session}.
 * <p>Infrastructure adapters (e.g. Lavaplayer, FFmpeg) implement this interface;
 * application and presentation layers depend only on it.</p>
 */
public interface AudioPlayer {

    /**
     * Plays a track, adding it to the queue if necessary.
     * <p>If the player is currently playing another track, this will add the new track to the queue.</p>
     *
     * @param track The track to play.
     * @return The outcome of the play operation, which includes the track that was played or queued.
     */
    PlayerOutcome play(Track track);

    /**
     * Immediately plays a track without queuing it.
     * <p>This is used for commands that require immediate playback, such as "play now".</p>
     *
     * @param track The track to play immediately.
     * @return The outcome of the play operation, which includes the track that was played.
     */
    PlayerOutcome playNow(Track track);

    /**
     * Repeats the currently playing track.
     * <p>This will cause the player to play the same track again when it reaches the end.</p>
     *
     * @return The outcome of the repeat operation, which indicates whether the player was successfully set to repeat.
     */
    PlayerOutcome repeat();

    /**
     * Skips the currently playing track and plays the next track in the queue.
     * <p>If there are no tracks in the queue, this will stop playback.</p>
     *
     * @return The outcome of the skip operation, which includes the next track that was played or an indication of stopping.
     */
    PlayerOutcome skip();

    /**
     * Pauses the currently playing track.
     * <p>If the player is already paused, this will have no effect.</p>
     *
     * @return The outcome of the pause operation, which indicates whether the player was successfully paused.
     */
    PlayerOutcome pause();

    /**
     * Resumes playback of the currently paused track.
     * <p>If the player is already playing, this will have no effect.</p>
     *
     * @return The outcome of the resume operation, which indicates whether the player was successfully resumed.
     */
    PlayerOutcome resume();

    /**
     * Stops the audio player and clears the current track.
     * <p>This will stop playback and clear the queue.</p>
     *
     * @return The outcome of the stop operation, which indicates whether the player was successfully stopped.
     */
    PlayerOutcome stop();

    /**
     * Plays the previous track in the queue.
     * <p>This will return to the last track that was played before the current one.</p>
     *
     * @return The outcome of the previous operation, which includes the track that was played or an indication of stopping.
     */
    PlayerOutcome previous();

    /**
     * Shuffles the current queue of tracks.
     * <p>This will randomize the order of tracks in the queue.</p>
     *
     * @return The outcome of the shuffle operation, which indicates whether the queue was successfully shuffled.
     */
    PlayerOutcome shuffle();

    /**
     * Enables repeat mode for the player.
     * <p>This will cause the player to repeat the currently playing track when it reaches the end.</p>
     *
     * @return The outcome of enabling repeat mode, which indicates whether it was successfully enabled.
     */
    PlayerOutcome toggleRepeat();

    /**
     * Clears the entire queue of tracks.
     * <p>This does not stop the currently playing track; it only clears the queue.</p>
     */
    void clearQueue();

    /**
     * Returns the current queue of tracks.
     * <p>This includes all tracks that are queued for playback, excluding the currently playing track.</p>
     *
     * @return A list of tracks in the queue.
     */
    List<Track> getQueue();

    /**
     * Returns the currently playing track, if any.
     * <p>This will return an empty Optional if no track is currently playing.</p>
     *
     * @return An Optional containing the currently playing track, or empty if none.
     */
    Optional<Track> getCurrentlyPlaying();

    /**
     * Returns the current state of the player.
     * <p>This indicates whether the player is playing, paused, or stopped.</p>
     *
     * @return The current PlayerState.
     */
    PlayerState getState();

    /**
     * Checks if repeat mode is enabled.
     * <p>This indicates whether the player will repeat the currently playing track when it finishes.</p>
     *
     * @return True if repeat mode is enabled, false otherwise.
     */
    boolean isRepeatEnabled();

    /**
     * Returns the current position of the track being played, in milliseconds.
     * <p>This is useful for displaying the current playback position in a UI.</p>
     *
     * @return The current track position in milliseconds.
     */
    long getTrackPosition();

    /**
     * Returns the session associated with this audio player.
     * <p>This is used to identify which user's session this player belongs to.</p>
     *
     * @return The Session associated with this audio player.
     */
    Session getSession();
}
