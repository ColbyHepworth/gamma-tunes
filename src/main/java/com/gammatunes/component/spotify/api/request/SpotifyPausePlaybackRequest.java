package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

public record SpotifyPausePlaybackRequest(
    String deviceId
) {
    public SpotifyPausePlaybackRequest {
        if (deviceId != null && deviceId.isBlank()) {
            deviceId = null;
        }
    }

    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/me/player/pause");

        if (deviceId != null) {
            uriBuilder.queryParam("device_id", deviceId.trim());
        }

        return uriBuilder.build().toUriString();
    }
}
