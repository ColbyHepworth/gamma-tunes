package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyArtist(
    @JsonProperty("external_urls")
    SpotifyExternalUrls externalUrls,

    @JsonProperty("href")
    String href,

    @JsonProperty("id")
    String id,

    @JsonProperty("name")
    String name,

    @JsonProperty("type")
    String type,

    @JsonProperty("uri")
    String uri
) {
}
