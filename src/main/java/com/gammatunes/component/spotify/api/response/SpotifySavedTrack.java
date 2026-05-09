package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifySavedTrack(
    @JsonProperty("added_at")
    OffsetDateTime addedAt,

    @JsonProperty("track")
    SpotifyTrack track
) {
}
