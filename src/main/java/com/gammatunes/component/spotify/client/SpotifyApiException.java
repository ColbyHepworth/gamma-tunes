package com.gammatunes.component.spotify.client;

public class SpotifyApiException extends RuntimeException {

    private final int status;

    public SpotifyApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
