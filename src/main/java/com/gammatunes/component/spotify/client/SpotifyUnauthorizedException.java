package com.gammatunes.component.spotify.client;

public class SpotifyUnauthorizedException extends SpotifyApiException {

    public SpotifyUnauthorizedException(String message) {
        super(401, message);
    }
}
