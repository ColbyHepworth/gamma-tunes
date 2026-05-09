package com.gammatunes.service.playback;

import com.gammatunes.component.spotify.control.SpotifyControlSession;
import com.gammatunes.model.dto.RequesterInfo;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;
import java.util.Objects;

public record PlaybackRequest(
    long guildId,
    long voiceChannelId,
    TextChannel textChannel,
    RequesterInfo requesterInfo,
    SpotifyControlSession spotifyControlSession,
    List<Track> tracks,
    PlaybackMode mode
) {
    public PlaybackRequest {
        Objects.requireNonNull(tracks, "tracks must not be null");
        Objects.requireNonNull(mode, "mode must not be null");

        if (tracks.isEmpty()) {
            throw new IllegalArgumentException("tracks must not be empty");
        }

        tracks = List.copyOf(tracks);
    }
}
