package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyRecommendationSeed(
    @JsonProperty("afterFilteringSize")
    int afterFilteringSize,

    @JsonProperty("afterRelinkingSize")
    int afterRelinkingSize,

    @JsonProperty("href")
    Optional<String> href,

    @JsonProperty("id")
    String id,

    @JsonProperty("initialPoolSize")
    int initialPoolSize,

    @JsonProperty("type")
    String type
) {
}
