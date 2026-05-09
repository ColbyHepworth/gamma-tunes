package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPageOfTracks(
    @JsonProperty("href")
    String href,

    @JsonProperty("limit")
    int limit,

    @JsonProperty("next")
    Optional<String> next,

    @JsonProperty("offset")
    int offset,

    @JsonProperty("previous")
    Optional<String> previous,

    @JsonProperty("total")
    int total,

    @JsonProperty("items")
    List<SpotifySavedTrack> items
) {
}
