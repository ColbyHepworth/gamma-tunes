package com.gammatunes.service;

import com.gammatunes.component.spotify.control.SpotifyControlAudioSnapshot;
import com.gammatunes.component.spotify.control.SpotifyControlPlaybackStateStore;
import com.gammatunes.component.spotify.control.SpotifyControlSession;
import com.gammatunes.component.spotify.control.SpotifyControlSessionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyControlService {

    private final SpotifyAccountLinkService spotifyAccountLinkService;
    private final SpotifyControlAudioGuard spotifyControlAudioGuard;
    private final SpotifyControlSessionStore spotifyControlSessionStore;
    private final SpotifyControlPlaybackStateStore spotifyControlPlaybackStateStore;

    public Mono<SpotifyControlSession> startControl(
        long guildId,
        long discordUserId,
        long voiceChannelId,
        Long textChannelId
    ) {
        if (spotifyAccountLinkService.getLinkedAccount(discordUserId).isEmpty()) {
            return Mono.error(new IllegalArgumentException("No Spotify account is linked for this Discord user."));
        }

        return restorePreviousSession(guildId)
            .then(spotifyControlAudioGuard.capture(discordUserId))
            .map(snapshot -> startSession(guildId, discordUserId, voiceChannelId, textChannelId, snapshot));
    }

    public Mono<SpotifyControlSession> stopControl(long guildId) {
        return Mono.defer(() -> {
            spotifyControlPlaybackStateStore.clear(guildId);
            Optional<SpotifyControlSession> session = spotifyControlSessionStore.getControlSession(guildId);

            if (session.isEmpty()) {
                return Mono.empty();
            }

            SpotifyControlSession stoppedSession = session.get();
            return spotifyControlAudioGuard.release(stoppedSession)
                .then(Mono.fromRunnable(() -> spotifyControlSessionStore.clear(stoppedSession)))
                .thenReturn(stoppedSession);
        });
    }

    public Mono<Void> cancelControlStart(SpotifyControlSession session) {
        return Mono.defer(() -> {
            SpotifyControlSession currentSession = spotifyControlSessionStore.getControlSession(session.guildId())
                .orElse(session);

            spotifyControlPlaybackStateStore.clear(currentSession.guildId());
            return spotifyControlAudioGuard.pauseIfResumedByControlStart(currentSession)
                .then(spotifyControlAudioGuard.restoreVolume(currentSession))
                .then(Mono.fromRunnable(() -> spotifyControlSessionStore.clear(currentSession)));
        });
    }

    @EventListener(ApplicationReadyEvent.class)
    public void releaseStaleControlSessions() {
        Collection<SpotifyControlSession> staleSessions = spotifyControlSessionStore.getControlSessions();
        if (staleSessions.isEmpty()) {
            return;
        }

        log.info("Releasing {} stale Spotify control session(s)", staleSessions.size());
        Flux.fromIterable(staleSessions)
            .flatMap(session -> spotifyControlAudioGuard.release(session)
                .then(Mono.fromRunnable(() -> spotifyControlSessionStore.clear(session))), 2)
            .doOnError(error -> log.warn("Could not release stale Spotify control sessions", error))
            .onErrorComplete()
            .subscribe();
    }

    private SpotifyControlSession startSession(
        long guildId,
        long discordUserId,
        long voiceChannelId,
        Long textChannelId,
        SpotifyControlAudioSnapshot audioSnapshot
    ) {
        spotifyControlPlaybackStateStore.clear(guildId);
        return spotifyControlSessionStore.startControl(
            guildId,
            discordUserId,
            voiceChannelId,
            textChannelId,
            audioSnapshot.spotifyDeviceId(),
            audioSnapshot.originalVolume(),
            audioSnapshot.originallyPlaying()
        );
    }

    public SpotifyControlSession markResumedByControlStart(SpotifyControlSession session) {
        return spotifyControlSessionStore.markResumedByControlStart(session);
    }

    private Mono<Void> restorePreviousSession(long guildId) {
        return Mono.defer(() -> {
            SpotifyControlSession previous = spotifyControlSessionStore.getControlSession(guildId).orElse(null);
            if (previous == null) {
                return Mono.empty();
            }
            return spotifyControlAudioGuard.release(previous);
        });
    }

    public Optional<SpotifyControlSession> getControlSession(long guildId) {
        return spotifyControlSessionStore.getControlSession(guildId);
    }

    public boolean isControlled(long guildId) {
        return spotifyControlSessionStore.isControlled(guildId);
    }

    public Collection<SpotifyControlSession> getControlSessions() {
        return spotifyControlSessionStore.getControlSessions();
    }
}
