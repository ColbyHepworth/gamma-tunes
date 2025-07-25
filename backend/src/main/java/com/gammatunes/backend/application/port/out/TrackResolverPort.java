package com.gammatunes.backend.application.port.out;

import com.gammatunes.backend.infrastructure.source.exception.TrackLoadException;
import com.gammatunes.backend.domain.model.Track;

/**
 * Defines the contract for a source that can resolve a query into a playable Track.
 * Each implementation will handle a specific source (e.g., YouTube, Spotify).
 */
public interface TrackResolverPort {

    /**
     * Checks if this resolver can handle the given query.
     *
     * @param query The user's input (e.g., a URL or search term).
     * @return true if this resolver can handle the query, false otherwise.
     */
    boolean canResolve(String query);

    /**
     * Resolves a query into a playable Track object.
     *
     * @param query The user's input.
     * @return A {@link Track} object containing the details of the playable audio.
     * @throws TrackLoadException if the track cannot be resolved or loaded.
     */
    Track resolve(String query) throws TrackLoadException;
}
