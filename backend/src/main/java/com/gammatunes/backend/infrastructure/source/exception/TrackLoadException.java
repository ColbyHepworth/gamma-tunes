package com.gammatunes.backend.infrastructure.source.exception;

/**
 * An exception thrown when a query fails to be resolved into a playable track
 * for reasons like a network error or an invalid response from an API.
 */
public class TrackLoadException extends Exception {
    public TrackLoadException(String message) {
        super(message);
    }
    public TrackLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
