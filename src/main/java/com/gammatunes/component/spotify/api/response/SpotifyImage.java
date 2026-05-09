package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyImage(
    @JsonProperty("url")
    String url,

    @JsonProperty("height")
    Optional<Integer> height,

    @JsonProperty("width")
    Optional<Integer> width
) {
}
