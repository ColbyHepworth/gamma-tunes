package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPlaybackContext(
    @JsonProperty("type")
    String type,

    @JsonProperty("href")
    String href,

    @JsonProperty("external_urls")
    SpotifyExternalUrls externalUrls,

    @JsonProperty("uri")
    String uri
) {
}
