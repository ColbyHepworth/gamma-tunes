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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaybackService {

    private final PlayerRegistry playerRegistry;
    private final DiscordVoiceConnector discordVoiceConnector;
    private final PlayerPanelService playerPanelService;

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
