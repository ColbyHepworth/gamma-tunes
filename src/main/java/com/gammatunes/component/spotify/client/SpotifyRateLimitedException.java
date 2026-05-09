package com.gammatunes.component.spotify.client;

import java.time.Duration;
import java.util.Optional;

public class SpotifyRateLimitedException extends SpotifyApiException {

    private final Duration retryAfter;

    public SpotifyRateLimitedException(String message, Duration retryAfter) {
        super(429, message);
        this.retryAfter = retryAfter;
    }

    public Optional<Duration> retryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
