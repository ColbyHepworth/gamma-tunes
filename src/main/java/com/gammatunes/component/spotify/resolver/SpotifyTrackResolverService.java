package com.gammatunes.component.spotify.resolver;

import com.gammatunes.component.spotify.api.request.SpotifyPlaylistTracksRequest;
import com.gammatunes.component.spotify.api.response.SpotifyPlaylistTrackItem;
import com.gammatunes.component.spotify.api.response.SpotifyPlaylistTracksPage;
import com.gammatunes.component.spotify.api.response.SpotifyTrack;
import com.gammatunes.component.spotify.auth.SpotifyAccessToken;
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

    private static final int PLAYLIST_PAGE_LIMIT = 50;
    private static final String PLAYLIST_TRACK_FIELDS = "items(is_local,track(id,name,uri,duration_ms,artists(name))),next,limit,offset,total";

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
            .flatMapMany(token -> playlistTrackPages(token, spotifyPlaylistId, 0))
            .flatMap(page -> Flux.fromIterable(page.items()))
            .filter(item -> !item.isLocal())
            .flatMap(this::resolvePlaylistTrackItem)
            .collectList();
    }

    private Flux<SpotifyPlaylistTracksPage> playlistTrackPages(SpotifyAccessToken token, String playlistId, int offset) {
        return spotifyPlaylistClient.getPlaylistTracks(token, playlistTracksRequest(playlistId, offset))
            .expand(page -> nextPlaylistTracksPage(token, playlistId, page));
    }

    private Mono<SpotifyPlaylistTracksPage> nextPlaylistTracksPage(SpotifyAccessToken token, String playlistId, SpotifyPlaylistTracksPage page) {
        if (page.next().isEmpty()) {
            return Mono.empty();
        }
        return spotifyPlaylistClient.getPlaylistTracks(token, playlistTracksRequest(playlistId, page.offset() + page.limit()));
    }

    private SpotifyPlaylistTracksRequest playlistTracksRequest(String playlistId, int offset) {
        return new SpotifyPlaylistTracksRequest(
            playlistId,
            PLAYLIST_PAGE_LIMIT,
            offset,
            PLAYLIST_TRACK_FIELDS,
            null
        );
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
