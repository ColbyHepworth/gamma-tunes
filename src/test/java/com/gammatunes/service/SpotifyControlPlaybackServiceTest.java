package com.gammatunes.service;

import com.gammatunes.component.spotify.api.response.SpotifyCurrentlyPlaying;
import com.gammatunes.component.spotify.api.response.SpotifyTrack;
import com.gammatunes.component.spotify.control.SpotifyControlPlaybackStateStore;
import com.gammatunes.component.spotify.control.SpotifyControlSession;
import com.gammatunes.component.spotify.resolver.SpotifyTrackResolverService;
import com.gammatunes.service.playback.PlaybackRequestFactory;
import com.gammatunes.service.playback.PlaybackService;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpotifyControlPlaybackServiceTest {

    private static final long GUILD_ID = 123L;
    private static final long DISCORD_USER_ID = 456L;
    private static final String SPOTIFY_TRACK_ID = "spotify-track-id";

    private final SpotifyControlService spotifyControlService = mock(SpotifyControlService.class);
    private final SpotifyPlayerService spotifyPlayerService = mock(SpotifyPlayerService.class);
    private final SpotifyTrackResolverService spotifyTrackResolverService = mock(SpotifyTrackResolverService.class);
    private final SpotifyControlPlaybackStateStore playbackStateStore = new SpotifyControlPlaybackStateStore();
    private final PlaybackRequestFactory playbackRequestFactory = mock(PlaybackRequestFactory.class);
    private final PlaybackService playbackService = mock(PlaybackService.class);
    private final JDA jda = mock(JDA.class);

    private final SpotifyControlPlaybackService service = new SpotifyControlPlaybackService(
        spotifyControlService,
        spotifyPlayerService,
        spotifyTrackResolverService,
        playbackStateStore,
        playbackRequestFactory,
        playbackService,
        jda
    );

    @Test
    void syncNowSeeksWhenSpotifyProgressDriftsBeyondThreshold() {
        SpotifyControlSession session = session();
        playbackStateStore.save(GUILD_ID, SPOTIFY_TRACK_ID, true);

        when(spotifyControlService.getControlSession(GUILD_ID)).thenReturn(Optional.of(session));
        when(spotifyControlService.isControlled(GUILD_ID)).thenReturn(true);
        when(spotifyPlayerService.getCurrentlyPlaying(DISCORD_USER_ID)).thenReturn(Mono.just(currentlyPlaying(10_000)));
        when(playbackService.getPositionMs(GUILD_ID)).thenReturn(Mono.just(6_000L));
        when(playbackService.seek(GUILD_ID, 10_000L)).thenReturn(Mono.empty());

        StepVerifier.create(service.syncNow(GUILD_ID))
            .verifyComplete();

        verify(playbackService).seek(GUILD_ID, 10_000L);
    }

    @Test
    void syncNowDoesNotSeekWhenSpotifyProgressIsWithinThreshold() {
        SpotifyControlSession session = session();
        playbackStateStore.save(GUILD_ID, SPOTIFY_TRACK_ID, true);

        when(spotifyControlService.getControlSession(GUILD_ID)).thenReturn(Optional.of(session));
        when(spotifyControlService.isControlled(GUILD_ID)).thenReturn(true);
        when(spotifyPlayerService.getCurrentlyPlaying(DISCORD_USER_ID)).thenReturn(Mono.just(currentlyPlaying(10_000)));
        when(playbackService.getPositionMs(GUILD_ID)).thenReturn(Mono.just(8_000L));

        StepVerifier.create(service.syncNow(GUILD_ID))
            .verifyComplete();

        verify(playbackService, never()).seek(GUILD_ID, 10_000L);
    }

    @Test
    void syncNowDoesNotSeekWhenSpotifyProgressIsMissing() {
        SpotifyControlSession session = session();
        playbackStateStore.save(GUILD_ID, SPOTIFY_TRACK_ID, true);

        when(spotifyControlService.getControlSession(GUILD_ID)).thenReturn(Optional.of(session));
        when(spotifyControlService.isControlled(GUILD_ID)).thenReturn(true);
        when(spotifyPlayerService.getCurrentlyPlaying(DISCORD_USER_ID)).thenReturn(Mono.just(currentlyPlaying(null)));

        StepVerifier.create(service.syncNow(GUILD_ID))
            .verifyComplete();

        verify(playbackService, never()).getPositionMs(GUILD_ID);
        verify(playbackService, never()).seek(GUILD_ID, 10_000L);
    }

    private SpotifyControlSession session() {
        return new SpotifyControlSession(
            GUILD_ID,
            DISCORD_USER_ID,
            789L,
            null,
            Instant.now(),
            "spotify-device-id",
            50,
            true,
            false
        );
    }

    private SpotifyCurrentlyPlaying currentlyPlaying(Integer progressMs) {
        return new SpotifyCurrentlyPlaying(
            null,
            null,
            false,
            Optional.empty(),
            0,
            Optional.ofNullable(progressMs),
            true,
            Optional.of(spotifyTrack()),
            "track",
            null
        );
    }

    private SpotifyTrack spotifyTrack() {
        return new SpotifyTrack(
            Optional.empty(),
            List.of(),
            List.of(),
            1,
            180_000,
            false,
            Optional.empty(),
            null,
            "https://api.spotify.com/v1/tracks/" + SPOTIFY_TRACK_ID,
            SPOTIFY_TRACK_ID,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            "Test Track",
            Optional.empty(),
            Optional.empty(),
            1,
            "track",
            "spotify:track:" + SPOTIFY_TRACK_ID,
            false
        );
    }
}
