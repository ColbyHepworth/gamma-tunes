package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

public record SpotifySeekPlaybackRequest(
    Integer positionMs,
    String deviceId
) {
    public SpotifySeekPlaybackRequest {
        if (positionMs == null) {
            throw new IllegalArgumentException("Seek position is required.");
        }
        if (positionMs < 0) {
            throw new IllegalArgumentException("Seek position cannot be negative.");
        }
        if (deviceId != null && deviceId.isBlank()) {
            deviceId = null;
        }
    }

    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/me/player/seek")
            .queryParam("position_ms", positionMs);

        if (deviceId != null) {
            uriBuilder.queryParam("device_id", deviceId.trim());
        }

        return uriBuilder.build().toUriString();
    }
}
