package com.gammatunes.backend.application.port.in;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.exception.TrackLoadException;

public interface AudioControlUseCase {

    /**
     * Plays a track based on the provided session ID and query.
     * If the track is not already loaded, it will be resolved and loaded.
     *
     * @param sessionId The unique identifier for the user's session.
     * @param query The user's input (URL or search term) to resolve into a playable track.
     * @throws TrackLoadException if the track cannot be resolved or loaded.
     */
    PlayerOutcome play(String sessionId, String query) throws TrackLoadException;

    /**
     * Immediately plays a track based on the provided session ID and query.
     * This method is used to play a track without queuing it.
     *
     * @param sessionId The unique identifier for the user's session.
     * @param query The user's input (URL or search term) to resolve into a playable track.
     * @throws TrackLoadException if the track cannot be resolved or loaded.
     */
    PlayerOutcome playNow(String sessionId, String query) throws TrackLoadException;

    /**
     * Pauses the audio playback for the specified session.
     *
     * @param sessionId The unique identifier for the user's session.
     */
    PlayerOutcome pause(String sessionId);

    /**
     * Resumes audio playback for the specified session.
     *
     * @param sessionId The unique identifier for the user's session.
     */
    PlayerOutcome resume(String sessionId);

    /**
     * Stops the audio playback and clears the queue for the specified session.
     *
     * @param sessionId The unique identifier for the user's session.
     */
    PlayerOutcome stop(String sessionId);

    /**
     * Skips the currently playing track and starts playing the next one in the queue.
     * If there are no tracks in the queue, it will stop playback.
     *
     * @param sessionId The unique identifier for the user's session.
     */
    PlayerOutcome skip(String sessionId);


    /**
     * Clears the entire queue of tracks for the specified session.
     * This does not stop the currently playing track; it only clears the queue.
     *
     * @param sessionId The unique identifier for the user's session.
     */
    PlayerOutcome previous(String sessionId);

    /**
     * Jumps to a specific track in the queue based on its identifier.
     * This will start playing the specified track immediately.
     *
     * @param sessionId The unique identifier for the user's session.
     * @param trackIdentifier The identifier of the track to jump to.
     * @return The outcome of the jump operation, which includes the track that was played or an indication of stopping.
     */
    PlayerOutcome jumpToTrack(String sessionId, String trackIdentifier);

    /**
     * Shuffles the current queue of tracks for the specified session.
     * This will randomize the order of tracks in the queue.
     * @param sessionId The unique identifier for the user's session.
     */
    PlayerOutcome shuffle(String sessionId);

    /**
     * Toggles the repeat mode for the specified session.
     * If repeat mode is enabled, the current track will be repeated after it finishes.
     * If disabled, the player will continue to the next track in the queue.
     *
     * @param sessionId The unique identifier for the user's session.
     * @return The new state of repeat mode.
     */
    PlayerOutcome toggleRepeat(String sessionId);

}
