package com.gammatunes.component.audio.lavalink;

import com.gammatunes.component.audio.core.Player;
import com.gammatunes.model.domain.PlayerState;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackException;
import dev.arbjerg.lavalink.protocol.v4.Message;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Processes player events for a specific Player instance.
 * Handles track start, updates, end, exceptions, and stuck events.
 * Each method returns a Mono<Void> to indicate completion of the operation.
 */
@Slf4j
public record PlayerEventProcessor(Player player) {

    /**
     * Called when a track starts playing.
     * Updates the player state to PLAYING and resets the position.
     *
     * @param track The track that started playing.
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> onTrackStart(Track track) {
        player.updateState(PlayerState.PLAYING);
        player.updatePosition(0L);
        return Mono.empty();
    }

    /**
     * Called when the player updates its position.
     * Updates the player's current position in milliseconds.
     *
     * @param positionMillis The current position in milliseconds.
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> onPlayerUpdate(long positionMillis) {
        player.updatePosition(positionMillis);
        return Mono.empty();
    }

    /**
     * Called when a track ends.
     * Handles different end reasons: FINISHED, REPLACED, STOPPED, and LOAD_FAILED.
     * If FINISHED and repeat is enabled, replays the current track.
     * If REPLACED, does nothing. If STOPPED, updates the state to STOPPED.
     * If LOAD_FAILED, attempts to play the next track or become idle.
     *
     * @param track      The track that ended.
     * @param endReason  The reason why the track ended.
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> onTrackEnd(
        Track track,
        Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason
    ) {
        long guildId = player.getGuildId();
        log.debug("Track ended in guild {} with reason {}", guildId, endReason);

        return switch (endReason) {
            case FINISHED -> {
                if (player.isRepeatEnabled()) {
                    yield player.replayCurrent().onErrorResume(e -> {
                        log.warn("Replay failed in guild {}: {}", guildId, e.toString());
                        return Mono.empty();
                    });
                } else {
                    yield player.playNextOrBecomeIdle().onErrorResume(e -> {
                        log.warn("Advance failed in guild {}: {}", guildId, e.toString());
                        return Mono.empty();
                    });
                }
            }
            case REPLACED ->
                Mono.empty();
            case STOPPED -> {
                player.updateState(PlayerState.STOPPED);
                yield Mono.empty();
            }
            case LOAD_FAILED -> player.playNextOrBecomeIdle().onErrorResume(e -> {
                log.warn("Advance after LOAD_FAILED failed in guild {}: {}", guildId, e.toString());
                return Mono.empty();
            });
            default -> Mono.empty();
        };
    }

    /**
     * Called when a track encounters an exception.
     * Logs the exception and attempts to play the next track or become idle.
     *
     * @param track      The track that encountered an exception.
     * @param exception  The exception that occurred.
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> onTrackException(Track track, TrackException exception) {
        long guildId = player.getGuildId();
        log.warn("TrackException in guild {} for {}: {}", guildId,
            track.getInfo().getIdentifier(), exception.getMessage());
        return player.playNextOrBecomeIdle().onErrorResume(e -> {
            log.warn("Advance after exception failed in guild {}: {}", guildId, e.toString());
            return Mono.empty();
        });
    }

    /**
     * Called when a track gets stuck.
     * Logs the event and attempts to play the next track or become idle.
     *
     * @param track        The track that got stuck.
     * @param thresholdMs  The threshold in milliseconds after which the track is considered stuck.
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> onTrackStuck(Track track, long thresholdMs) {
        long guildId = player.getGuildId();
        log.warn("TrackStuck in guild {} after {} ms for {}", guildId, thresholdMs,
            track.getInfo().getIdentifier());
        return player.playNextOrBecomeIdle().onErrorResume(e -> {
            log.warn("Advance after stuck failed in guild {}: {}", guildId, e.toString());
            return Mono.empty();
        });
    }
}
