package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyTrackLink(
    @JsonProperty("external_urls")
    Optional<SpotifyExternalUrls> externalUrls,

    @JsonProperty("href")
    Optional<String> href,

    @JsonProperty("id")
    Optional<String> id,

    @JsonProperty("type")
    Optional<String> type,

    @JsonProperty("uri")
    Optional<String> uri
) {
}
