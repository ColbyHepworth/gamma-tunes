package com.gammatunes.service;

import com.gammatunes.component.spotify.api.response.SpotifyCurrentlyPlaying;
import com.gammatunes.component.spotify.api.response.SpotifyDevice;
import com.gammatunes.component.spotify.api.response.SpotifyPlaybackState;
import com.gammatunes.component.spotify.api.response.SpotifyTrack;
import com.gammatunes.component.spotify.control.SpotifyControlPlaybackStateStore;
import com.gammatunes.component.spotify.control.SpotifyControlSession;
import com.gammatunes.component.spotify.resolver.SpotifyTrackResolverService;
import com.gammatunes.model.dto.RequesterInfo;
import com.gammatunes.service.playback.PlaybackMode;
import com.gammatunes.service.playback.PlaybackRequestFactory;
import com.gammatunes.service.playback.PlaybackService;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotifyControlStartService {

    private static final Duration SPOTIFY_START_SETTLE_DELAY = Duration.ofMillis(750);
    private static final Duration SPOTIFY_READY_POLL_INTERVAL = Duration.ofSeconds(2);
    private static final Duration SPOTIFY_READY_TIMEOUT = Duration.ofMinutes(1);

    private final SpotifyControlService spotifyControlService;
    private final SpotifyControlAudioGuard spotifyControlAudioGuard;
    private final SpotifyPlayerService spotifyPlayerService;
    private final SpotifyTrackResolverService spotifyTrackResolverService;
    private final SpotifyControlPlaybackStateStore spotifyControlPlaybackStateStore;
    private final PlaybackRequestFactory playbackRequestFactory;
    private final PlaybackService playbackService;
    private final JDA jda;

    public Mono<Void> startControlAndPlay(
        long guildId,
        long discordUserId,
        long voiceChannelId,
        Long textChannelId
    ) {
        return spotifyControlService.startControl(guildId, discordUserId, voiceChannelId, textChannelId)
            .flatMap(session -> spotifyControlAudioGuard.mute(session)
                .then(playCurrentSpotifyTrack(session))
                .onErrorResume(error -> spotifyControlService.cancelControlStart(session)
                    .onErrorResume(cancelError -> Mono.empty())
                    .then(Mono.error(error))));
    }

    public Mono<Void> startControlAndPlayWhenReady(
        long guildId,
        long discordUserId,
        long voiceChannelId,
        Long textChannelId
    ) {
        return waitForSpotifyReady(discordUserId)
            .then(startControlAndPlay(guildId, discordUserId, voiceChannelId, textChannelId));
    }

    private Mono<SpotifyPlaybackState> waitForSpotifyReady(long discordUserId) {
        return Flux.interval(Duration.ZERO, SPOTIFY_READY_POLL_INTERVAL)
            .concatMap(ignored -> spotifyPlayerService.getPlaybackState(discordUserId)
                .filter(this::isReadyForControlStart))
            .next()
            .timeout(SPOTIFY_READY_TIMEOUT, Mono.error(new IllegalStateException(
                "I could not find active Spotify playback after 60 seconds. Start a Spotify track on a volume-controllable device and run `/spotify control start` again."
            )));
    }

    private boolean isReadyForControlStart(SpotifyPlaybackState playbackState) {
        return hasMuteableDevice(playbackState.device());
    }

    private boolean hasMuteableDevice(SpotifyDevice device) {
        return device != null
            && device.id().isPresent()
            && device.supportsVolume()
            && device.volumePercent().isPresent();
    }

    private Mono<Void> playCurrentSpotifyTrack(SpotifyControlSession session) {
        return spotifyPlayerService.getCurrentlyPlaying(session.controllingDiscordUserId())
            .switchIfEmpty(resumeAndGetCurrentlyPlaying(session))
            .flatMap(currentlyPlaying -> currentlyPlaying.isPlaying()
                ? playCurrentlyPlayingTrack(session, currentlyPlaying)
                : resumeSpotifyForControlStart(session)
                    .then(Mono.delay(SPOTIFY_START_SETTLE_DELAY))
                    .then(Mono.defer(() -> playCurrentlyPlayingTrack(
                        spotifyControlService.markResumedByControlStart(session),
                        currentlyPlaying
                    ))));
    }

    private Mono<SpotifyCurrentlyPlaying> resumeAndGetCurrentlyPlaying(SpotifyControlSession session) {
        return resumeSpotifyForControlStart(session)
            .then(Mono.delay(SPOTIFY_START_SETTLE_DELAY))
            .then(spotifyPlayerService.getCurrentlyPlaying(session.controllingDiscordUserId()))
            .switchIfEmpty(Mono.error(new IllegalStateException("No Spotify track is available to sync.")))
            .map(currentlyPlaying -> {
                spotifyControlService.markResumedByControlStart(session);
                return currentlyPlaying;
            });
    }

    private Mono<Void> resumeSpotifyForControlStart(SpotifyControlSession session) {
        return spotifyControlAudioGuard.resume(session);
    }

    private Mono<Void> playCurrentlyPlayingTrack(SpotifyControlSession session, SpotifyCurrentlyPlaying currentlyPlaying) {
        if (currentlyPlaying.item().isEmpty()) {
            return Mono.error(new IllegalStateException("No Spotify track is available to sync."));
        }

        SpotifyTrack spotifyTrack = currentlyPlaying.item().get();
        return spotifyTrackResolverService.resolveSpotifyTrack(spotifyTrack)
            .flatMap(track -> playSpotifyTrack(session, track))
            .doOnSuccess(ignored -> spotifyControlPlaybackStateStore.save(session.guildId(), spotifyTrack.id(), true));
    }

    private Mono<Void> playSpotifyTrack(SpotifyControlSession session, Track track) {
        return playbackService.play(playbackRequestFactory.create(
            session.guildId(),
            session.voiceChannelId(),
            textChannel(session),
            requesterInfo(session),
            List.of(track),
            PlaybackMode.PLAY_NOW
        ));
    }

    private RequesterInfo requesterInfo(SpotifyControlSession session) {
        return new RequesterInfo(
            String.valueOf(session.controllingDiscordUserId()),
            "Spotify Control",
            null
        );
    }

    private TextChannel textChannel(SpotifyControlSession session) {
        if (session.textChannelId() == null) {
            return null;
        }

        return jda.getTextChannelById(session.textChannelId());
    }
}
