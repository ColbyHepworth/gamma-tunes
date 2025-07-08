package com.gammatunes.backend.audio.source;

import com.gammatunes.backend.audio.exception.TrackLoadException;
import com.gammatunes.backend.audio.exception.UnresolvableTrackException;
import com.gammatunes.backend.common.model.Track;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A TrackResolver implementation for YouTube links and searches.
 * It uses the lavaplayer library to load tracks.
 */
@Component
public class YouTubeResolver implements TrackResolver {

    private final AudioPlayerManager playerManager;

    public YouTubeResolver(AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean canResolve(String query) {
        // A simple check. This could be improved with more robust URL parsing.
        // For now, we assume if it's not a spotify link, it might be a YouTube link or a search.
        return !query.contains("spotify.com");
    }

    @Override
    public Track resolve(String query) throws TrackLoadException {
        // Lavaplayer's loadItem is asynchronous. We use a CompletableFuture to wait for the result.
        CompletableFuture<AudioTrack> future = new CompletableFuture<>();
        String identifier = query.startsWith("http") ? query : "ytsearch:" + query;

        playerManager.loadItem(identifier, new com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                future.complete(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // If it's a search, take the first result. Otherwise, we don't support playlists yet.
                if (playlist.isSearchResult()) {
                    future.complete(playlist.getTracks().getFirst());
                } else {
                    future.completeExceptionally(new UnresolvableTrackException("Playlists are not supported."));
                }
            }

            @Override
            public void noMatches() {
                future.completeExceptionally(new UnresolvableTrackException("No match found for: " + query));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                future.completeExceptionally(new TrackLoadException("Failed to load track: " + exception.getMessage(), exception));
            }
        });

        try {
            // Wait for the result, with a timeout.
            AudioTrack loadedTrack = future.get(10, TimeUnit.SECONDS);
            // Convert lavaplayer's AudioTrack into our own Track model.
            return new Track(
                loadedTrack.getInfo().uri, // Using the URI as the identifier for re-loading
                loadedTrack.getInfo().title,
                loadedTrack.getInfo().author,
                Duration.ofMillis(loadedTrack.getDuration()),
                loadedTrack.getInfo().uri,
                // You could extract a thumbnail URL here if available, e.g., from YouTube API
                null
            );
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            if (e.getCause() instanceof TrackLoadException) {
                throw (TrackLoadException) e.getCause();
            }
            throw new TrackLoadException("Could not resolve track in time: " + e.getMessage(), e);
        }
    }
}
