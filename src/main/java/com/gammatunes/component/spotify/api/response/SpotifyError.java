package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyError(
    @JsonProperty("status")
    int status,

    @JsonProperty("message")
    String message
) {
}
