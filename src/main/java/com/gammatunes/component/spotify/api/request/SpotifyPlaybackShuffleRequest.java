package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

public record SpotifyPlaybackShuffleRequest(
    Boolean state,
    String deviceId
) {
    public SpotifyPlaybackShuffleRequest {
        if (state == null) {
            throw new IllegalArgumentException("Shuffle state is required.");
        }
        if (deviceId != null && deviceId.isBlank()) {
            deviceId = null;
        }
    }

    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/me/player/shuffle")
            .queryParam("state", state);

        if (deviceId != null) {
            uriBuilder.queryParam("device_id", deviceId.trim());
        }

        return uriBuilder.build().toUriString();
    }
}
