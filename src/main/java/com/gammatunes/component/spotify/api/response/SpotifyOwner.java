package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyOwner(
    @JsonProperty("external_urls")
    SpotifyExternalUrls externalUrls,

    @JsonProperty("href")
    String href,

    @JsonProperty("id")
    String id,

    @JsonProperty("type")
    String type,

    @JsonProperty("uri")
    String uri,

    @JsonProperty("display_name")
    Optional<String> displayName
) {
}
