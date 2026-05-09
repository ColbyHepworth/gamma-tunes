package com.gammatunes.component.spotify.resolver;

public record SpotifyResource(
    SpotifyResourceType type,
    String id
) {
    public SpotifyResource {
        if (type == null) {
            throw new IllegalArgumentException("Spotify resource type is required.");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Spotify resource id is required.");
        }
        id = id.trim();
    }
}
