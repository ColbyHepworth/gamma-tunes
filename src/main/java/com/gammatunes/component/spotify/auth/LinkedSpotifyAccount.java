package com.gammatunes.component.spotify.auth;

import java.time.Instant;

public record LinkedSpotifyAccount(
    long discordUserId,
    long guildId,
    String accessToken,
    String refreshToken,
    Instant expiresAt,
    String scope,
    String tokenType
) {
}
