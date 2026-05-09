package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

public record SpotifySavedTracksRequest(
    Integer limit,
    Integer offset,
    String market
) {
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 50;
    private static final int MIN_OFFSET = 0;

    public SpotifySavedTracksRequest {
        if (limit != null && (limit < MIN_LIMIT || limit > MAX_LIMIT)) {
            throw new IllegalArgumentException("Saved tracks limit must be between 1 and 50.");
        }
        if (offset != null && offset < MIN_OFFSET) {
            throw new IllegalArgumentException("Saved tracks offset cannot be negative.");
        }
        if (market != null && market.isBlank()) {
            market = null;
        }
    }

    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/me/tracks");

        if (limit != null) {
            uriBuilder.queryParam("limit", limit);
        }
        if (offset != null) {
            uriBuilder.queryParam("offset", offset);
        }
        if (market != null) {
            uriBuilder.queryParam("market", market.trim());
        }

        return uriBuilder.build().toUriString();
    }
}
