package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

public record SpotifySkipPlaybackRequest(
    String deviceId
) {
    public SpotifySkipPlaybackRequest {
        if (deviceId != null && deviceId.isBlank()) {
            deviceId = null;
        }
    }

    public String nextPath() {
        return path("/v1/me/player/next");
    }

    public String previousPath() {
        return path("/v1/me/player/previous");
    }

    private String path(String path) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(path);

        if (deviceId != null) {
            uriBuilder.queryParam("device_id", deviceId.trim());
        }

        return uriBuilder.build().toUriString();
    }
}
