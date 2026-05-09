package com.gammatunes.service;

import com.gammatunes.component.spotify.control.SpotifyControlSession;
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

    public Mono<SpotifyControlSession> startControl(long guildId, long discordUserId) {
        return Mono.defer(() -> {
            if (spotifyAccountLinkService.getLinkedAccount(discordUserId).isEmpty()) {
                return Mono.error(new IllegalArgumentException("No Spotify account is linked for this Discord user."));
            }

            return Mono.just(spotifyControlSessionStore.startControl(guildId, discordUserId));
        });
    }

    public Mono<SpotifyControlSession> stopControl(long guildId) {
        return Mono.defer(() -> Mono.justOrEmpty(spotifyControlSessionStore.stopControl(guildId)));
    }

    public Optional<SpotifyControlSession> getControlSession(long guildId) {
        return spotifyControlSessionStore.getControlSession(guildId);
    }

    public boolean isControlled(long guildId) {
        return spotifyControlSessionStore.isControlled(guildId);
    }
}
