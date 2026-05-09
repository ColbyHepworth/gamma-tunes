package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyDevice(
    @JsonProperty("id")
    Optional<String> id,

    @JsonProperty("is_active")
    boolean isActive,

    @JsonProperty("is_private_session")
    boolean isPrivateSession,

    @JsonProperty("is_restricted")
    boolean isRestricted,

    @JsonProperty("name")
    String name,

    @JsonProperty("type")
    String type,

    @JsonProperty("volume_percent")
    Optional<Integer> volumePercent,

    @JsonProperty("supports_volume")
    boolean supportsVolume
) {
}
