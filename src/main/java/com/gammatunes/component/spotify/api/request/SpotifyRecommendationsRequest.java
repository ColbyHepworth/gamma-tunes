package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public record SpotifyRecommendationsRequest(
    List<String> seedArtists,
    List<String> seedGenres,
    List<String> seedTracks,
    Integer limit
) {
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 100;
    private static final int MAX_SEEDS = 5;

    public SpotifyRecommendationsRequest {
        seedArtists = clean(seedArtists);
        seedGenres = clean(seedGenres);
        seedTracks = clean(seedTracks);

        int seedCount = seedArtists.size() + seedGenres.size() + seedTracks.size();
        if (seedCount == 0) {
            throw new IllegalArgumentException("At least one recommendation seed is required.");
        }
        if (seedCount > MAX_SEEDS) {
            throw new IllegalArgumentException("Spotify allows at most 5 recommendation seeds.");
        }
        if (limit != null && (limit < MIN_LIMIT || limit > MAX_LIMIT)) {
            throw new IllegalArgumentException("Recommendation limit must be between 1 and 100.");
        }
    }

    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/recommendations");

        if (limit != null) {
            uriBuilder.queryParam("limit", limit);
        }
        addSeedParam(uriBuilder, "seed_artists", seedArtists);
        addSeedParam(uriBuilder, "seed_genres", seedGenres);
        addSeedParam(uriBuilder, "seed_tracks", seedTracks);

        return uriBuilder.build().toUriString();
    }

    private static List<String> clean(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(String::trim)
            .toList();
    }

    private static void addSeedParam(UriComponentsBuilder uriBuilder, String name, List<String> seeds) {
        if (!seeds.isEmpty()) {
            uriBuilder.queryParam(name, String.join(",", seeds));
        }
    }
}
