package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyQueue(
    @JsonProperty("currently_playing")
    Optional<SpotifyTrack> currentlyPlaying,

    @JsonProperty("queue")
    List<SpotifyTrack> queue
) {
}
