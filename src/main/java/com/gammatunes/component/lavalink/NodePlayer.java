package com.gammatunes.component.lavalink;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


/* This service is used to control the player on the Lavalink node.
 * It provides methods to play, stop, pause, and change the volume of the player.
 * It is bound to a specific guild, meaning it operates on the player associated with that guild.
 * The methods return a Mono<Void> to indicate completion of the operation.
 */
@Slf4j
public record NodePlayer(LavalinkClient lavalink, long guildId) {


    public Mono<Void> play(Track track, int volume) {
        return link().createOrUpdatePlayer()
            .setTrack(track)
            .setVolume(volume).then();
    }

    /**
     * Stops the player and clears the current track.
     *
     * @return A Mono that completes when the stop operation is done.
     */
    public Mono<Void> stop() {
        return link().createOrUpdatePlayer().setTrack(null).then();
    }

    /**
     * Pauses or resumes the player.
     *
     * @param shouldPause true to pause, false to resume.
     * @return A Mono that completes when the operation is done.
     */
    public Mono<Void> pause(boolean shouldPause) {
        return link().createOrUpdatePlayer().setPaused(shouldPause).then();
    }

    /**
     * Skips to the next track in the queue.
     *
     * @return A Mono that completes when the skip operation is done.
     */
    public Mono<Void> volume(int volume) {
        return link().createOrUpdatePlayer().setVolume(volume).then();
    }

    /**
     * Gets the current player state.
     *
     * @return A Mono that emits the current player state.
     */
    private Link link() {
        return lavalink.getOrCreateLink(guildId);
    }
}


