package com.gammatunes.service.playback;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gammatunes.component.audio.core.Player;
import com.gammatunes.component.audio.core.PlayerRegistry;
import com.gammatunes.component.discord.DiscordVoiceConnector;
import com.gammatunes.model.dto.RequesterInfo;
import com.gammatunes.service.PlayerPanelService;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaybackService {

    private final PlayerRegistry playerRegistry;
    private final DiscordVoiceConnector discordVoiceConnector;
    private final PlayerPanelService playerPanelService;

    public Mono<Void> pause(long guildId) {
        return playerRegistry.getOrCreate(guildId).flatMap(Player::pause);
    }

    public Mono<Void> resume(long guildId) {
        return playerRegistry.getOrCreate(guildId).flatMap(Player::resume);
    }

    public Mono<Void> skip(long guildId) {
        return playerRegistry.getOrCreate(guildId).flatMap(Player::skip);
    }

    public Mono<Void> previous(long guildId) {
        return playerRegistry.getOrCreate(guildId).flatMap(Player::previous);
    }

    public Mono<Void> seek(long guildId, long positionMs) {
        return playerRegistry.getOrCreate(guildId)
            .flatMap(player -> player.seek(positionMs));
    }

    public Mono<Long> getPositionMs(long guildId) {
        return playerRegistry.getOrCreate(guildId)
            .map(Player::getPositionMs);
    }

    public Mono<Void> jumpToTrack(long guildId, String trackIdentifier) {
        return playerRegistry.getOrCreate(guildId)
            .flatMap(player -> player.jumpToTrack(trackIdentifier));
    }

    public Mono<Void> shuffle(long guildId) {
        return playerRegistry.getOrCreate(guildId)
            .doOnNext(Player::shuffle)
            .then();
    }

    public Mono<Void> toggleRepeat(long guildId) {
        return playerRegistry.getOrCreate(guildId)
            .doOnNext(Player::toggleRepeat)
            .then();
    }

    public Mono<Boolean> getRepeat(long guildId) {
        return playerRegistry.getOrCreate(guildId)
            .map(Player::isRepeatEnabled);
    }

    public Mono<Void> stop(long guildId) {
        return playerRegistry.getOrCreate(guildId)
            .flatMap(Player::stop)
            .onErrorResume(error -> {
                log.warn("Player stop failed for guild {}", guildId, error);
                return Mono.empty();
            })
            .then(playerPanelService.deletePanel(guildId).onErrorResume(error -> {
                log.warn("Panel delete failed for guild {}", guildId, error);
                return Mono.empty();
            }))
            .then(discordVoiceConnector.disconnect(guildId))
            .doFinally(signal -> playerRegistry.destroy(guildId));
    }

    public Mono<Void> play(PlaybackRequest request) {
        return discordVoiceConnector.connect(request.guildId(), request.voiceChannelId())
            .then(playerRegistry.getOrCreate(request.guildId()))
            .flatMap(player -> play(player, withRequester(request.tracks(), request.requesterInfo()), request.mode()))
            .then(Mono.defer(() -> createPanelIfMissing(request.guildId(), request.textChannel())));
    }

    private Mono<Void> play(Player player, List<Track> tracks, PlaybackMode mode) {
        return switch (mode) {
            case QUEUE -> tracks.size() == 1
                ? player.play(tracks.getFirst())
                : player.playAll(tracks);
            case PLAY_NOW -> playNow(player, tracks);
        };
    }

    private Mono<Void> playNow(Player player, List<Track> tracks) {
        if (tracks.size() == 1) {
            return player.playNow(tracks.getFirst());
        }

        return player.playNow(tracks.getFirst())
            .then(Mono.defer(() -> player.playAll(tracks.subList(1, tracks.size()))));
    }

    private Mono<Void> createPanelIfMissing(long guildId, TextChannel textChannel) {
        if (textChannel == null || playerPanelService.getMessage(guildId).isPresent()) {
            return Mono.empty();
        }
        return playerPanelService.createPanel(guildId, textChannel);
    }

    private List<Track> withRequester(List<Track> tracks, RequesterInfo requesterInfo) {
        if (requesterInfo == null) {
            return tracks;
        }

        return tracks.stream()
            .map(track -> attachRequester(track, requesterInfo))
            .toList();
    }

    private Track attachRequester(Track track, RequesterInfo requesterInfo) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("userId", requesterInfo.userId());
        node.put("displayName", requesterInfo.displayName());
        if (requesterInfo.avatarUrl() != null) {
            node.put("avatarUrl", requesterInfo.avatarUrl());
        }
        track.setUserData(node);
        return track;
    }
}
