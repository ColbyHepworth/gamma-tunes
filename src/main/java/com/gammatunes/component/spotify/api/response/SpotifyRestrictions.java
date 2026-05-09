package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyRestrictions(
    @JsonProperty("reason")
    String reason
) {
}
