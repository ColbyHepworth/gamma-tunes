package com.gammatunes.component.spotify.track;

import com.gammatunes.component.spotify.api.request.SpotifyPausePlaybackRequest;
import com.gammatunes.component.spotify.api.request.SpotifyPlaybackShuffleRequest;
import com.gammatunes.component.spotify.api.request.SpotifyPlaybackVolumeRequest;
import com.gammatunes.component.spotify.api.request.SpotifyRepeatModeRequest;
import com.gammatunes.component.spotify.api.request.SpotifySeekPlaybackRequest;
import com.gammatunes.component.spotify.api.request.SpotifySkipPlaybackRequest;
import com.gammatunes.component.spotify.api.request.SpotifyStartPlaybackRequest;
import com.gammatunes.component.spotify.api.response.SpotifyCurrentlyPlaying;
import com.gammatunes.component.spotify.api.response.SpotifyPlaybackState;
import com.gammatunes.component.spotify.api.response.SpotifyQueue;
import com.gammatunes.component.spotify.auth.SpotifyAccessToken;
import com.gammatunes.component.spotify.client.SpotifyApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SpotifyPlayerClient {

    private final SpotifyApiClient spotifyApiClient;

    public Mono<SpotifyPlaybackState> getPlaybackState(SpotifyAccessToken token) {
        return spotifyApiClient.get(token, "/v1/me/player", SpotifyPlaybackState.class);
    }

    public Mono<SpotifyCurrentlyPlaying> getCurrentlyPlaying(SpotifyAccessToken token) {
        return spotifyApiClient.get(token, "/v1/me/player/currently-playing", SpotifyCurrentlyPlaying.class);
    }

    public Mono<SpotifyQueue> getQueue(SpotifyAccessToken token) {
        return spotifyApiClient.get(token, "/v1/me/player/queue", SpotifyQueue.class);
    }

    public Mono<Void> startPlayback(SpotifyAccessToken token, SpotifyStartPlaybackRequest request) {
        return spotifyApiClient.put(token, request.path(), request);
    }

    public Mono<Void> pausePlayback(SpotifyAccessToken token, SpotifyPausePlaybackRequest request) {
        return spotifyApiClient.putWithoutBody(token, request.path());
    }

    public Mono<Void> skipToNext(SpotifyAccessToken token, SpotifySkipPlaybackRequest request) {
        return spotifyApiClient.postWithoutBody(token, request.nextPath());
    }

    public Mono<Void> skipToPrevious(SpotifyAccessToken token, SpotifySkipPlaybackRequest request) {
        return spotifyApiClient.postWithoutBody(token, request.previousPath());
    }

    public Mono<Void> seekToPosition(SpotifyAccessToken token, SpotifySeekPlaybackRequest request) {
        return spotifyApiClient.putWithoutBody(token, request.path());
    }

    public Mono<Void> setRepeatMode(SpotifyAccessToken token, SpotifyRepeatModeRequest request) {
        return spotifyApiClient.putWithoutBody(token, request.path());
    }

    public Mono<Void> setPlaybackVolume(SpotifyAccessToken token, SpotifyPlaybackVolumeRequest request) {
        return spotifyApiClient.putWithoutBody(token, request.path());
    }

    public Mono<Void> togglePlaybackShuffle(SpotifyAccessToken token, SpotifyPlaybackShuffleRequest request) {
        return spotifyApiClient.putWithoutBody(token, request.path());
    }
}
