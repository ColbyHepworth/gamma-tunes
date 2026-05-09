package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyTrack(
    @JsonProperty("album")
    Optional<SpotifyAlbum> album,

    @JsonProperty("artists")
    List<SpotifyArtist> artists,

    @JsonProperty("available_markets")
    List<String> availableMarkets,

    @JsonProperty("disc_number")
    int discNumber,

    @JsonProperty("duration_ms")
    int durationMs,

    @JsonProperty("explicit")
    boolean explicit,

    @JsonProperty("external_ids")
    Optional<SpotifyExternalIds> externalIds,

    @JsonProperty("external_urls")
    SpotifyExternalUrls externalUrls,

    @JsonProperty("href")
    String href,

    @JsonProperty("id")
    String id,

    @JsonProperty("is_playable")
    Optional<Boolean> isPlayable,

    @JsonProperty("linked_from")
    Optional<SpotifyTrackLink> linkedFrom,

    @JsonProperty("restrictions")
    Optional<SpotifyRestrictions> restrictions,

    @JsonProperty("name")
    String name,

    @JsonProperty("popularity")
    Optional<Integer> popularity,

    @JsonProperty("preview_url")
    Optional<String> previewUrl,

    @JsonProperty("track_number")
    int trackNumber,

    @JsonProperty("type")
    String type,

    @JsonProperty("uri")
    String uri,

    @JsonProperty("is_local")
    boolean isLocal
) {
}
