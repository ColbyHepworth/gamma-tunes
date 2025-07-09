package com.gammatunes.backend.audio.lavalink;

import com.gammatunes.backend.audio.player.TrackScheduler;
import com.gammatunes.backend.common.model.PlayerState;
import com.gammatunes.backend.common.model.Session;
import com.gammatunes.backend.common.model.Track;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class LavalinkPlayer implements com.gammatunes.backend.audio.api.AudioPlayer {

    private static final Logger log = LoggerFactory.getLogger(LavalinkPlayer.class);
    private final Session session;
    private final AudioPlayer lavaplayer;
    private final AudioPlayerManager playerManager;
    private final TrackScheduler scheduler = new TrackScheduler();
    final AtomicReference<PlayerState> state = new AtomicReference<>(PlayerState.STOPPED);

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
        scheduler.enqueue(track);
        if (state.get() == PlayerState.STOPPED) {
            skip(); // Start playing if stopped
        }
    }

    @Override
    public Optional<Track> skip() {
        log.info("Session {}: Skipping track", session.id());
        Optional<Track> nextTrack = scheduler.next();
        nextTrack.ifPresent(this::playTrack);
        return nextTrack;
    }

    @Override
    public Optional<Track> previous() {
        log.info("Session {}: Playing previous track", session.id());
        Optional<Track> previousTrack = scheduler.previous();
        previousTrack.ifPresent(this::playTrack);
        return previousTrack;
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
        log.info("Session {}: Stopping player and clearing all tracks", session.id());
        scheduler.clearAll();
        stopPlayerAndClear();
    }

    @Override
    public void clearQueue() {
        log.info("Session {}: Clearing upcoming tracks from queue", session.id());
        scheduler.clearQueue();
    }

    private void playTrack(Track track) {
        playerManager.loadItem(track.identifier(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack loadedTrack) {
                log.info("Session {}: Now playing '{}'", session.id(), loadedTrack.getInfo().title);
                state.set(PlayerState.PLAYING);
                lavaplayer.playTrack(loadedTrack);
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                log.warn("Session {}: Expected a single track, but got a playlist. Playing first track.", session.id());
                trackLoaded(playlist.getTracks().getFirst());
            }
            @Override
            public void noMatches() {
                log.error("Session {}: Could not find a match for track identifier '{}'. Skipping.", session.id(), track.identifier());
                onTrackEnd();
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                log.error("Session {}: Failed to load track identifier '{}': {}", session.id(), track.identifier(), exception.getMessage());
                onTrackEnd();
            }
        });
    }

    private void stopPlayerAndClear() {
        log.info("Session {}: Queue is empty, player stopped", session.id());
        state.set(PlayerState.STOPPED);
        lavaplayer.stopTrack();
        scheduler.clearQueue();
    }

    @Override
    public List<Track> getQueue() {
        return scheduler.getQueue();
    }

    @Override
    public Optional<Track> getCurrentlyPlaying() {
        return scheduler.getCurrentTrack();
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
        skip();
    }
}
