package com.gammatunes.component.spotify.auth;

import java.util.Optional;

public interface SpotifyTokenStore {

    void save(LinkedSpotifyAccount account);

    Optional<LinkedSpotifyAccount> findByDiscordUserId(long discordUserId);
}
