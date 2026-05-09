package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

public record SpotifyPlaybackVolumeRequest(
    Integer volumePercent,
    String deviceId
) {
    private static final int MIN_VOLUME = 0;
    private static final int MAX_VOLUME = 100;

    public SpotifyPlaybackVolumeRequest {
        if (volumePercent == null) {
            throw new IllegalArgumentException("Playback volume is required.");
        }
        if (volumePercent < MIN_VOLUME || volumePercent > MAX_VOLUME) {
            throw new IllegalArgumentException("Playback volume must be between 0 and 100.");
        }
        if (deviceId != null && deviceId.isBlank()) {
            deviceId = null;
        }
    }

    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/me/player/volume")
            .queryParam("volume_percent", volumePercent);

        if (deviceId != null) {
            uriBuilder.queryParam("device_id", deviceId.trim());
        }

        return uriBuilder.build().toUriString();
    }
}
