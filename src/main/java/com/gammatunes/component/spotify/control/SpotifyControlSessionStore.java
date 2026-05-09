package com.gammatunes.component.spotify.control;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpotifyControlSessionStore {

    private final ConcurrentHashMap<Long, SpotifyControlSession> sessionsByGuildId = new ConcurrentHashMap<>();

    public SpotifyControlSession startControl(long guildId, long controllingDiscordUserId) {
        SpotifyControlSession session = new SpotifyControlSession(
            guildId,
            controllingDiscordUserId,
            Instant.now()
        );
        sessionsByGuildId.put(guildId, session);
        return session;
    }

    public Optional<SpotifyControlSession> getControlSession(long guildId) {
        return Optional.ofNullable(sessionsByGuildId.get(guildId));
    }

    public boolean isControlled(long guildId) {
        return sessionsByGuildId.containsKey(guildId);
    }

    public Optional<SpotifyControlSession> stopControl(long guildId) {
        return Optional.ofNullable(sessionsByGuildId.remove(guildId));
    }
}
