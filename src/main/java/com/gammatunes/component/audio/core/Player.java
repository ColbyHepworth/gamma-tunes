package com.gammatunes.component.audio.core;

import com.gammatunes.component.audio.lavalink.PlayerActionsHandler;
import com.gammatunes.component.audio.lavalink.PlayerEventProcessor;
import com.gammatunes.component.audio.queue.TrackScheduler;
import com.gammatunes.component.lavalink.NodePlayer;
import com.gammatunes.component.audio.events.PlayerUIState;
import com.gammatunes.component.audio.events.PlayerPosition;
import com.gammatunes.model.domain.PlayerState;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * Represents a single audio player instance for a guild.
 * Manages playback state, track scheduling, and UI state updates.
 */
@Slf4j
public class Player {

    @Getter
    private final long guildId;

    private final PlayerStateStore stateStore;
    private final TrackScheduler trackScheduler = new TrackScheduler();
    private final PlayerActionsHandler playerActionsHandler;

    @Getter
    private final PlayerEventProcessor eventHandler;

    @Getter
    private volatile PlayerState state = PlayerState.STOPPED;
    private volatile boolean repeat;
    @Getter
    private volatile long positionMs = 0L;
    @Getter
    private volatile int volume = 100;

    /**
     * Creates a new Player instance for the given NodePlayer and state store.
     *
     * @param nodePlayer The NodePlayer instance to control playback.
     * @param stateStore The PlayerStateStore to manage UI state and positions.
     */
    public Player(NodePlayer nodePlayer, PlayerStateStore stateStore) {
        this.guildId = nodePlayer.guildId();
        this.stateStore = stateStore;

        this.playerActionsHandler = new PlayerActionsHandler(nodePlayer);
        this.eventHandler  = new PlayerEventProcessor(this);

        log.info("Player created for guild {}", this.guildId);
        publishStatus();
    }

    /**
     * Initializes the player by subscribing to state updates.
     * This should be called after the player is created.
     */
    private Mono<Void> playCurrentTrack() {
        log.debug("Playing current track for guild {}: state={}", guildId, getState());
        Track track = trackScheduler.getCurrentTrack()
            .orElseThrow(() -> new IllegalStateException("No track to play"));

        return playerActionsHandler.playTrack(track, volume);
    }

    /**
     * Advances to the next track or becomes idle if no tracks are available.
     * This method is called when a track ends or is skipped.
     *
     * @return A Mono that completes when the next track starts playing, or empty if idle.
     */
    public Mono<Void> playNextOrBecomeIdle() {
        log.debug("Advancing to next track for guild {}: state={}", guildId, getState());
        if (trackScheduler.next().isPresent()) {
            return playCurrentTrack();
        }
        publishUIState();
        return Mono.empty();
    }

    /**
     * Starts playback of the current track, if available.
     * This method is called when a track starts playing.
     *
     * @return A Mono that completes when the track starts playing.
     */
    public Mono<Void> replayCurrent() {
        if (trackScheduler.getCurrentTrack().isEmpty()) return Mono.empty();
        log.debug("Replaying current track for guild {}", guildId);
        return playCurrentTrack();
    }

    /**
     * Plays the specified track, either by starting playback or enqueuing it.
     * If the player is stopped or paused, it will start playback immediately.
     * If already playing, it will enqueue the track for later playback.
     *
     * @param track The track to play.
     * @return A Mono that completes when the play request is processed.
     */
    public Mono<Void> play(Track track) {
        log.debug("Request to play track: {} (state={}) for guild {}", track.getInfo().getTitle(), getState(), guildId);

        if (getState() == PlayerState.STOPPED || getState() == PlayerState.PAUSED) {
            trackScheduler.push(track);
            trackScheduler.next();
            return playCurrentTrack();
        }

        trackScheduler.enqueue(track);
        publishUIState();
        return Mono.empty();
    }

    /**
     * Immediately plays the specified track, skipping any current playback.
     * This is used for commands like "play now" that require immediate action.
     *
     * @param track The track to play immediately.
     * @return A Mono that completes when the play request is processed.
     */
    public Mono<Void> playNow(Track track) {
        log.debug("Playing track immediately: {}", track.getInfo().getTitle());
        trackScheduler.push(track);
        publishUIState();
        return skip();
    }

    /**
     * Stops playback and clears the track queue.
     * This method is called when the player is stopped or reset.
     *
     * @return A Mono that completes when the stop action is processed.
     */
    public Mono<Void> stop() {
        log.debug("Stopping playback (state={}) for guild {}", getState(), guildId);

        trackScheduler.clearAll();
        publishUIState();

        return playerActionsHandler.stopTrack();
    }

    /**
     * Skips the current track and plays the next one in the queue.
     * If no next track is available, it leaves playback as-is.
     *
     * @return A Mono that completes when the skip action is processed.
     */
    public Mono<Void> skip() {
        log.debug("Skipping track (state={}): guild={}", getState(), guildId);

        if (trackScheduler.next().isPresent()) {
            return playCurrentTrack();
        }

        log.debug("No next track available; leaving current playback as-is.");
        publishUIState();
        return Mono.empty();
    }

    /**
     * Goes back to the previous track in the queue, if available.
     * If no previous track exists, it leaves playback as-is.
     *
     * @return A Mono that completes when the previous action is processed.
     */
    public Mono<Void> previous() {
        log.debug("Going back to previous track (state={}): guild={}", getState(), guildId);
        if (trackScheduler.previous().isPresent()) {
            return playCurrentTrack();
        }
        log.debug("No previous track available; cannot go back.");
        publishUIState();
        return Mono.empty();
    }

    /**
     * Pauses the current playback.
     * If already paused, it does nothing and returns immediately.
     *
     * @return A Mono that completes when the pause action is processed.
     */
    public Mono<Void> pause() {
        log.debug("Pausing playback (state={}): guild={}", getState(), guildId);
        if (getState() == PlayerState.PAUSED) {
            publishUIState();
            return Mono.empty();
        }

        PlayerState previousState = this.state;
        updateState(PlayerState.PAUSED);

        return playerActionsHandler.pauseTrack()
            .onErrorResume(e -> {
                log.warn("Pause failed for guild {}, rolling back: {}", guildId, e.toString());
                updateState(previousState);
                return Mono.error(e);
            });
    }

    /**
     * Resumes playback if currently paused.
     * If already playing, it does nothing and returns immediately.
     *
     * @return A Mono that completes when the resume action is processed.
     */
    public Mono<Void> resume() {
        if (getState() == PlayerState.PLAYING) {
            publishUIState();
            return Mono.empty();
        }
        PlayerState previousState = this.state;
        updateState(PlayerState.PLAYING);

        return playerActionsHandler.resumeTrack()
            .onErrorResume(e -> {
                log.warn("Resume failed for guild {}, rolling back: {}", guildId, e.toString());
                updateState(previousState);
                return Mono.error(e);
            });
    }

    /**
     * Jumps to a specific track by its identifier.
     * If the track is found, it starts playback of that track.
     * If not found, it logs a warning and publishes the current UI state.
     *
     * @param trackIdentifier The identifier of the track to jump to.
     * @return A Mono that completes when the jump action is processed.
     */
    public Mono<Void> jumpToTrack(String trackIdentifier) {
        log.debug("Jumping to track by identifier: {}", trackIdentifier);
        if (trackScheduler.jumpToPrefixedIdentifier(trackIdentifier).isPresent()) {
            return playCurrentTrack();
        }
        log.warn("Track identifier not found: {}", trackIdentifier);
        publishUIState();
        return Mono.empty();
    }

    /**
     * Shuffles the current track queue.
     * This will randomize the order of tracks in the queue.
     */
    public void shuffle() {
        log.debug("Shuffling track queue for guild {}", guildId);
        trackScheduler.shuffle();
        publishUIState();
    }

    /**
     * Toggles the repeat mode for the player.
     * If repeat is enabled, it will replay the current track when it ends.
     * If disabled, it will play the next track in the queue.
     */
    public synchronized void toggleRepeat() {
        log.debug("Toggling repeat mode for guild {}: current state={}", guildId, repeat);
        this.repeat = !this.repeat;
        publishUIState();
    }


    public boolean isRepeatEnabled() {
        return repeat;
    }


    public List<Track> getQueue() {
        return trackScheduler.getQueue();
    }

    /**
     * Updates the player state and resets position if stopped.
     * This method is called when the player state changes (e.g., to PAUSED, PLAYING, or STOPPED).
     *
     * @param newState The new state to set for the player.
     */
    public void updateState(PlayerState newState) {
        log.debug("Updating state for guild {}", guildId);
        this.state = newState;
        if (newState == PlayerState.STOPPED) {
            this.positionMs = 0L;
        }
        publishUIState();
        publishPosition();
    }

    /**
     * Updates the playback position in milliseconds.
     * This method is called periodically to update the current playback position.
     *
     * @param positionMs The new position in milliseconds.
     */
    public void updatePosition(long positionMs) {
        this.positionMs = positionMs;
        publishPosition();
    }

    /**
     * Publishes the current UI state to the state store.
     * This includes the guild ID, player state, volume, repeat mode,
     * current track, queue, and history.
     * This method is called whenever the player state changes.
     * It ensures that the UI is kept in sync with the current playback state.
     */
    private void publishUIState() {
        log.debug("Publishing UI state for guild {}: state={}, volume={}, repeat={}", guildId, state, volume, repeat);
        PlayerUIState uiState = new PlayerUIState(
            guildId,
            state,
            volume,
            repeat,
            trackScheduler.getCurrentTrack().orElse(null),
            List.copyOf(trackScheduler.getQueue()),
            List.copyOf(trackScheduler.getHistory())
        );
        stateStore.setUIState(uiState);
    }

    /**
     * Publishes the current playback position to the state store.
     * This includes the guild ID, current position in milliseconds,
     * and the length of the current track.
     * This method is called periodically to keep the position updated.
     */
    private void publishPosition() {
        log.debug("Publishing position for guild {}: positionMs={}", guildId, positionMs);
        long lengthMs = trackScheduler.getCurrentTrack()
            .map(t -> t.getInfo().getLength())
            .orElse(0L);

        PlayerPosition position = new PlayerPosition(
            guildId,
            positionMs,
            lengthMs
        );
        stateStore.setPosition(position);
    }

    /**
     * Publishes both the UI state and position updates.
     * This method is called whenever there are changes to the player state
     * or playback position, ensuring that both are kept in sync.
     */
    private void publishStatus() {
        log.debug("Publishing status for guild {}: state={}, positionMs={}", guildId, state, positionMs);
        publishUIState();
        publishPosition();
    }
}
