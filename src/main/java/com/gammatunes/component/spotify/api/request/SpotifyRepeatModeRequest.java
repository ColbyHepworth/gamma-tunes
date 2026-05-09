package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

public record SpotifyRepeatModeRequest(
    SpotifyRepeatMode state,
    String deviceId
) {
    public SpotifyRepeatModeRequest {
        if (state == null) {
            throw new IllegalArgumentException("Repeat mode state is required.");
        }
        if (deviceId != null && deviceId.isBlank()) {
            deviceId = null;
        }
    }

    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/me/player/repeat")
            .queryParam("state", state.value());

        if (deviceId != null) {
            uriBuilder.queryParam("device_id", deviceId.trim());
        }

        return uriBuilder.build().toUriString();
    }
}
