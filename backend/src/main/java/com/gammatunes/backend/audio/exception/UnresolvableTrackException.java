package com.gammatunes.backend.audio.exception;

/**
 * A specific type of TrackLoadException thrown when a query is valid,
 * but no corresponding track can be found (e.g., a search with no results).
 */
public class UnresolvableTrackException extends TrackLoadException {
    public UnresolvableTrackException(String message) {
        super(message);
    }
}
