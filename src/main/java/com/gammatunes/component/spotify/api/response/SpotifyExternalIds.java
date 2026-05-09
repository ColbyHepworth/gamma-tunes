package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyExternalIds(
    @JsonProperty("isrc")
    Optional<String> isrc,

    @JsonProperty("ean")
    Optional<String> ean,

    @JsonProperty("upc")
    Optional<String> upc
) {
}
