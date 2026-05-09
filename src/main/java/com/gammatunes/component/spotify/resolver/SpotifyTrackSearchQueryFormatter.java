package com.gammatunes.component.spotify.resolver;

import com.gammatunes.component.spotify.api.response.SpotifyArtist;
import com.gammatunes.component.spotify.api.response.SpotifyTrack;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SpotifyTrackSearchQueryFormatter {

    public String format(SpotifyTrack spotifyTrack) {
        String artists = spotifyTrack.artists().stream()
            .map(SpotifyArtist::name)
            .collect(Collectors.joining(", "));

        return "ytmsearch:" + artists + " - " + spotifyTrack.name();
    }
}
