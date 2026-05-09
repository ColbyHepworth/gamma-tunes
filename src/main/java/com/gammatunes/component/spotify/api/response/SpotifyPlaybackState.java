package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPlaybackState(
    @JsonProperty("device")
    SpotifyDevice device,

    @JsonProperty("repeat_state")
    String repeatState,

    @JsonProperty("shuffle_state")
    boolean shuffleState,

    @JsonProperty("context")
    Optional<SpotifyPlaybackContext> context,

    @JsonProperty("timestamp")
    long timestamp,

    @JsonProperty("progress_ms")
    Optional<Integer> progressMs,

    @JsonProperty("is_playing")
    boolean isPlaying,

    @JsonProperty("item")
    Optional<SpotifyTrack> item,

    @JsonProperty("currently_playing_type")
    String currentlyPlayingType,

    @JsonProperty("actions")
    SpotifyPlaybackActions actions
) {
}
