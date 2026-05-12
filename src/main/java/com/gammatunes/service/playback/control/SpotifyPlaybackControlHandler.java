package com.gammatunes.service.playback.control;

import com.gammatunes.component.spotify.api.request.SpotifyPausePlaybackRequest;
import com.gammatunes.component.spotify.api.request.SpotifySkipPlaybackRequest;
import com.gammatunes.component.spotify.api.request.SpotifyStartPlaybackRequest;
import com.gammatunes.component.spotify.control.SpotifyControlSession;
import com.gammatunes.service.SpotifyControlPlaybackService;
import com.gammatunes.service.SpotifyControlService;
import com.gammatunes.service.SpotifyPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SpotifyPlaybackControlHandler implements PlaybackControlHandler {

    private static final Duration SPOTIFY_SETTLE_DELAY = Duration.ofMillis(750);

    private final SpotifyControlService spotifyControlService;
    private final SpotifyPlayerService spotifyPlayerService;
    private final SpotifyControlPlaybackService spotifyControlPlaybackService;

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean supports(PlaybackControlAction action) {
        return action == PlaybackControlAction.SKIP
            || action == PlaybackControlAction.PREVIOUS
            || action == PlaybackControlAction.PAUSE
            || action == PlaybackControlAction.RESUME
            || action == PlaybackControlAction.STOP;
    }

    @Override
    public Mono<PlaybackControlResult> handle(long guildId, PlaybackControlAction action) {
        SpotifyControlSession session = spotifyControlService.getControlSession(guildId).orElse(null);
        if (session == null) {
            return Mono.just(PlaybackControlResult.NOT_HANDLED);
        }

        return switch (action) {
            case SKIP -> spotifyPlayerService
                .skipToNext(session.controllingDiscordUserId(), new SpotifySkipPlaybackRequest(session.spotifyDeviceId()))
                .then(syncAfterSpotifyChange(guildId))
                .thenReturn(PlaybackControlResult.HANDLED);
            case PREVIOUS -> spotifyPlayerService
                .skipToPrevious(session.controllingDiscordUserId(), new SpotifySkipPlaybackRequest(session.spotifyDeviceId()))
                .then(syncAfterSpotifyChange(guildId))
                .thenReturn(PlaybackControlResult.HANDLED);
            case PAUSE -> spotifyPlayerService
                .pausePlayback(session.controllingDiscordUserId(), new SpotifyPausePlaybackRequest(session.spotifyDeviceId()))
                .thenReturn(PlaybackControlResult.HANDLED_CONTINUE);
            case RESUME -> spotifyPlayerService
                .startPlayback(session.controllingDiscordUserId(), new SpotifyStartPlaybackRequest(session.spotifyDeviceId(), null, null, null, null))
                .then(syncAfterSpotifyChange(guildId).onErrorResume(error -> Mono.empty()))
                .thenReturn(PlaybackControlResult.HANDLED_CONTINUE);
            case STOP -> spotifyControlService.stopControl(guildId)
                .thenReturn(PlaybackControlResult.HANDLED_CONTINUE);
            default -> Mono.just(PlaybackControlResult.NOT_HANDLED);
        };
    }

    private Mono<Void> syncAfterSpotifyChange(long guildId) {
        return Mono.delay(SPOTIFY_SETTLE_DELAY)
            .then(spotifyControlPlaybackService.syncNow(guildId));
    }
}
