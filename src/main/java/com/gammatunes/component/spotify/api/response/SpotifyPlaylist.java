package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPlaylist(
    @JsonProperty("collaborative")
    boolean collaborative,

    @JsonProperty("description")
    Optional<String> description,

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

    @JsonProperty("owner")
    SpotifyOwner owner,

    @JsonProperty("public")
    Optional<Boolean> publicStatus,

    @JsonProperty("snapshot_id")
    String snapshotId,

    @JsonProperty("items")
    Optional<SpotifyPlaylistTracksPage> items,

    @JsonProperty("tracks")
    Optional<SpotifyPlaylistTracksPage> tracks,

    @JsonProperty("type")
    String type,

    @JsonProperty("uri")
    String uri
) {
}
