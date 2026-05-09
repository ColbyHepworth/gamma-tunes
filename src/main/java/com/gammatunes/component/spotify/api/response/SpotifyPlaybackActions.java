package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPlaybackActions(
    @JsonProperty("interrupting_playback")
    Optional<Boolean> interruptingPlayback,

    @JsonProperty("pausing")
    Optional<Boolean> pausing,

    @JsonProperty("resuming")
    Optional<Boolean> resuming,

    @JsonProperty("seeking")
    Optional<Boolean> seeking,

    @JsonProperty("skipping_next")
    Optional<Boolean> skippingNext,

    @JsonProperty("skipping_prev")
    Optional<Boolean> skippingPrevious,

    @JsonProperty("toggling_repeat_context")
    Optional<Boolean> togglingRepeatContext,

    @JsonProperty("toggling_shuffle")
    Optional<Boolean> togglingShuffle,

    @JsonProperty("toggling_repeat_track")
    Optional<Boolean> togglingRepeatTrack,

    @JsonProperty("transferring_playback")
    Optional<Boolean> transferringPlayback
) {
}
