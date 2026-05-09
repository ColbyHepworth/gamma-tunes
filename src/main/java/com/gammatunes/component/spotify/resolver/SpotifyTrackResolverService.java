package com.gammatunes.component.spotify.resolver;

import com.gammatunes.component.spotify.api.response.SpotifyPlaylistTrackItem;
import com.gammatunes.component.spotify.api.response.SpotifyTrack;
import com.gammatunes.component.spotify.track.SpotifyPlaylistClient;
import com.gammatunes.component.spotify.track.SpotifyTrackClient;
import com.gammatunes.service.SpotifyAccountLinkService;
import com.gammatunes.service.TrackQueryService;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpotifyTrackResolverService {

    private final SpotifyAccountLinkService spotifyAccountLinkService;
    private final SpotifyTrackClient spotifyTrackClient;
    private final SpotifyPlaylistClient spotifyPlaylistClient;
    private final SpotifyTrackSearchQueryFormatter spotifyTrackSearchQueryFormatter;
    private final TrackQueryService trackQueryService;

    public Mono<Track> resolveTrack(long discordUserId, String spotifyTrackId) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyTrackClient.getTrack(spotifyTrackId, token))
            .map(spotifyTrackSearchQueryFormatter::format)
            .flatMap(trackQueryService::resolve);
    }

    public Mono<List<Track>> resolvePlaylist(long discordUserId, String spotifyPlaylistId) {
        return spotifyAccountLinkService.getValidAccessToken(discordUserId)
            .flatMap(token -> spotifyPlaylistClient.getPlaylistTracks(token, spotifyPlaylistId))
            .flatMapMany(page -> Flux.fromIterable(page.items()))
            .filter(item -> !item.isLocal())
            .flatMap(this::resolvePlaylistTrackItem)
            .collectList();
    }

    private Mono<Track> resolvePlaylistTrackItem(SpotifyPlaylistTrackItem item) {
        return item.item()
            .or(item::track)
            .map(this::resolveSpotifyTrack)
            .orElseGet(Mono::empty);
    }

    private Mono<Track> resolveSpotifyTrack(SpotifyTrack spotifyTrack) {
        return trackQueryService.resolve(spotifyTrackSearchQueryFormatter.format(spotifyTrack));
    }
}
