package com.gammatunes.component.spotify.auth;

public record SpotifyAccessToken(String value) {

    public SpotifyAccessToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Spotify access token cannot be blank.");
        }
    }
}
