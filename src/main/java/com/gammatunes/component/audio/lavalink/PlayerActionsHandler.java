package com.gammatunes.component.audio.lavalink;

import com.gammatunes.component.lavalink.NodePlayer;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handles player actions such as play, stop, pause, and resume for a specific NodePlayer.
 * Each method returns a Mono<Void> to indicate completion of the operation.
 */
@Slf4j
public record PlayerActionsHandler(NodePlayer nodePlayer) {

    /**
     * Plays a track with the specified volume.
     *
     * @param track  The track to play.
     * @param volume The volume level to set (0-100).
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> playTrack(Track track, int volume) {
        long guildId = nodePlayer.guildId();
        return nodePlayer.play(track, volume)
            .onErrorResume(ex -> {
                log.error("Error playing track for guild {}", guildId, ex);
                return Mono.empty();
            });
    }

    /**
     * Stops the currently playing track and clears the queue.
     *
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> stopTrack() {
        long guildId = nodePlayer.guildId();
        return nodePlayer.stop()
            .onErrorResume(ex -> {
                log.error("Error stopping track for guild {}", guildId, ex);
                return Mono.empty();
            });
    }

    /**
     * Pauses the currently playing track.
     *
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> pauseTrack() {
        long guildId = nodePlayer.guildId();
        return nodePlayer.pause(true)
            .onErrorResume(ex -> {
                log.error("Error pausing track for guild {}", guildId, ex);
                return Mono.empty();
            });
    }

    /**
     * Resumes the currently paused track.
     *
     * @return A Mono<Void> indicating completion of the operation.
     */
    public Mono<Void> resumeTrack() {
        long guildId = nodePlayer.guildId();
        return nodePlayer.pause(false)
            .onErrorResume(ex -> {
                log.error("Error resuming track for guild {}", guildId, ex);
                return Mono.empty();
            });
    }
}
