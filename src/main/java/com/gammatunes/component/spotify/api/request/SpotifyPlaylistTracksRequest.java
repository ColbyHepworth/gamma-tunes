package com.gammatunes.component.spotify.api.request;

import org.springframework.web.util.UriComponentsBuilder;

public record SpotifyPlaylistTracksRequest(
    String playlistId,
    Integer limit,
    Integer offset,
    String fields,
    String market
) {
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 50;
    private static final int MIN_OFFSET = 0;

    public SpotifyPlaylistTracksRequest {
        if (playlistId == null || playlistId.isBlank()) {
            throw new IllegalArgumentException("Spotify playlist id is required.");
        }
        playlistId = playlistId.trim();

        if (limit != null && (limit < MIN_LIMIT || limit > MAX_LIMIT)) {
            throw new IllegalArgumentException("Playlist tracks limit must be between 1 and 50.");
        }
        if (offset != null && offset < MIN_OFFSET) {
            throw new IllegalArgumentException("Playlist tracks offset cannot be negative.");
        }
        if (fields != null && fields.isBlank()) {
            fields = null;
        }
        if (market != null && market.isBlank()) {
            market = null;
        }
    }

    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/playlists/{playlistId}/tracks");

        if (limit != null) {
            uriBuilder.queryParam("limit", limit);
        }
        if (offset != null) {
            uriBuilder.queryParam("offset", offset);
        }
        if (fields != null) {
            uriBuilder.queryParam("fields", fields.trim());
        }
        if (market != null) {
            uriBuilder.queryParam("market", market.trim());
        }

        return uriBuilder.buildAndExpand(playlistId).toUriString();
    }
}
