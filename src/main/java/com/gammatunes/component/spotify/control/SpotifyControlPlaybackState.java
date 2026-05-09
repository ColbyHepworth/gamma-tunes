package com.gammatunes.component.spotify.control;

import java.time.Instant;

public record SpotifyControlPlaybackState(
    long guildId,
    String lastSpotifyTrackId,
    Instant lastSyncedAt
) {
}
