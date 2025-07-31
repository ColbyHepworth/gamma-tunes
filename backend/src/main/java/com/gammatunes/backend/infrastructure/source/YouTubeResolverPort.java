package com.gammatunes.backend.infrastructure.source;

import com.gammatunes.backend.application.port.out.TrackResolverPort;
import com.gammatunes.backend.domain.exception.TrackLoadException;
import com.gammatunes.backend.domain.exception.UnresolvableTrackException;
import com.gammatunes.backend.domain.model.Track;
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
public class YouTubeResolverPort implements TrackResolverPort {

    private final AudioPlayerManager playerManager;

    public YouTubeResolverPort(AudioPlayerManager playerManager) {
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
            String videoId = loadedTrack.getInfo().identifier;
            String thumb   = deriveYoutubeThumb(videoId, loadedTrack.getInfo().uri);

            // Convert lavaplayer's AudioTrack into our own Track model.
            return new Track(
                loadedTrack.getInfo().uri, // Using the URI as the identifier for re-loading
                loadedTrack.getInfo().title,
                loadedTrack.getInfo().author,
                Duration.ofMillis(loadedTrack.getDuration()),
                loadedTrack.getInfo().uri,
                thumb
            );
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            if (e.getCause() instanceof TrackLoadException) {
                throw (TrackLoadException) e.getCause();
            }
            throw new TrackLoadException("Could not resolve track in time: " + e.getMessage(), e);
        }
    }

    private String deriveYoutubeThumb(String id, String uri) {
        if (id == null) return null;
        // Quick heuristic: accept only if the URI is from youtube or youtu.be
        if (uri.contains("youtube.com") || uri.contains("youtu.be")) {
            return "https://img.youtube.com/vi/" + id + "/hqdefault.jpg";
        }
        return null;
    }
}
