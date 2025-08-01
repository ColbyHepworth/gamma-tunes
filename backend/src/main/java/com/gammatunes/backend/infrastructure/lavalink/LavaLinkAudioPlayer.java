package com.gammatunes.backend.infrastructure.lavalink;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.model.QueueItem;
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
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class adapts the Lavaplayer's AudioPlayer to the GammaTunes AudioPlayer interface.
 * It manages playback, queueing, and state transitions for audio tracks in a session.
 * It also handles events from Lavaplayer to update the player state accordingly.
 */
@Slf4j
public class LavaLinkAudioPlayer implements AudioPlayer {


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
    public PlayerOutcome play(QueueItem item) {
        if (state.get() == PlayerState.STOPPED || state.get() == PlayerState.PAUSED) {
            log.debug("[{}] Starting track: {}", session.id(), item.track().identifier());
            return playNow(item);
        }

        log.debug("[{}] Adding track to queue: {}", session.id(), item.track().identifier());
        scheduler.enqueue(item);
        gotoState(state.get(), PlayerOutcome.ADDED_TO_QUEUE, false);
        return PlayerOutcome.ADDED_TO_QUEUE;
    }

    @Override
    public PlayerOutcome playNow(QueueItem item) {
        log.debug("[{}] Starting track now: {}", session.id(), item.track().identifier());
        scheduler.addNext(item);
        return skip();
    }

    @Override
    public PlayerOutcome repeat() {
        log.debug("[{}] Starting repeating", session.id());
        gotoState(state.get(), PlayerOutcome.REPEATED, false);
        return PlayerOutcome.REPEATED;
    }

    @Override
    public PlayerOutcome toggleRepeat() {
        boolean repeatEnabled = scheduler.toggleRepeat();
        boolean isCurrentlyPaused = (state.get() == PlayerState.PAUSED);

        if (repeatEnabled) {
            log.debug("[{}] Toggling repeat mode on", session.id());
            gotoState(state.get(), PlayerOutcome.REPEAT_ENABLED, isCurrentlyPaused);
            return PlayerOutcome.REPEAT_ENABLED;
        } else {
            log.debug("[{}] Toggling repeat mode off", session.id());
            gotoState(state.get(), PlayerOutcome.REPEAT_DISABLED, isCurrentlyPaused);
            return PlayerOutcome.REPEAT_DISABLED;
        }
    }

    @Override
    public PlayerOutcome skip() {
        if (scheduler.peekNext().isEmpty()) {
            log.debug("[{}] No next track to skip to", session.id());
            gotoState(state.get(), PlayerOutcome.NO_NEXT_TRACK, state.get() != PlayerState.PLAYING);
            return PlayerOutcome.NO_NEXT_TRACK;
        }
        log.debug("[{}] Skipping to next track", session.id());
        return playNextInQueue();
    }

    @Override
    public PlayerOutcome previous() {
        Optional<QueueItem> prev = scheduler.peekPrevious();
        if (prev.isEmpty()) {
            log.debug("[{}] No previous track to play", session.id());
            gotoState(state.get(), PlayerOutcome.NO_PREVIOUS_TRACK, state.get() != PlayerState.PLAYING);
            return PlayerOutcome.NO_PREVIOUS_TRACK;
        }

        lavaPlayer.stopTrack();
        gotoState(PlayerState.LOADING, PlayerOutcome.PLAYING_PREVIOUS, false);
        scheduler.previous();
        log.debug("[{}] Playing previous track: {}", session.id(), prev.get().track().identifier());
        playTrack(prev.get().track());
        return PlayerOutcome.PLAYING_PREVIOUS;
    }

    @Override
    public PlayerOutcome pause() {
        if (state.get() == PlayerState.PAUSED) {
            log.debug("[{}] Player is already paused", session.id());
            gotoState(state.get(), PlayerOutcome.ALREADY_PAUSED, true);
            return PlayerOutcome.ALREADY_PAUSED;
        }
        log.debug("[{}] Pausing player", session.id());
        gotoState(PlayerState.PAUSED, PlayerOutcome.PAUSED, true);
        return PlayerOutcome.PAUSED;
    }

    @Override
    public PlayerOutcome resume() {

        if (state.get() == PlayerState.PLAYING) {
            log.debug("[{}] Player is already playing", session.id());
            gotoState(state.get(), PlayerOutcome.ALREADY_PLAYING, false);
            return PlayerOutcome.ALREADY_PLAYING;
        }

        if (state.get() == PlayerState.STOPPED) {
            Optional<QueueItem> lastTrack = scheduler.getCurrentItem();
            if (lastTrack.isPresent()) {
                log.debug("[{}] Resuming last track: {}", session.id(), lastTrack.get().track().identifier());
                playTrack(lastTrack.get().track());
                return PlayerOutcome.RESUMED;
            } else {
                log.debug("[{}] No last track to resume", session.id());
                gotoState(state.get(), PlayerOutcome.QUEUE_EMPTY, false);
                return PlayerOutcome.QUEUE_EMPTY;
            }
        }
        log.debug("[{}] Resuming player", session.id());
        gotoState(PlayerState.PLAYING, PlayerOutcome.RESUMED, false);
        return PlayerOutcome.RESUMED;
    }

    @Override
    public PlayerOutcome stop() {
        if (state.get() == PlayerState.STOPPED) {
            log.debug("[{}] Player is already stopped", session.id());
            gotoState(state.get(), PlayerOutcome.ALREADY_STOPPED, false);
            return PlayerOutcome.ALREADY_STOPPED;
        }
        scheduler.clearAll();
        log.debug("[{}] Stopping player", session.id());
        lavaPlayer.stopTrack();
        gotoState(PlayerState.STOPPED, PlayerOutcome.STOPPED, false);
        return PlayerOutcome.STOPPED;
    }

    @Override
    public PlayerOutcome jumpToTrack(String trackIdentifier) {
        int trackIndex = scheduler.findTrackIndex(trackIdentifier);

        if (trackIndex == -1) {
            log.warn("Attempted to jump to a track that was not found: {}", trackIdentifier);
            notify(PlayerOutcome.ERROR);
            return PlayerOutcome.ERROR;
        }

        scheduler.setCurrentIndex(trackIndex - 1);
        log.debug("[{}] Jumping to track: {}", session.id(), trackIdentifier);
        return playNextInQueue();
    }

    @Override
    public PlayerOutcome shuffle() {
        log.debug("[{}] Shuffling", session.id());
        scheduler.shuffle();
        boolean isCurrentlyPaused = (state.get() == PlayerState.PAUSED);
        gotoState(state.get(), PlayerOutcome.SHUFFLED, isCurrentlyPaused);
        return PlayerOutcome.SHUFFLED;
    }

    @Override
    public void clearQueue() {
        log.debug("[{}] Clearing queue", session.id());
        scheduler.clearQueue();
    }

    @Override
    public List<QueueItem> getQueue() {
        return scheduler.getQueue();
    }

    @Override
    public List<QueueItem> getHistory() {
        return scheduler.getHistory();
    }

    @Override
    public Optional<QueueItem> getCurrentItem() {
        return scheduler.getCurrentItem();
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
        log.debug("[{}] Getting session", session.id());
        return session;
    }

    @Override
    public boolean isRepeatEnabled() {
        return scheduler.isRepeatEnabled();
    }

    private void playTrack(Track track) {
        log.debug("[{}] Loading track: {}", session.id(), track.identifier());
        gotoState(PlayerState.LOADING, null, false);
        playerManager.loadItem(track.identifier(), new AudioLoadResultHandler() {
            @Override public void trackLoaded(AudioTrack loaded) {
                log.debug("[{}] Track loaded: {}", session.id(), loaded.getIdentifier());
                lavaPlayer.playTrack(loaded);
            }
            @Override public void playlistLoaded(AudioPlaylist pl) {
                log.debug("[{}] Playlist loaded: {}", session.id(), pl.getName());
                lavaPlayer.playTrack(pl.getTracks().getFirst());
            }
            @Override public void noMatches() {
                log.warn("[{}] No match for '{}'", session.id(), track.identifier()); onTrackEnd();
            }
            @Override public void loadFailed(FriendlyException ex) {
                log.error("[{}] Load failed '{}': {}", session.id(), track.identifier(), ex.getMessage()); onTrackEnd();
            }
        });
    }

    void onTrackEnd() {
        if (state.get() != PlayerState.LOADING) {
            log.debug("[{}] Track ended, checking queue", session.id());
            playNextInQueue();
        }
    }

    private void onQueueEnd() {
        log.debug("[{}] Queue ended, stopping player", session.id());
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
            log.debug("[{}] Notifying listener of outcome: {}", session.id(), outcome);
            listener.onOutcome(session.id(), state.get(), outcome);
        } else {
            log.warn("[{}] No listener to notify or outcome is null", session.id());
        }
    }

    private PlayerOutcome playNextInQueue() {

        Optional<QueueItem> nextTrack = scheduler.next();
        if (nextTrack.isPresent()) {
            log.debug("[{}] Playing next track: {}", session.id(), nextTrack.get().track().identifier());
            lavaPlayer.stopTrack();
            gotoState(PlayerState.LOADING, PlayerOutcome.SKIPPED, false);

            playTrack(nextTrack.get().track());
            return PlayerOutcome.SKIPPED;
        } else {
            log.debug("[{}] No next track available, queue is empty", session.id());
            onQueueEnd();
            return PlayerOutcome.NO_NEXT_TRACK;
        }
    }
}
