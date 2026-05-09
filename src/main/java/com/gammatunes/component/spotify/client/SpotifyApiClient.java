package com.gammatunes.component.spotify.client;

import com.gammatunes.component.spotify.api.response.SpotifyErrorResponse;
import com.gammatunes.component.spotify.auth.SpotifyAccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class SpotifyApiClient {

    private static final String SPOTIFY_API_BASE_URL = "https://api.spotify.com";
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final WebClient webClient;

    public SpotifyApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl(SPOTIFY_API_BASE_URL)
            .build();
    }

    public <T> Mono<T> get(SpotifyAccessToken token, String path, Class<T> responseType, Object... uriVariables) {
        return withSpotifyErrorHandling(webClient.get()
            .uri(path, uriVariables)
            .headers(headers -> headers.setBearerAuth(token.value()))
            .retrieve())
            .bodyToMono(responseType);
    }

    public Mono<Void> put(SpotifyAccessToken token, String path, Object body) {
        return withSpotifyErrorHandling(webClient.put()
            .uri(path)
            .headers(headers -> headers.setBearerAuth(token.value()))
            .bodyValue(body)
            .retrieve())
            .bodyToMono(Void.class);
    }

    public Mono<Void> putWithoutBody(SpotifyAccessToken token, String path) {
        return withSpotifyErrorHandling(webClient.put()
            .uri(path)
            .headers(headers -> headers.setBearerAuth(token.value()))
            .retrieve())
            .bodyToMono(Void.class);
    }

    public Mono<Void> postWithoutBody(SpotifyAccessToken token, String path) {
        return withSpotifyErrorHandling(webClient.post()
            .uri(path)
            .headers(headers -> headers.setBearerAuth(token.value()))
            .retrieve())
            .bodyToMono(Void.class);
    }

    private ResponseSpec withSpotifyErrorHandling(ResponseSpec responseSpec) {
        return responseSpec
            .onStatus(status -> status.value() == 401, response -> spotifyError(response, SpotifyUnauthorizedException::new))
            .onStatus(status -> status.value() == 403, response -> spotifyError(response, SpotifyForbiddenException::new))
            .onStatus(status -> status.value() == 429, this::rateLimitError)
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::apiError);
    }

    private Mono<? extends Throwable> rateLimitError(ClientResponse response) {
        return errorMessage(response)
            .map(message -> new SpotifyRateLimitedException(message, retryAfter(response)));
    }

    private Mono<? extends Throwable> apiError(ClientResponse response) {
        return errorMessage(response).map(message -> new SpotifyApiException(response.statusCode().value(), message));
    }

    private Mono<? extends Throwable> spotifyError(ClientResponse response, java.util.function.Function<String, ? extends SpotifyApiException> exceptionFactory) {
        return errorMessage(response).map(exceptionFactory);
    }

    private Mono<String> errorMessage(ClientResponse response) {
        return response.bodyToMono(SpotifyErrorResponse.class)
            .map(errorResponse -> {
                if (errorResponse.error() == null || errorResponse.error().message() == null) {
                    return "Spotify API request failed with status " + response.statusCode().value();
                }
                return errorResponse.error().message();
            })
            .defaultIfEmpty("Spotify API request failed with status " + response.statusCode().value());
    }

    private Duration retryAfter(ClientResponse response) {
        String retryAfter = response.headers().header(RETRY_AFTER_HEADER).stream()
            .findFirst()
            .orElse(null);

        if (retryAfter == null || retryAfter.isBlank()) {
            return null;
        }

        try {
            return Duration.ofSeconds(Long.parseLong(retryAfter));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
