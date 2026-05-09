package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPlaylistTrackItem(
    @JsonProperty("added_at")
    Optional<OffsetDateTime> addedAt,

    @JsonProperty("added_by")
    Optional<SpotifyOwner> addedBy,

    @JsonProperty("is_local")
    boolean isLocal,

    @JsonProperty("item")
    Optional<SpotifyTrack> item,

    @JsonProperty("track")
    Optional<SpotifyTrack> track
) {
}
