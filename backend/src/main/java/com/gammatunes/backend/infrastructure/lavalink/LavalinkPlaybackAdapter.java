package com.gammatunes.backend.infrastructure.lavalink;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.model.Track;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.domain.player.TrackScheduler;
import com.gammatunes.backend.infrastructure.lavalink.event.LavalinkEventHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Infrastructure adapter that turns a raw Lavaplayer {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer}
 * into the domain‐level {@link AudioPlayer} interface expected by the rest of the system.
 * <p>Instances are created by {@link LavalinkPlayerRegistry}, one per {@link Session}.</p>
 */
public class LavalinkPlaybackAdapter implements AudioPlayer {

    private static final Logger log = LoggerFactory.getLogger(LavalinkPlaybackAdapter.class);

    private final Session session;
    @Getter
    private final com.sedmelluq.discord.lavaplayer.player.AudioPlayer lavaPlayer;   // exposed for cleanup
    private final AudioPlayerManager playerManager;

    private final TrackScheduler scheduler = new TrackScheduler();
    private final AtomicReference<PlayerState> state = new AtomicReference<>(PlayerState.STOPPED);


    public LavalinkPlaybackAdapter(Session session,
                                   com.sedmelluq.discord.lavaplayer.player.AudioPlayer lavaPlayer,
                                   AudioPlayerManager playerManager) {
        this.session       = session;
        this.lavaPlayer    = lavaPlayer;
        this.playerManager = playerManager;
        this.lavaPlayer.addListener(new LavalinkEventHandler(this));
    }


    @Override
    public PlayerOutcome play(Track track) {
        scheduler.enqueue(track);

        // Start only if the player is stopped or finished loading – NOT when paused.
        if (state.get() == PlayerState.STOPPED
            || state.get() == PlayerState.LOADING) {

            // skip() will advance to the first track in the queue and
            // return SKIPPED (we started something) or NO_NEXT_TRACK (should never happen here).
            return skip();
        }
        // When PLAYING or PAUSED just leave it in the queue.
        return PlayerOutcome.ADDED_TO_QUEUE;
    }

    @Override
    public PlayerOutcome playNow(Track track) {
        scheduler.addNext(track);
        return skip();
    }

    @Override
    public PlayerOutcome skip() {
        boolean wasPaused = state.get() == PlayerState.PAUSED;

        /* 1. Peek – do we actually have something after this track? */
        Optional<Track> next = scheduler.peekNext();
        if (next.isEmpty()) {
            return PlayerOutcome.NO_NEXT_TRACK;
        }

        if (wasPaused) {
            lavaPlayer.setPaused(false);
            changeState(PlayerState.PAUSED, PlayerState.PLAYING, /*pause=*/false);
        }

        /* 2️.  We *do* have another track ─ perform real skip */
        lavaPlayer.stopTrack();
        state.set(PlayerState.LOADING);

        // pop the queue head and start it; we resume if we were playing before
        scheduler.next();
        playTrack(next.get());
        return PlayerOutcome.SKIPPED;
    }


    @Override
    public PlayerOutcome previous() {
        boolean wasPaused = state.get() == PlayerState.PAUSED;

        /* 1. Is there actually something in the history? */
        Optional<Track> prev = scheduler.peekPrevious();
        if (prev.isEmpty()) {
            return PlayerOutcome.NO_PREVIOUS_TRACK; // keep playing / stay paused
        }


        if (wasPaused) {
            lavaPlayer.setPaused(false);
            changeState(PlayerState.PAUSED, PlayerState.PLAYING, /*pause=*/false);
        }
        /* 2. Real “go-back” */
        lavaPlayer.stopTrack();
        state.set(PlayerState.LOADING);

        scheduler.previous();
        playTrack(prev.get());
        return PlayerOutcome.PLAYING_PREVIOUS;
    }


    @Override
    public PlayerOutcome pause() {
        if (state.get() == PlayerState.PAUSED) {
            return PlayerOutcome.ALREADY_PAUSED;
        }
        changeState(PlayerState.PLAYING, PlayerState.PAUSED, /*pause=*/true);
        return PlayerOutcome.PAUSED;
    }

    @Override
    public PlayerOutcome resume() {
        if (state.get() == PlayerState.PLAYING) {
            return PlayerOutcome.ALREADY_PLAYING;
        }
        changeState(PlayerState.PAUSED, PlayerState.PLAYING, /*pause=*/false);
        return PlayerOutcome.RESUMED;
    }

    @Override
    public PlayerOutcome stop() {
        if (state.get() == PlayerState.STOPPED) {
            return PlayerOutcome.ALREADY_STOPPED;
        }
        scheduler.clearAll();
        onQueueEnd();
        return PlayerOutcome.STOPPED;
    }

    @Override
    public void clearQueue() {
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
    public long getTrackPosition() {
        AudioTrack t = lavaPlayer.getPlayingTrack();
        return t == null ? 0L : t.getPosition();
    }

    @Override
    public Session getSession() {
        return session;
    }

    private void playTrack(Track track) {
        state.set(PlayerState.LOADING);
        playerManager.loadItem(track.identifier(), new AudioLoadResultHandler() {


            @Override
            public void trackLoaded(AudioTrack loaded) {
                log.info("[{}] ▶ {}", session.id(), loaded.getInfo().title);

                lavaPlayer.playTrack(loaded);
                lavaPlayer.setPaused(true);
                state.set(PlayerState.PAUSED);
            }

            @Override
            public void playlistLoaded(AudioPlaylist pl) {
                start(pl.getTracks().getFirst());
            }

            @Override
            public void noMatches() {
                log.warn("[{}] No match for '{}'", session.id(), track.identifier());
                onTrackEnd();
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                log.error("[{}] Load failed '{}': {}", session.id(), track.identifier(), ex.getMessage());
                onTrackEnd();
            }

            private void start(AudioTrack at) {
                state.set(PlayerState.PLAYING);
                lavaPlayer.playTrack(at);
            }
        });
    }

    public void onTrackEnd() {
        if (state.get() != PlayerState.LOADING) {   // already skipping?
            skip();
        }
    }

    private void onQueueEnd() {
        log.info("[{}] Queue empty — stopping", session.id());
        state.set(PlayerState.STOPPED);
        lavaPlayer.stopTrack();
    }

    private void changeState(PlayerState from, PlayerState to, boolean pause) {
        if (state.compareAndSet(from, to)) {
            lavaPlayer.setPaused(pause);
        }
    }
}
