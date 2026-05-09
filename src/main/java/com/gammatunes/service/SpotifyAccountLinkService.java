package com.gammatunes.service;

import com.gammatunes.component.spotify.SpotifyAuthService;
import com.gammatunes.component.spotify.api.response.SpotifyToken;
import com.gammatunes.component.spotify.auth.LinkedSpotifyAccount;
import com.gammatunes.component.spotify.auth.SpotifyAccessToken;
import com.gammatunes.component.spotify.auth.SpotifyTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SpotifyAccountLinkService {

    private static final Duration PENDING_AUTH_TTL = Duration.ofMinutes(10);
    private static final Duration ACCESS_TOKEN_REFRESH_BUFFER = Duration.ofSeconds(60);
    private static final List<String> DEFAULT_SCOPES = List.of(
        "user-read-currently-playing",
        "user-read-playback-state",
        "user-modify-playback-state",
        "playlist-read-private",
        "playlist-read-collaborative"
    );

    private final SpotifyAuthService spotifyAuthService;
    private final SpotifyTokenStore spotifyTokenStore;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, PendingSpotifyAuth> pendingAuthByState = new ConcurrentHashMap<>();

    public URI createAuthorizationUri(long discordUserId, long guildId) {
        String state = generateState();
        pendingAuthByState.put(state, new PendingSpotifyAuth(discordUserId, guildId, Instant.now()));
        return spotifyAuthService.buildAuthorizeUri(state, DEFAULT_SCOPES);
    }

    public Mono<LinkedSpotifyAccount> completeLink(String code, String state) {
        PendingSpotifyAuth pending = pendingAuthByState.remove(state);
        if (pending == null || pending.isExpired()) {
            return Mono.error(new IllegalArgumentException("Spotify authorization expired or was not started by this app."));
        }

        return spotifyAuthService.exchangeCode(code)
            .map(tokens -> toLinkedAccount(pending, tokens))
            .doOnNext(spotifyTokenStore::save);
    }

    public Mono<LinkedSpotifyAccount> refreshTokens(long discordUserId) {
        LinkedSpotifyAccount existing = spotifyTokenStore.findByDiscordUserId(discordUserId).orElse(null);
        if (existing == null) {
            return Mono.error(new IllegalArgumentException("No Spotify account is linked for this Discord user."));
        }

        return spotifyAuthService.refreshAccessToken(existing.refreshToken())
            .map(tokens -> refreshedAccount(existing, tokens))
            .doOnNext(spotifyTokenStore::save);
    }

    public Optional<LinkedSpotifyAccount> getLinkedAccount(long discordUserId) {
        return spotifyTokenStore.findByDiscordUserId(discordUserId);
    }

    public Mono<SpotifyAccessToken> getValidAccessToken(long discordUserId) {
        LinkedSpotifyAccount existing = spotifyTokenStore.findByDiscordUserId(discordUserId).orElse(null);
        if (existing == null) {
            return Mono.error(new IllegalArgumentException("No Spotify account is linked for this Discord user."));
        }

        if (!isExpiredOrExpiringSoon(existing)) {
            return Mono.just(accessToken(existing));
        }

        return refreshTokens(discordUserId)
            .map(this::accessToken);
    }

    private LinkedSpotifyAccount toLinkedAccount(PendingSpotifyAuth pending, SpotifyToken tokens) {
        return new LinkedSpotifyAccount(
            pending.discordUserId(),
            pending.guildId(),
            tokens.accessToken(),
            tokens.refreshToken(),
            Instant.now().plusSeconds(tokens.expiresIn()),
            tokens.scope(),
            tokens.tokenType()
        );
    }

    private LinkedSpotifyAccount refreshedAccount(LinkedSpotifyAccount existing, SpotifyToken tokens) {
        String refreshToken = tokens.refreshToken() == null || tokens.refreshToken().isBlank()
            ? existing.refreshToken()
            : tokens.refreshToken();

        return new LinkedSpotifyAccount(
            existing.discordUserId(),
            existing.guildId(),
            tokens.accessToken(),
            refreshToken,
            Instant.now().plusSeconds(tokens.expiresIn()),
            tokens.scope(),
            tokens.tokenType()
        );
    }

    private boolean isExpiredOrExpiringSoon(LinkedSpotifyAccount account) {
        return account.expiresAt().isBefore(Instant.now().plus(ACCESS_TOKEN_REFRESH_BUFFER));
    }

    private SpotifyAccessToken accessToken(LinkedSpotifyAccount account) {
        return new SpotifyAccessToken(account.accessToken());
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record PendingSpotifyAuth(
        long discordUserId,
        long guildId,
        Instant createdAt
    ) {
        boolean isExpired() {
            return createdAt.plus(PENDING_AUTH_TTL).isBefore(Instant.now());
        }
    }

}
