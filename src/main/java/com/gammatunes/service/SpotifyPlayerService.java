package com.gammatunes.service;

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
import com.gammatunes.component.spotify.resolver.SpotifyTrackResolverService;
import com.gammatunes.component.spotify.track.SpotifyPlayerClient;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SpotifyPlayerService {

    private final SpotifyAccountLinkService spotifyAccountLinkService;
    private final SpotifyPlayerClient spotifyPlayerClient;
    private final SpotifyTrackResolverService spotifyTrackResolverService;

    public Mono<SpotifyPlaybackState> getPlaybackState(long discordUserId) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(spotifyPlayerClient::getPlaybackState);
    }

    public Mono<SpotifyCurrentlyPlaying> getCurrentlyPlaying(long discordUserId) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(spotifyPlayerClient::getCurrentlyPlaying);
    }

    public Mono<SpotifyQueue> getQueue(long discordUserId) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(spotifyPlayerClient::getQueue);
    }

    public Mono<Track> resolveCurrentlyPlaying(long discordUserId) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(spotifyPlayerClient::getCurrentlyPlaying)
            .flatMap(currentlyPlaying -> currentlyPlaying.item()
                .map(spotifyTrackResolverService::resolveSpotifyTrack)
                .orElseGet(() -> Mono.error(new IllegalArgumentException("No Spotify track is currently playing."))));
    }

    public Mono<Void> startPlayback(long discordUserId, SpotifyStartPlaybackRequest request) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlayerClient.startPlayback(token, request));
    }

    public Mono<Void> pausePlayback(long discordUserId, SpotifyPausePlaybackRequest request) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlayerClient.pausePlayback(token, request));
    }

    public Mono<Void> skipToNext(long discordUserId, SpotifySkipPlaybackRequest request) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlayerClient.skipToNext(token, request));
    }

    public Mono<Void> skipToPrevious(long discordUserId, SpotifySkipPlaybackRequest request) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlayerClient.skipToPrevious(token, request));
    }

    public Mono<Void> seekToPosition(long discordUserId, SpotifySeekPlaybackRequest request) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlayerClient.seekToPosition(token, request));
    }

    public Mono<Void> setRepeatMode(long discordUserId, SpotifyRepeatModeRequest request) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlayerClient.setRepeatMode(token, request));
    }

    public Mono<Void> setPlaybackVolume(long discordUserId, SpotifyPlaybackVolumeRequest request) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlayerClient.setPlaybackVolume(token, request));
    }

    public Mono<Void> togglePlaybackShuffle(long discordUserId, SpotifyPlaybackShuffleRequest request) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlayerClient.togglePlaybackShuffle(token, request));
    }
}
