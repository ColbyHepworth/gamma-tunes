package com.gammatunes.component.spotify.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SpotifyControlSessionStore {

    private static final Path SESSION_STORE_PATH = Path.of(".local", "spotify-control-sessions.json");
    private static final TypeReference<Map<Long, SpotifyControlSession>> STORE_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, SpotifyControlSession> sessionsByGuildId = new ConcurrentHashMap<>();

    public SpotifyControlSessionStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.sessionsByGuildId.putAll(loadSessions());
    }

    public synchronized SpotifyControlSession startControl(
        long guildId,
        long controllingDiscordUserId,
        long voiceChannelId,
        Long textChannelId,
        String spotifyDeviceId,
        Integer originalVolume,
        boolean originallyPlaying
    ) {
        SpotifyControlSession session = new SpotifyControlSession(
            guildId,
            controllingDiscordUserId,
            voiceChannelId,
            textChannelId,
            Instant.now(),
            spotifyDeviceId,
            originalVolume,
            originallyPlaying,
            false
        );
        sessionsByGuildId.put(guildId, session);
        writeSessions();
        return session;
    }

    public synchronized SpotifyControlSession markResumedByControlStart(SpotifyControlSession session) {
        SpotifyControlSession current = sessionsByGuildId.get(session.guildId());
        if (!session.equals(current)) {
            return session;
        }

        SpotifyControlSession updated = new SpotifyControlSession(
            session.guildId(),
            session.controllingDiscordUserId(),
            session.voiceChannelId(),
            session.textChannelId(),
            session.startedAt(),
            session.spotifyDeviceId(),
            session.originalVolume(),
            session.originallyPlaying(),
            true
        );
        sessionsByGuildId.put(updated.guildId(), updated);
        writeSessions();
        return updated;
    }

    public Optional<SpotifyControlSession> getControlSession(long guildId) {
        return Optional.ofNullable(sessionsByGuildId.get(guildId));
    }

    public boolean isControlled(long guildId) {
        return sessionsByGuildId.containsKey(guildId);
    }

    public Collection<SpotifyControlSession> getControlSessions() {
        return List.copyOf(sessionsByGuildId.values());
    }

    public synchronized void clearAll() {
        sessionsByGuildId.clear();
        writeSessions();
    }

    public synchronized void clear(SpotifyControlSession session) {
        if (sessionsByGuildId.remove(session.guildId(), session)) {
            writeSessions();
        }
    }

    private Map<Long, SpotifyControlSession> loadSessions() {
        if (!Files.exists(SESSION_STORE_PATH)) {
            return new HashMap<>();
        }

        try {
            return new HashMap<>(objectMapper.readValue(SESSION_STORE_PATH.toFile(), STORE_TYPE));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read Spotify control session store: " + SESSION_STORE_PATH, exception);
        }
    }

    private void writeSessions() {
        try {
            Files.createDirectories(SESSION_STORE_PATH.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(SESSION_STORE_PATH.toFile(), sessionsByGuildId);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write Spotify control session store: " + SESSION_STORE_PATH, exception);
        }
        log.debug("Saved {} Spotify control sessions to {}", sessionsByGuildId.size(), SESSION_STORE_PATH);
    }
}
