package com.gammatunes.component.spotify.track;

import com.gammatunes.component.spotify.api.request.SpotifyRecommendationsRequest;
import com.gammatunes.component.spotify.api.request.SpotifySavedTracksRequest;
import com.gammatunes.component.spotify.api.response.SpotifyPageOfTracks;
import com.gammatunes.component.spotify.api.response.SpotifyRecommendations;
import com.gammatunes.component.spotify.api.response.SpotifyTrack;
import com.gammatunes.component.spotify.api.response.SpotifyTracks;
import com.gammatunes.component.spotify.auth.SpotifyAccessToken;
import com.gammatunes.component.spotify.client.SpotifyApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@RequiredArgsConstructor
public class SpotifyTrackClient {

    private final SpotifyApiClient spotifyApiClient;



    public Mono<SpotifyTrack> getTrack(String trackId, SpotifyAccessToken token) {
        return spotifyApiClient.get(
            token,
            "/v1/tracks/{trackId}",
            SpotifyTrack.class,
            trackId);
    }

    public Mono<SpotifyTracks> getSeveralTracks(List<String> trackIds, SpotifyAccessToken token) {
        return spotifyApiClient.get(
            token,
            "/v1/tracks?ids={trackIds}",
            SpotifyTracks.class,
            String.join(",", trackIds)
        );
    }

    public Mono<SpotifyPageOfTracks> getUsersSavedTracks(SpotifyAccessToken token, SpotifySavedTracksRequest request) {
        return spotifyApiClient.get(
            token,
            request.path(),
            SpotifyPageOfTracks.class
        );
    }

    public Mono<SpotifyRecommendations> getRecommendations(SpotifyAccessToken token, SpotifyRecommendationsRequest request) {
        return spotifyApiClient.get(
            token,
            request.path(),
            SpotifyRecommendations.class
        );
    }
}
