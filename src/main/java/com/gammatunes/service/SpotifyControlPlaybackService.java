package com.gammatunes.service;

import com.gammatunes.component.spotify.api.response.SpotifyTrack;
import com.gammatunes.component.spotify.control.SpotifyControlPlaybackState;
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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SpotifyControlPlaybackService {

    private static final long SPOTIFY_SEEK_DRIFT_THRESHOLD_MS = 2_500L;

    private final SpotifyControlService spotifyControlService;
    private final SpotifyPlayerService spotifyPlayerService;
    private final SpotifyTrackResolverService spotifyTrackResolverService;
    private final SpotifyControlPlaybackStateStore spotifyControlPlaybackStateStore;
    private final PlaybackRequestFactory playbackRequestFactory;
    private final PlaybackService playbackService;
    private final JDA jda;
    private final Set<Long> syncingGuildIds = ConcurrentHashMap.newKeySet();

    public Mono<Void> syncNow(long guildId) {
        return Mono.defer(() -> {
            if (!syncingGuildIds.add(guildId)) {
                return Mono.empty();
            }

            return syncNowInternal(guildId)
                .doFinally(signalType -> syncingGuildIds.remove(guildId));
        });
    }

    public Mono<Void> syncControlledGuilds() {
        return Flux.fromIterable(spotifyControlService.getControlSessions())
            .flatMap(session -> syncNow(session.guildId())
                .onErrorResume(error -> Mono.empty()), 4)
            .then();
    }

    private Mono<Void> syncNowInternal(long guildId) {
        SpotifyControlSession session = spotifyControlService.getControlSession(guildId).orElse(null);
        if (session == null) {
            return Mono.empty();
        }

        return syncSession(session);
    }

    private Mono<Void> syncSession(SpotifyControlSession session) {
        return spotifyPlayerService.getCurrentlyPlaying(session.controllingDiscordUserId())
            .flatMap(currentlyPlaying -> {
                long guildId = session.guildId();
                if (!spotifyControlService.isControlled(guildId)) {
                    return Mono.empty();
                }

                if (currentlyPlaying.item().isEmpty()) {
                    return Mono.empty();
                }

                SpotifyTrack spotifyTrack = currentlyPlaying.item().get();
                boolean alreadySynced = isAlreadySynced(guildId, spotifyTrack.id());
                boolean wasPlaying = wasPlaying(guildId);

                if (!currentlyPlaying.isPlaying()) {
                    if (alreadySynced && wasPlaying) {
                        spotifyControlPlaybackStateStore.save(guildId, spotifyTrack.id(), false);
                        return playbackService.pause(guildId);
                    }
                    return Mono.empty();
                }

                if (alreadySynced) {
                    if (!wasPlaying) {
                        spotifyControlPlaybackStateStore.save(guildId, spotifyTrack.id(), true);
                        return playbackService.resume(guildId)
                            .then(seekIfDrifted(guildId, currentlyPlaying.progressMs().orElse(null)));
                    }
                    return seekIfDrifted(guildId, currentlyPlaying.progressMs().orElse(null));
                }

                return spotifyTrackResolverService.resolveSpotifyTrack(spotifyTrack)
                    .flatMap(track -> playSpotifyTrack(session, track))
                    .then(seekToSpotifyProgress(guildId, currentlyPlaying.progressMs().orElse(null)))
                    .doOnSuccess(ignored -> spotifyControlPlaybackStateStore.save(guildId, spotifyTrack.id(), true));
            });
    }

    private Mono<Void> seekToSpotifyProgress(long guildId, Integer spotifyProgressMs) {
        if (spotifyProgressMs == null || spotifyProgressMs <= 0) {
            return Mono.empty();
        }

        return playbackService.seek(guildId, spotifyProgressMs);
    }

    private Mono<Void> seekIfDrifted(long guildId, Integer spotifyProgressMs) {
        if (spotifyProgressMs == null) {
            return Mono.empty();
        }

        return playbackService.getPositionMs(guildId)
            .flatMap(discordPositionMs -> {
                long driftMs = Math.abs(discordPositionMs - spotifyProgressMs);
                if (driftMs <= SPOTIFY_SEEK_DRIFT_THRESHOLD_MS) {
                    return Mono.empty();
                }

                return playbackService.seek(guildId, spotifyProgressMs);
            });
    }

    private boolean isAlreadySynced(long guildId, String spotifyTrackId) {
        return spotifyControlPlaybackStateStore.get(guildId)
            .map(state -> Objects.equals(state.lastSpotifyTrackId(), spotifyTrackId))
            .orElse(false);
    }

    private boolean wasPlaying(long guildId) {
        return spotifyControlPlaybackStateStore.get(guildId)
            .map(SpotifyControlPlaybackState::lastPlayingState)
            .orElse(true);
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
