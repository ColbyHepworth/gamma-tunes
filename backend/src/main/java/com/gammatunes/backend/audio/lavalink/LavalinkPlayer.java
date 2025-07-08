package com.gammatunes.backend.audio.lavalink;

import com.gammatunes.backend.common.model.PlayerState;
import com.gammatunes.backend.common.model.Session;
import com.gammatunes.backend.common.model.Track;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer; // Import the lavaplayer class
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A player implementation that uses the lavaplayer library to handle audio playback.
 * This class manages the state of the player, the queue of tracks, and interacts with the lavaplayer API.
 */
public class LavalinkPlayer implements com.gammatunes.backend.audio.api.AudioPlayer {

    private static final Logger log = LoggerFactory.getLogger(LavalinkPlayer.class);
    private final Session session;
    private final AudioPlayer lavaplayer;
    private final AudioPlayerManager playerManager;
    final AtomicReference<PlayerState> state = new AtomicReference<>(PlayerState.STOPPED);
    final AtomicReference<Track> currentlyPlaying = new AtomicReference<>(null);
    private final ConcurrentLinkedQueue<Track> queue = new ConcurrentLinkedQueue<>();

    public LavalinkPlayer(Session session, AudioPlayer lavaplayer, AudioPlayerManager playerManager) {
        this.session = session;
        this.lavaplayer = lavaplayer;
        this.playerManager = playerManager;
        this.lavaplayer.addListener(new LavalinkEventHandler(this));
    }

    public AudioPlayer getRealPlayer() {
        return this.lavaplayer;
    }

    @Override
    public void enqueue(Track track) {
        log.debug("Session {}: Enqueuing track '{}'", session.id(), track.title());
        queue.add(track);
        if (state.compareAndSet(PlayerState.STOPPED, PlayerState.LOADING)) {
            playNextInQueue();
        }
    }

    @Override
    public void pause() {
        if (state.compareAndSet(PlayerState.PLAYING, PlayerState.PAUSED)) {
            log.info("Session {}: Pausing player", session.id());
            lavaplayer.setPaused(true);
        }
    }

    @Override
    public void resume() {
        if (state.compareAndSet(PlayerState.PAUSED, PlayerState.PLAYING)) {
            log.info("Session {}: Resuming player", session.id());
            lavaplayer.setPaused(false);
        }
    }

    @Override
    public void stop() {
        log.info("Session {}: Stopping player and clearing queue", session.id());
        state.set(PlayerState.STOPPED);
        queue.clear();
        lavaplayer.stopTrack();
        currentlyPlaying.set(null);
    }

    @Override
    public Optional<Track> skip() {
        log.info("Session {}: Skipping track", session.id());
        Track skippedTrack = currentlyPlaying.get();
        playNextInQueue();
        return Optional.ofNullable(skippedTrack);
    }

    @Override
    public List<Track> getQueue() {
        return List.copyOf(queue);
    }

    @Override
    public Optional<Track> getCurrentlyPlaying() {
        return Optional.ofNullable(currentlyPlaying.get());
    }

    @Override
    public PlayerState getState() {
        return state.get();
    }

    @Override
    public Session getSession() {
        return session;
    }

    void onTrackEnd() {
        log.debug("Session {}: Track ended, trying to play next in queue", session.id());
        playNextInQueue();
    }

    private void playNextInQueue() {
        Track nextTrack = queue.poll();
        if (nextTrack != null) {
            currentlyPlaying.set(nextTrack);
            playerManager.loadItem(nextTrack.identifier(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    log.info("Session {}: Now playing '{}' by {}", session.id(), track.getInfo().title, track.getInfo().author);
                    state.set(PlayerState.PLAYING);
                    lavaplayer.playTrack(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    // This should not happen if our resolver works correctly, but as a fallback, play the first track.
                    log.warn("Session {}: Expected a single track, but got a playlist. Playing first track.", session.id());
                    trackLoaded(playlist.getTracks().getFirst());
                }

                @Override
                public void noMatches() {
                    log.error("Session {}: Could not find a match for track identifier '{}'. Skipping.", session.id(), nextTrack.identifier());
                    onTrackEnd(); // Try to play the next song in the queue
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    log.error("Session {}: Failed to load track identifier '{}': {}", session.id(), nextTrack.identifier(), exception.getMessage());
                    onTrackEnd(); // Try to play the next song in the queue
                }
            });
        } else {
            log.info("Session {}: Queue is empty, player stopped", session.id());
            currentlyPlaying.set(null);
            state.set(PlayerState.STOPPED);
        }
    }
}


