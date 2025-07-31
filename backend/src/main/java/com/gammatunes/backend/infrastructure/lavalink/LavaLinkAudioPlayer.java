package com.gammatunes.backend.infrastructure.lavalink;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.model.Track;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.domain.player.PlayerOutcomeListener;
import com.gammatunes.backend.domain.player.TrackScheduler;
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
 * This class adapts the Lavaplayer's AudioPlayer to the GammaTunes AudioPlayer interface.
 * It manages playback, queueing, and state transitions for audio tracks in a session.
 * It also handles events from Lavaplayer to update the player state accordingly.
 */
public class LavaLinkAudioPlayer implements AudioPlayer {

    private static final Logger log = LoggerFactory.getLogger(LavaLinkAudioPlayer.class);

    private final Session session;
    @Getter
    private final com.sedmelluq.discord.lavaplayer.player.AudioPlayer lavaPlayer;   // exposed for cleanup
    private final AudioPlayerManager playerManager;
    private final PlayerOutcomeListener listener;

    private final TrackScheduler scheduler = new TrackScheduler();
    private final AtomicReference<PlayerState> state = new AtomicReference<>(PlayerState.STOPPED);


    public LavaLinkAudioPlayer(Session session,
                               com.sedmelluq.discord.lavaplayer.player.AudioPlayer lavaPlayer,
                               AudioPlayerManager playerManager, PlayerOutcomeListener listener) {
        this.session       = session;
        this.lavaPlayer    = lavaPlayer;
        this.playerManager = playerManager;
        this.listener      = listener;
        this.lavaPlayer.addListener(new LavalinkEventHandler(this));
    }

    @Override
    public PlayerOutcome play(Track track) {
        if (state.get() == PlayerState.STOPPED || state.get() == PlayerState.PAUSED) {
            return playNow(track);
        }

        scheduler.enqueue(track);

        if (state.get() == PlayerState.STOPPED || state.get() == PlayerState.LOADING) {
            return skip();
        }

        gotoState(state.get(), PlayerOutcome.ADDED_TO_QUEUE, false);
        return PlayerOutcome.ADDED_TO_QUEUE;
    }

    @Override
    public PlayerOutcome playNow(Track track) {
        scheduler.addNext(track);
        return skip();
    }

    @Override
    public PlayerOutcome skip() {
        if (scheduler.peekNext().isEmpty()) {
            gotoState(state.get(), PlayerOutcome.NO_NEXT_TRACK, state.get() != PlayerState.PLAYING);
            return PlayerOutcome.NO_NEXT_TRACK;
        }
        return playNextInQueue();
    }

    @Override
    public PlayerOutcome previous() {
        Optional<Track> prev = scheduler.peekPrevious();
        if (prev.isEmpty()) {
            gotoState(state.get(), PlayerOutcome.NO_NEXT_TRACK, state.get() != PlayerState.PLAYING);
            return PlayerOutcome.NO_PREVIOUS_TRACK;
        }


        lavaPlayer.stopTrack();
        gotoState(PlayerState.LOADING, PlayerOutcome.PLAYING_PREVIOUS, false);
        scheduler.previous();
        playTrack(prev.get());
        return PlayerOutcome.PLAYING_PREVIOUS;
    }

    @Override
    public PlayerOutcome pause() {
        if (state.get() == PlayerState.PAUSED) {
            gotoState(state.get(), PlayerOutcome.ALREADY_PAUSED, true);
            return PlayerOutcome.ALREADY_PAUSED;
        }
        gotoState(PlayerState.PAUSED, PlayerOutcome.PAUSED, true);
        return PlayerOutcome.PAUSED;
    }

    @Override
    public PlayerOutcome resume() {

        if (state.get() == PlayerState.PLAYING) {
            gotoState(state.get(), PlayerOutcome.ALREADY_PLAYING, false);
            return PlayerOutcome.ALREADY_PLAYING;
        }

        if (state.get() == PlayerState.STOPPED) {
            Optional<Track> lastTrack = scheduler.getCurrentTrack();
            if (lastTrack.isPresent()) {
                playTrack(lastTrack.get());
                gotoState(PlayerState.PLAYING, PlayerOutcome.RESUMED, false);
                return PlayerOutcome.RESUMED;
            } else {
                gotoState(state.get(), PlayerOutcome.QUEUE_EMPTY, false);
                return PlayerOutcome.QUEUE_EMPTY;
            }
        }

        gotoState(PlayerState.PLAYING, PlayerOutcome.RESUMED, false);
        return PlayerOutcome.RESUMED;
    }

    @Override
    public PlayerOutcome stop() {
        if (state.get() == PlayerState.STOPPED) {
            gotoState(state.get(), PlayerOutcome.ALREADY_STOPPED, false);
            return PlayerOutcome.ALREADY_STOPPED;
        }
        scheduler.clearAll();
        lavaPlayer.stopTrack();
        gotoState(PlayerState.STOPPED, PlayerOutcome.STOPPED, false);
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
    public PlayerState getState() { return state.get(); }

    @Override
    public long getTrackPosition() {
        AudioTrack t = lavaPlayer.getPlayingTrack();
        return t == null ? 0L : t.getPosition();
    }

    @Override
    public Session getSession() { return session; }

    private void playTrack(Track track) {
        gotoState(PlayerState.LOADING, null, false);
        playerManager.loadItem(track.identifier(), new AudioLoadResultHandler() {
            @Override public void trackLoaded(AudioTrack loaded) { lavaPlayer.playTrack(loaded); }
            @Override public void playlistLoaded(AudioPlaylist pl) { lavaPlayer.playTrack(pl.getTracks().getFirst()); }
            @Override public void noMatches() { log.warn("[{}] No match for '{}'", session.id(), track.identifier()); onTrackEnd(); }
            @Override public void loadFailed(FriendlyException ex) { log.error("[{}] Load failed '{}': {}", session.id(), track.identifier(), ex.getMessage()); onTrackEnd(); }
        });
    }

    void onTrackEnd() {
        if (state.get() != PlayerState.LOADING) {
            playNextInQueue();
        }
    }

    private void onQueueEnd() {
        lavaPlayer.stopTrack();
        gotoState(PlayerState.STOPPED, PlayerOutcome.NO_NEXT_TRACK, false);
    }

    void gotoState(PlayerState newState, PlayerOutcome outcome, boolean pause) {
        state.set(newState);
        lavaPlayer.setPaused(pause);
        notify(outcome);
    }

    private void notify(PlayerOutcome outcome) {
        if (listener != null && outcome != null) {
            listener.onOutcome(session.id(), state.get(), outcome);
        }
    }

    private PlayerOutcome playNextInQueue() {

        Optional<Track> nextTrack = scheduler.next();
        if (nextTrack.isPresent()) {
            lavaPlayer.stopTrack();
            gotoState(PlayerState.LOADING, PlayerOutcome.SKIPPED, false);

            playTrack(nextTrack.get());
            return PlayerOutcome.SKIPPED;
        } else {
            onQueueEnd();
            return PlayerOutcome.NO_NEXT_TRACK;
        }
    }
}
