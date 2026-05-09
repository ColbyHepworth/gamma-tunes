package com.gammatunes.component.spotify.resolver;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

@Component
public class SpotifyUrlParser {

    private static final String OPEN_SPOTIFY_HOST = "open.spotify.com";
    private static final String SPOTIFY_URI_SCHEME = "spotify";

    public Optional<SpotifyResource> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String value = input.trim();
        return value.startsWith(SPOTIFY_URI_SCHEME + ":")
            ? parseSpotifyUri(value)
            : parseSpotifyUrl(value);
    }

    private Optional<SpotifyResource> parseSpotifyUri(String value) {
        String[] parts = value.split(":", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }

        return resource(parts[1], parts[2]);
    }

    private Optional<SpotifyResource> parseSpotifyUrl(String value) {
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        if (!OPEN_SPOTIFY_HOST.equalsIgnoreCase(uri.getHost())) {
            return Optional.empty();
        }

        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return Optional.empty();
        }

        String[] parts = path.split("/");
        if (parts.length < 3) {
            return Optional.empty();
        }

        return resource(parts[1], parts[2]);
    }

    private Optional<SpotifyResource> resource(String type, String id) {
        SpotifyResourceType resourceType = switch (type.toLowerCase()) {
            case "track" -> SpotifyResourceType.TRACK;
            case "playlist" -> SpotifyResourceType.PLAYLIST;
            default -> null;
        };

        if (resourceType == null || id == null || id.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new SpotifyResource(resourceType, id));
    }
}
