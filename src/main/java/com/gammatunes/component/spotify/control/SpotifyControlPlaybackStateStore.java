package com.gammatunes.component.spotify.control;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpotifyControlPlaybackStateStore {

    private final ConcurrentHashMap<Long, SpotifyControlPlaybackState> statesByGuildId = new ConcurrentHashMap<>();

    public Optional<SpotifyControlPlaybackState> get(long guildId) {
        return Optional.ofNullable(statesByGuildId.get(guildId));
    }

    public SpotifyControlPlaybackState save(long guildId, String lastSpotifyTrackId, boolean playing) {
        SpotifyControlPlaybackState state = new SpotifyControlPlaybackState(
            guildId,
            lastSpotifyTrackId,
            playing,
            Instant.now()
        );
        statesByGuildId.put(guildId, state);
        return state;
    }

    public Optional<SpotifyControlPlaybackState> clear(long guildId) {
        return Optional.ofNullable(statesByGuildId.remove(guildId));
    }
}
