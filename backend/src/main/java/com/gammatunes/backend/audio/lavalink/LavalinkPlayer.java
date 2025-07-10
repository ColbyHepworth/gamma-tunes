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

    private static final Logger logger = LoggerFactory.getLogger(LavalinkPlayer.class);
    private final Session session;
    private final AudioPlayer lavaPlayer;
    private final AudioPlayerManager playerManager;
    private final TrackScheduler scheduler = new TrackScheduler();
    final AtomicReference<PlayerState> state = new AtomicReference<>(PlayerState.STOPPED);

    public LavalinkPlayer(Session session, AudioPlayer lavaplayer, AudioPlayerManager playerManager) {
        this.session = session;
        this.lavaPlayer = lavaplayer;
        this.playerManager = playerManager;
        this.lavaPlayer.addListener(new LavalinkEventHandler(this));
    }

    public AudioPlayer getRealPlayer() {
        return this.lavaPlayer;
    }

    @Override
    public void play(Track track) {
        scheduler.enqueue(track);
        if (state.get() != PlayerState.PLAYING) {
            logger.info("Session {}: Playing track: {}", session.id(), track.title());
            skip();
        } else {
            logger.info("Session {}: Player is already playing, track added to queue: {}", session.id(), track.title());
        }
    }

    @Override
    public void playNow(Track track) {
        logger.info("Session {}: Playing track immediately: {}", session.id(), track.title());
        scheduler.addNext(track);
        skip();
    }

    @Override
    public Optional<Track> skip() {
        Optional<Track> nextTrack = scheduler.next();
        if (nextTrack.isPresent()) {
            logger.info("Session {}: Skipping to next track: {}", session.id(), nextTrack.get().title());
            playTrack(nextTrack.get());
        } else {
            logger.info("Session {}: No more tracks in queue, can't skip", session.id());
            onQueueEnd();
        }
        return nextTrack;
    }

    @Override
    public Optional<Track> previous() {
        Optional<Track> previousTrack = scheduler.previous();
        if (previousTrack.isPresent()) {
            logger.info("Session {}: Now playing previous track: {}", session.id(), previousTrack.get().title());
            playTrack(previousTrack.get());
        } else {
            logger.warn("Session {}: No previous track available to play", session.id());
        }
        return previousTrack;
    }

    @Override
    public void pause() {
        if (state.compareAndSet(PlayerState.PLAYING, PlayerState.PAUSED)) {
            logger.info("Session {}: Pausing player", session.id());
            lavaPlayer.setPaused(true);
        } else {
            logger.warn("Session {}: Cannot pause player, current state is {}", session.id(), state.get());
            if (state.get() == PlayerState.PAUSED) {
                logger.info("Session {}: Player is already paused", session.id());
            } else {
                logger.warn("Session {}: Player is not in a state that allows pausing", session.id());
            }
        }
    }

    @Override
    public void resume() {
        // Allow resuming from PAUSED or if the player is STOPPED (end of queue)
        if (state.compareAndSet(PlayerState.PAUSED, PlayerState.PLAYING)) {
            logger.info("Session {}: Resuming player", session.id());
            lavaPlayer.setPaused(false);
        } else if (state.get() == PlayerState.STOPPED && scheduler.getCurrentTrack().isPresent()) {
            // If stopped but there's a track, try to play it (handles end-of-queue resume)
            logger.info("Session {}: Resuming player from stopped state with current track", session.id());
            playTrack(scheduler.getCurrentTrack().get());
        } else {
            logger.warn("Session {}: Cannot resume player, current state is {}", session.id(), state.get());
        }
    }

    @Override
    public void stopAndClear() {
        logger.info("Session {}: Stopping player and clearing all tracks", session.id());
        scheduler.clearAll();
        onQueueEnd();
    }

    @Override
    public void clearQueue() {
        logger.info("Session {}: Clearing upcoming tracks from queue", session.id());
        scheduler.clearQueue();
    }

    private void playTrack(Track track) {
        state.set(PlayerState.LOADING);
        playerManager.loadItem(track.identifier(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack loadedTrack) {
                logger.info("Session {}: Now playing '{}'", session.id(), loadedTrack.getInfo().title);
                state.set(PlayerState.PLAYING);
                lavaPlayer.playTrack(loadedTrack);
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                logger.warn("Session {}: Expected a single track, but got a playlist. Playing first track.", session.id());
                trackLoaded(playlist.getTracks().getFirst());
            }
            @Override
            public void noMatches() {
                logger.error("Session {}: Could not find a match for track identifier '{}'. Skipping.", session.id(), track.identifier());
                onTrackEnd();
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Session {}: Failed to load track identifier '{}': {}", session.id(), track.identifier(), exception.getMessage());
                onTrackEnd();
            }
        });
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
        logger.debug("Session {}: Current player state is {}", session.id(), state.get());
        return state.get();
    }

    @Override
    public Session getSession() {
        return session;
    }

    private void onQueueEnd() {
        logger.info("Session {}: Queue is empty, player stopped.", session.id());
        state.set(PlayerState.STOPPED);
        lavaPlayer.stopTrack();
    }

    void onTrackEnd() {
        logger.debug("Session {}: Track ended, trying to play next in queue", session.id());
        skip();
    }
}
