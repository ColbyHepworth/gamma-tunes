package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyAlbum(
    @JsonProperty("album_type")
    String albumType,

    @JsonProperty("total_tracks")
    int totalTracks,

    @JsonProperty("available_markets")
    List<String> availableMarkets,

    @JsonProperty("external_urls")
    SpotifyExternalUrls externalUrls,

    @JsonProperty("href")
    String href,

    @JsonProperty("id")
    String id,

    @JsonProperty("images")
    List<SpotifyImage> images,

    @JsonProperty("name")
    String name,

    @JsonProperty("release_date")
    String releaseDate,

    @JsonProperty("release_date_precision")
    String releaseDatePrecision,

    @JsonProperty("restrictions")
    Optional<SpotifyRestrictions> restrictions,

    @JsonProperty("type")
    String type,

    @JsonProperty("uri")
    String uri,

    @JsonProperty("artists")
    List<SpotifyArtist> artists
) {
}
