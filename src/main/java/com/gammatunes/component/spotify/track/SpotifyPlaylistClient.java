package com.gammatunes.component.spotify.track;

import com.gammatunes.component.spotify.api.request.SpotifyPlaylistTracksRequest;
import com.gammatunes.component.spotify.api.response.SpotifyPlaylist;
import com.gammatunes.component.spotify.api.response.SpotifyPlaylistTracksPage;
import com.gammatunes.component.spotify.auth.SpotifyAccessToken;
import com.gammatunes.component.spotify.client.SpotifyApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SpotifyPlaylistClient {

    private final SpotifyApiClient spotifyApiClient;

    public Mono<SpotifyPlaylist> getPlaylist(SpotifyAccessToken token, String playlistId) {
        return spotifyApiClient.get(token, "/v1/playlists/{playlistId}", SpotifyPlaylist.class, playlistId);
    }

    public Mono<SpotifyPlaylistTracksPage> getPlaylistTracks(SpotifyAccessToken token, String playlistId) {
        return spotifyApiClient.get(token, "/v1/playlists/{playlistId}/tracks", SpotifyPlaylistTracksPage.class, playlistId);
    }

    public Mono<SpotifyPlaylistTracksPage> getPlaylistTracks(SpotifyAccessToken token, SpotifyPlaylistTracksRequest request) {
        return spotifyApiClient.get(token, request.path(), SpotifyPlaylistTracksPage.class);
    }
}
