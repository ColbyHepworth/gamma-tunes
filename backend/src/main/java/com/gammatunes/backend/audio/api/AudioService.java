package com.gammatunes.backend.audio.api;

import com.gammatunes.backend.audio.exception.TrackLoadException;
import com.gammatunes.backend.common.model.Session;

/**
 * The main entry point for interacting with the audio module.
 * <p>
 * This service is responsible for managing {@link AudioPlayer} instances for various sessions.
 * It acts as a factory and central point of control.
 */
public interface AudioService {

    /**
     * Retrieves an existing AudioPlayer for the given session, or creates a new one if it doesn't exist.
     *
     * @param session The session for which to get the player.
     * @return The {@link AudioPlayer} for the session.
     */
    AudioPlayer getOrCreatePlayer(Session session);

    /**
     * A high-level convenience method to resolve a query and enqueue the resulting track(s).
     * This method orchestrates the interaction with the clients
     *
     * @param session The session in which to play the track.
     * @param query   The user's input (e.g., a URL or search term).
     * @throws TrackLoadException if the query cannot be resolved into a playable track.
     */
    void play(Session session, String query) throws TrackLoadException;

    void playNow(Session session, String query) throws TrackLoadException;
}
