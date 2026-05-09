package com.gammatunes.component.spotify.client;

public class SpotifyForbiddenException extends SpotifyApiException {

    public SpotifyForbiddenException(String message) {
        super(403, message);
    }
}
