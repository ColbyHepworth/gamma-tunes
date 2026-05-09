package com.gammatunes.component.spotify;

import com.gammatunes.component.spotify.api.response.SpotifyToken;
import com.gammatunes.component.spotify.config.SpotifyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class SpotifyAuthService {

    private final WebClient.Builder webClientBuilder;
    private final SpotifyProperties spotifyProperties;

    public URI buildAuthorizeUri(String state, Collection<String> scopes) {
        return UriComponentsBuilder.fromUriString("https://accounts.spotify.com/authorize")
            .queryParam("client_id", spotifyProperties.getClientId())
            .queryParam("response_type", "code")
            .queryParam("redirect_uri", spotifyProperties.getRedirectUri())
            .queryParam("state", state)
            .queryParam("scope", String.join(" ", scopes))
            .build()
            .toUri();
    }

    public Mono<SpotifyToken> exchangeCode(String code) {
        return accountsClient().post()
            .uri("/api/token")
            .header(HttpHeaders.AUTHORIZATION, basicAuthorizationHeader())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                .with("code", code)
                .with("redirect_uri", spotifyProperties.getRedirectUri().toString()))
            .retrieve()
            .bodyToMono(SpotifyToken.class);
    }

    public Mono<SpotifyToken> refreshAccessToken(String refreshToken) {
        return accountsClient().post()
            .uri("/api/token")
            .header(HttpHeaders.AUTHORIZATION, basicAuthorizationHeader())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                .with("refresh_token", refreshToken))
            .retrieve()
            .bodyToMono(SpotifyToken.class);
    }

    private WebClient accountsClient() {
        return webClientBuilder
            .baseUrl("https://accounts.spotify.com")
            .build();
    }

    private String basicAuthorizationHeader() {
        String rawCredentials = spotifyProperties.getClientId() + ":" + spotifyProperties.getClientSecret();
        String encodedCredentials = Base64.getEncoder()
            .encodeToString(rawCredentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }
}
