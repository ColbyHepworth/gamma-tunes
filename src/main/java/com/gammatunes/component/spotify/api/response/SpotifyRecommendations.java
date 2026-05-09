package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyRecommendations(
    @JsonProperty("seeds")
    List<SpotifyRecommendationSeed> seeds,

    @JsonProperty("tracks")
    List<SpotifyRecommendedTrack> tracks
) {
}
