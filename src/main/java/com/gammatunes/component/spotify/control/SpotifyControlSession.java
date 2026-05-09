package com.gammatunes.component.spotify.control;

import java.time.Instant;

public record SpotifyControlSession(
    long guildId,
    long controllingDiscordUserId,
    long voiceChannelId,
    Long textChannelId,
    Instant startedAt
) {
}
