package com.gammatunes.component.audio.interaction;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gammatunes.component.audio.core.Player;
import com.gammatunes.component.audio.core.PlayerRegistry;
import com.gammatunes.model.dto.RequesterInfo;
import com.gammatunes.service.TrackQueryService;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * This orchestrator handles player interactions such as play, pause, resume, stop, etc.
 * It uses PlayerRegistry to manage players and TrackQueryService to resolve track queries.
 * Each method returns a Mono<Void> to indicate completion of the operation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerInteractionOrchestrator {

    private final PlayerRegistry playerRegistry;
    private final TrackQueryService trackQueryService;

    /**
     * Plays a track for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @param query The track query to resolve.
     * @param requesterInfo Information about the requester.
     * @return A Mono that completes when the track is played.
     */
    public Mono<Void> play(long guildId, String query, RequesterInfo requesterInfo) {
        log.debug("Playing track for guild {}: {}", guildId, query);
        return trackQueryService.resolve(query)
            .map(track -> attachRequester(track, requesterInfo))
            .flatMap(track -> playerRegistry.getOrCreate(guildId)
                .flatMap(player -> player.play(track)))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> log.error("Error in play command for guild {}", guildId, error))
            .doOnSuccess(ignored -> log.debug("Play command for guild {} completed.", guildId));
    }

    /**
     * Plays a track immediately for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @param query The track query to resolve.
     * @param requesterInfo Information about the requester.
     * @return A Mono that completes when the track is played immediately.
     */
    public Mono<Void> playNow(long guildId, String query, RequesterInfo requesterInfo) {
        log.debug("Playing track immediately for guild {}: {}", guildId, query);
        return trackQueryService.resolve(query)
            .map(track -> attachRequester(track, requesterInfo))
            .flatMap(track -> playerRegistry.getOrCreate(guildId)
                .flatMap(player -> player.playNow(track)))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> log.error("Error in playNow command for guild {}", guildId, error))
            .doOnSuccess(ignored -> log.debug("PlayNow command for guild {} completed.", guildId));
    }

    /**
     * Jumps to an existing track for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @param trackIdentifier The identifier of the track to jump to.
     * @return A Mono that completes when the jump is successful.
     */
    public Mono<Void> jumpToTrack(long guildId, String trackIdentifier) {
        log.debug("Jumping to existing track for guild {}: {}", guildId, trackIdentifier);
        return playerRegistry.getOrCreate(guildId)
            .flatMap(player -> player.jumpToTrack(trackIdentifier))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> log.error("Error in jumpToTrack command for guild {}", guildId, error))
            .doOnSuccess(ignored -> log.debug("JumpToTrack command for guild {} completed.", guildId));
    }

    /**
     * Stops the player for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @return A Mono that completes when the player is stopped.
     */
    public Mono<Void> stop(long guildId) {
        log.debug("Stopping player for guild {}", guildId);
        return playerRegistry.getOrCreate(guildId)
            .flatMap(Player::stop)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> log.error("Error in stop command for guild {}", guildId, error))
            .doOnSuccess(success -> log.debug("Stop command for guild {} completed.", guildId));
    }

    /**
     * Pauses the player for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @return A Mono that completes when the player is paused.
     */
    public Mono<Void> pause(long guildId) {
        log.debug("Pausing player for guild {}", guildId);
        return playerRegistry.getOrCreate(guildId)
            .flatMap(Player::pause)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> log.error("Error in pause command for guild {}", guildId, error))
            .doOnSuccess(success -> log.debug("Pause command for guild {} completed.", guildId));
    }

    /**
     * Resumes the player for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @return A Mono that completes when the player is resumed.
     */
    public Mono<Void> resume(long guildId) {
        log.debug("Resuming player for guild {}", guildId);
        return playerRegistry.getOrCreate(guildId)
            .flatMap(Player::resume)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> log.error("Error in resume command for guild {}", guildId, error))
            .doOnSuccess(success -> log.debug("Resume command for guild {} completed.", guildId));
    }

    /**
     * Skips the current track for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @return A Mono that completes when the track is skipped.
     */
    public Mono<Void> skip(long guildId) {
        log.debug("Skipping track for guild {}", guildId);
        return playerRegistry.getOrCreate(guildId)
            .flatMap(Player::skip)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> log.error("Error in skip command for guild {}", guildId, error))
            .doOnSuccess(success -> log.debug("Skip command for guild {} completed.", guildId));
    }

    /**
     * Plays the previous track for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @return A Mono that completes when the previous track is played.
     */
    public Mono<Void> previous(long guildId) {
        log.debug("Playing previous track for guild {}", guildId);
        return playerRegistry.getOrCreate(guildId)
            .flatMap(Player::previous)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> log.error("Error in previous command for guild {}", guildId, error))
            .doOnSuccess(success -> log.debug("Previous command for guild {} completed.", guildId));
    }

    /**
     * Shuffles the current queue for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @return A Mono that completes when the queue is shuffled.
     */
    public Mono<Void> shuffle(long guildId) {
        return playerRegistry.getOrCreate(guildId)
            .flatMap(player -> Mono.fromRunnable(player::shuffle))
            .subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Toggles the repeat mode for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @return A Mono that completes with the new repeat state.
     */
    public Mono<Boolean> toggleRepeat(long guildId) {
        return playerRegistry.getOrCreate(guildId)
            .map(player -> { player.toggleRepeat(); return player.isRepeatEnabled(); })
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets the current repeat state for the specified guild.
     *
     * @param guildId The ID of the guild.
     * @return A Mono that completes with the repeat state.
     */
    public Mono<Boolean> getRepeat(long guildId) {
        return playerRegistry.getOrCreate(guildId)
            .map(Player::isRepeatEnabled)
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Attaches requester information to the track.
     *
     * @param track The track to attach the requester info to.
     * @param requesterInfo The requester information.
     * @return The track with attached requester info.
     */
    private Track attachRequester(Track track, RequesterInfo requesterInfo) {
        if (requesterInfo == null) {
            return track;
        }
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("userId", requesterInfo.userId());
        node.put("displayName", requesterInfo.displayName());
        if (requesterInfo.avatarUrl() != null) {
            node.put("avatarUrl", requesterInfo.avatarUrl());
        }
        track.setUserData(node);
        return track;
    }
}
