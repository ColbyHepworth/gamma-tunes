package com.gammatunes.service;

import com.gammatunes.component.spotify.resolver.SpotifyResource;
import com.gammatunes.component.spotify.resolver.SpotifyResourceType;
import com.gammatunes.component.spotify.resolver.SpotifyTrackResolverService;
import com.gammatunes.component.spotify.resolver.SpotifyUrlParser;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayInputResolverService {

    private final SpotifyUrlParser spotifyUrlParser;
    private final SpotifyTrackResolverService spotifyTrackResolverService;
    private final TrackQueryService trackQueryService;

    public Mono<Track> resolveOne(long discordUserId, String input) {
        Optional<SpotifyResource> spotifyResource = spotifyUrlParser.parse(input);
        if (spotifyResource.isEmpty()) {
            return trackQueryService.resolve(input);
        }

        SpotifyResource resource = spotifyResource.get();
        if (resource.type() == SpotifyResourceType.TRACK) {
            return spotifyTrackResolverService.resolveTrack(discordUserId, resource.id());
        }

        return spotifyTrackResolverService.resolvePlaylist(discordUserId, resource.id())
            .flatMap(tracks -> tracks.isEmpty()
                ? Mono.error(new IllegalArgumentException("Spotify playlist did not resolve to any playable tracks."))
                : Mono.just(tracks.getFirst()));
    }

    public Mono<List<Track>> resolveAll(long discordUserId, String input) {
        Optional<SpotifyResource> spotifyResource = spotifyUrlParser.parse(input);
        if (spotifyResource.isEmpty()) {
            return trackQueryService.resolveAll(input);
        }

        SpotifyResource resource = spotifyResource.get();
        return switch (resource.type()) {
            case TRACK -> spotifyTrackResolverService.resolveTrack(discordUserId, resource.id())
                .map(List::of);
            case PLAYLIST -> spotifyTrackResolverService.resolvePlaylist(discordUserId, resource.id());
        };
    }
}
