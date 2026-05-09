package com.gammatunes.service;

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
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SpotifyControlPlaybackService {

    private final SpotifyControlService spotifyControlService;
    private final SpotifyPlayerService spotifyPlayerService;
    private final SpotifyTrackResolverService spotifyTrackResolverService;
    private final SpotifyControlPlaybackStateStore spotifyControlPlaybackStateStore;
    private final PlaybackRequestFactory playbackRequestFactory;
    private final PlaybackService playbackService;
    private final JDA jda;

    public Mono<Void> syncNow(long guildId) {
        SpotifyControlSession session = spotifyControlService.getControlSession(guildId).orElse(null);
        if (session == null) {
            return Mono.error(new IllegalArgumentException("Spotify control is not enabled for this server."));
        }

        return spotifyPlayerService.getCurrentlyPlaying(session.controllingDiscordUserId())
            .flatMap(currentlyPlaying -> {
                if (!currentlyPlaying.isPlaying() || currentlyPlaying.item().isEmpty()) {
                    return Mono.empty();
                }

                SpotifyTrack spotifyTrack = currentlyPlaying.item().get();
                if (isAlreadySynced(guildId, spotifyTrack.id())) {
                    return Mono.empty();
                }

                return spotifyTrackResolverService.resolveSpotifyTrack(spotifyTrack)
                    .flatMap(track -> playSpotifyTrack(session, track))
                    .doOnSuccess(ignored -> spotifyControlPlaybackStateStore.save(guildId, spotifyTrack.id()));
            });
    }

    private boolean isAlreadySynced(long guildId, String spotifyTrackId) {
        return spotifyControlPlaybackStateStore.get(guildId)
            .map(state -> Objects.equals(state.lastSpotifyTrackId(), spotifyTrackId))
            .orElse(false);
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
