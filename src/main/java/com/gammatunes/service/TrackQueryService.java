package com.gammatunes.service;

import com.gammatunes.component.audio.core.PlayerRegistry;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.player.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for resolving and searching tracks using Lavalink.
 * It processes queries, checks if they are direct URLs or search queries,
 * and interacts with the Lavalink client to load tracks or playlists.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackQueryService {

    private final PlayerRegistry players;
    private final LavalinkClient lavalinkClient;

    /**
     * Regular expression pattern to match various music service URLs.
     * This includes YouTube, Spotify, SoundCloud, Bandcamp, Deezer, and Open Spotify URLs.
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be|spotify\\.com|soundcloud\\.com|bandcamp\\.com|deezer\\.com|open\\.spotify\\.com).*",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Resolves a track from a query string.
     * If the query is a direct URL, it uses it as-is.
     * If it's a search term, it prefixes it with "ytsearch:".
     *
     * @param query The query string to resolve.
     * @return A Mono that emits the resolved Track.
     */
    public Mono<Track> resolve(String query) {
        String processedQuery = processQuery(query);
        log.debug("Resolving query: '{}' -> '{}'", query, processedQuery);

        return lavalinkClient.getNodes().getFirst()
            .loadItem(processedQuery)
            .flatMap(this::firstTrack);
    }

    /**
     * Searches for tracks based on a query string.
     * If the query is a direct URL, it uses it as-is.
     * If it's a search term, it prefixes it with "ytsearch:".
     *
     * @param query The query string to search for tracks.
     * @param limit The maximum number of tracks to return.
     * @return A Mono that emits a list of Tracks matching the search query.
     */
    public Mono<List<Track>> search(String query, int limit) {
        String processedQuery = processQuery(query);
        log.debug("Searching for tracks: '{}' -> '{}' (limit: {})", query, processedQuery, limit);

        return lavalinkClient.getNodes().getFirst()
            .loadItem(processedQuery)
            .flatMap(loadResult -> searchResults(loadResult, limit));
    }

    /**
     * Processes the query string to determine if it's a search term or a direct URL.
     * If it's a search term, it prefixes it with "ytsearch:".
     * If it's a direct URL, it returns the URL as-is.
     *
     * @param query The query string to process.
     * @return The processed query string ready for Lavalink.
     */
    private String processQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        String trimmedQuery = query.trim();

        // If it's already a search query (starts with a search prefix), use as-is
        if (isSearchQuery(trimmedQuery)) {
            return trimmedQuery;
        }

        // If it's a direct URL, use as-is
        if (isDirectUrl(trimmedQuery)) {
            return trimmedQuery;
        }

        // Otherwise, treat as a search term and prefix with YouTube search
        return "ytsearch:" + trimmedQuery;
    }

    /**
     * Checks if the query is a search query by looking for specific prefixes.
     *
     * @param query The query string to check.
     * @return true if the query is a search query, false otherwise.
     */
    private boolean isSearchQuery(String query) {
        return query.startsWith("ytsearch:") ||
               query.startsWith("ytmsearch:") ||
               query.startsWith("scsearch:") ||
               query.startsWith("spsearch:");
    }

    /**
     * Checks if the query is a direct URL by matching it against a predefined pattern.
     *
     * @param query The query string to check.
     * @return true if the query is a direct URL, false otherwise.
     */
    private boolean isDirectUrl(String query) {
        return URL_PATTERN.matcher(query).matches();
    }

    /**
     * Extracts the first track from the Lavalink load result.
     * Handles different types of load results such as TrackLoaded, PlaylistLoaded, SearchResult, NoMatches, and LoadFailed.
     *
     * @param loadResult The result of the track loading operation.
     * @return A Mono that emits the first Track or an error if no tracks were found.
     */
    private Mono<Track> firstTrack(LavalinkLoadResult loadResult) {
        return switch (loadResult) {

            case TrackLoaded trackLoaded ->
                trackLoaded(trackLoaded);

            case PlaylistLoaded playlistLoaded
                // TODO: Handle playlists
                when playlistLoaded.getTracks().size() <= 50 ->
                Mono.just(playlistLoaded.getTracks().getFirst());

            case SearchResult searchResult ->
                searchResult.getTracks().isEmpty()
                    ? Mono.error(new IllegalArgumentException("No results for query"))
                    : Mono.just(searchResult.getTracks().getFirst());

            case NoMatches noMatches ->
                Mono.error(new IllegalArgumentException("Nothing found for query"));

            case LoadFailed lf ->
                Mono.error(new IllegalStateException("Failed to load track: " + lf.getException().getMessage()));
            default -> throw new IllegalStateException("Unexpected load result: " + loadResult);
        };
    }

    /**
     * Handles the TrackLoaded result by returning the loaded track.
     *
     * @param loadResult The TrackLoaded result containing the track.
     * @return A Mono that emits the loaded Track.
     */
    private Mono<Track> trackLoaded(TrackLoaded loadResult) {
        return Mono.just(loadResult.getTrack());
    }

    /**
     * Handles the PlaylistLoaded result by returning the list of tracks in the playlist.
     *
     * @param loadResult The PlaylistLoaded result containing the tracks.
     * @return A Mono that emits the list of tracks in the playlist.
     */
    private Mono<List<Track>> playlistLoaded(PlaylistLoaded loadResult) {
        return Mono.just(loadResult.getTracks());
    }

    /**
     * Handles the SearchResult by checking if there are tracks available.
     * If tracks are found, it returns them; otherwise, it throws an error.
     *
     * @param loadResult The SearchResult containing the tracks.
     * @return A Mono that emits the list of tracks or an error if no tracks were found.
     */
    private Mono<List<Track>> searchResult(SearchResult loadResult) {
        return loadResult.getTracks().isEmpty()
            ? Mono.error(new IllegalArgumentException("No results for query"))
            : Mono.just(loadResult.getTracks());
    }

    /**
     * Processes the search results based on the type of load result.
     * It returns a Mono that emits a list of tracks or an error if no tracks were found.
     *
     * @param loadResult The result of the track loading operation.
     * @param limit The maximum number of tracks to return.
     * @return A Mono that emits a list of Tracks or an error if no tracks were found.
     */
    private Mono<List<Track>> searchResults(LavalinkLoadResult loadResult, int limit) {
        return switch (loadResult) {

            case TrackLoaded trackLoaded ->
                Mono.just(List.of(trackLoaded.getTrack()));

            case PlaylistLoaded playlistLoaded ->
                Mono.just(playlistLoaded.getTracks().stream()
                    .limit(limit)
                    .toList());

            case SearchResult searchResult ->
                searchResult.getTracks().isEmpty()
                    ? Mono.error(new IllegalArgumentException("No results for query"))
                    : Mono.just(searchResult.getTracks().stream()
                        .limit(limit)
                        .toList());

            case NoMatches noMatches ->
                Mono.error(new IllegalArgumentException("Nothing found for query"));

            case LoadFailed lf ->
                Mono.error(new IllegalStateException("Failed to load track: " + lf.getException().getMessage()));

            default -> throw new IllegalStateException("Unexpected load result: " + loadResult);
        };
    }
}
