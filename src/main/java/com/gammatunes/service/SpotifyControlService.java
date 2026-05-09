package com.gammatunes.service;

import com.gammatunes.component.spotify.control.SpotifyControlSession;
import com.gammatunes.component.spotify.control.SpotifyControlPlaybackStateStore;
import com.gammatunes.component.spotify.control.SpotifyControlSessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpotifyControlService {

    private final SpotifyAccountLinkService spotifyAccountLinkService;
    private final SpotifyControlSessionStore spotifyControlSessionStore;
    private final SpotifyControlPlaybackStateStore spotifyControlPlaybackStateStore;

    public Mono<SpotifyControlSession> startControl(
        long guildId,
        long discordUserId,
        long voiceChannelId,
        Long textChannelId
    ) {
        return Mono.defer(() -> {
            if (spotifyAccountLinkService.getLinkedAccount(discordUserId).isEmpty()) {
                return Mono.error(new IllegalArgumentException("No Spotify account is linked for this Discord user."));
            }

            spotifyControlPlaybackStateStore.clear(guildId);
            return Mono.just(spotifyControlSessionStore.startControl(
                guildId,
                discordUserId,
                voiceChannelId,
                textChannelId
            ));
        });
    }

    public Mono<SpotifyControlSession> stopControl(long guildId) {
        return Mono.defer(() -> {
            spotifyControlPlaybackStateStore.clear(guildId);
            return Mono.justOrEmpty(spotifyControlSessionStore.stopControl(guildId));
        });
    }

    public Optional<SpotifyControlSession> getControlSession(long guildId) {
        return spotifyControlSessionStore.getControlSession(guildId);
    }

    public boolean isControlled(long guildId) {
        return spotifyControlSessionStore.isControlled(guildId);
    }
}
