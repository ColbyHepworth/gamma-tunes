package com.gammatunes.component.spotify.control;

public record SpotifyControlAudioSnapshot(
    String spotifyDeviceId,
    Integer originalVolume,
    boolean originallyPlaying
) {
}
