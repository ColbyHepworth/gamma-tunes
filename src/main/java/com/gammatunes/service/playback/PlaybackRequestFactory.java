package com.gammatunes.service.playback;

import com.gammatunes.model.dto.RequesterInfo;
import com.gammatunes.service.SpotifyControlService;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaybackRequestFactory {

    private final SpotifyControlService spotifyControlService;

    public PlaybackRequest create(
        long guildId,
        long voiceChannelId,
        TextChannel textChannel,
        RequesterInfo requesterInfo,
        List<Track> tracks,
        PlaybackMode mode
    ) {
        return new PlaybackRequest(
            guildId,
            voiceChannelId,
            textChannel,
            requesterInfo,
            spotifyControlService.getControlSession(guildId).orElse(null),
            tracks,
            mode
        );
    }
}
