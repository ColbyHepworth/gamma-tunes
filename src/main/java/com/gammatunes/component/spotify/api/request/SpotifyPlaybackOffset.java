package com.gammatunes.component.spotify.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpotifyPlaybackOffset(
    @JsonProperty("position")
    Integer position,

    @JsonProperty("uri")
    String uri
) {
    public SpotifyPlaybackOffset {
        if (position != null && position < 0) {
            throw new IllegalArgumentException("Playback offset position cannot be negative.");
        }
        if (uri != null && uri.isBlank()) {
            uri = null;
        }
        if (position != null && uri != null) {
            throw new IllegalArgumentException("Playback offset can use position or uri, not both.");
        }
    }
}
