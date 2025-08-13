package com.gammatunes.service;

import com.gammatunes.model.dto.RequesterInfo;
import com.gammatunes.component.discord.DiscordVoiceConnector;
import com.gammatunes.exception.player.MemberNotInVoiceChannelException;
import com.gammatunes.component.audio.interaction.PlayerInteractionOrchestrator;
import com.gammatunes.component.audio.core.PlayerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for managing player interactions in Discord.
 * Provides methods to control playback, manage voice connections, and handle player state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordPlayerService {

    private final PlayerInteractionOrchestrator playerControlService;
    private final DiscordVoiceConnector discordVoiceConnector;
    private final PlayerPanelService playerPanelService;
    private final PlayerRegistry playerRegistry;

    /**
     * Plays a track for the specified member in their current voice channel.
     * Connects to the voice channel if not already connected.
     *
     * @param member The member who requested to play the track.
     * @param query  The track query to resolve and play.
     * @return A Mono that completes when the track is played.
     */
    public Mono<Void> play(Member member, String query) {
        return play(member, query, null);
    }

    /**
     * Plays a track for the specified member in their current voice channel.
     * Connects to the voice channel if not already connected.
     * Automatically spawns a player panel in the specified channel if one doesn't exist.
     *
     * @param member The member who requested to play the track.
     * @param query  The track query to resolve and play.
     * @param textChannel The text channel to spawn the player panel in (if null, no panel is created)
     * @return A Mono that completes when the track is played.
     */
    public Mono<Void> play(Member member, String query, TextChannel textChannel) {
        log.debug("Playing track for member {}: {}", member.getId(), query);
        return Mono.defer(() -> {
            AudioChannel audioChannel = getAudioChannelOrThrow(member);
            long guildId = member.getGuild().getIdLong();
            RequesterInfo requesterInfo = RequesterInfo.fromMember(member);

            Mono<Void> playMono = discordVoiceConnector.connect(guildId, audioChannel.getIdLong())
                .then(playerControlService.play(guildId, query, requesterInfo));

            if (textChannel != null && playerPanelService.getMessage(guildId).isEmpty()) {
                log.debug("Creating player panel for guild {} in channel {} in play", guildId, textChannel.getId());
                return playMono
                    .then(Mono.defer(() -> playerPanelService.createPanel(guildId, textChannel)));
            }

            return playMono;
        }).name("play");
    }

    /**
     * Plays a track immediately for the specified member in their current voice channel.
     * Connects to the voice channel if not already connected.
     *
     * @param member The member who requested to play the track immediately.
     * @param query  The track query to resolve and play immediately.
     * @return A Mono that completes when the track is played immediately.
     */
    public Mono<Void> playNow(Member member, String query) {
        return playNow(member, query, null);
    }

    /**
     * Plays a track immediately for the specified member in their current voice channel.
     * Connects to the voice channel if not already connected.
     * Automatically spawns a player panel in the specified channel if one doesn't exist.
     *
     * @param member The member who requested to play the track immediately.
     * @param query  The track query to resolve and play immediately.
     * @param textChannel The text channel to spawn the player panel in (if null, no panel is created)
     * @return A Mono that completes when the track is played immediately.
     */
    public Mono<Void> playNow(Member member, String query, TextChannel textChannel) {
        log.debug("Playing track immediately for member {}: {}", member.getId(), query);
        return Mono.defer(() -> {
            AudioChannel audioChannel = getAudioChannelOrThrow(member);
            long guildId = member.getGuild().getIdLong();
            RequesterInfo requesterInfo = RequesterInfo.fromMember(member);

            Mono<Void> playNowMono = discordVoiceConnector.connect(guildId, audioChannel.getIdLong())
                .then(playerControlService.playNow(guildId, query, requesterInfo));

            if (textChannel != null && playerPanelService.getMessage(guildId).isEmpty()) {
                log.debug("Creating player panel for guild {} in channel {} in playNow", guildId, textChannel.getId());
                return playNowMono
                    .then(Mono.defer(() -> playerPanelService.createPanel(guildId, textChannel)));
            }

            return playNowMono;
        }).name("playNow");
    }

    /**
     * Jumps to a specific track in the queue for the specified member.
     *
     * @param member The member who requested to jump to a track.
     * @param trackIdentifier The identifier of the track to jump to.
     * @return A Mono that completes when the jump operation is done.
     */
    public Mono<Void> jumpToTrack(Member member, String trackIdentifier) {
        log.debug("Jumping to track {} for member {}", trackIdentifier, member.getId());
        long guildId = member.getGuild().getIdLong();
        return playerControlService.jumpToTrack(guildId, trackIdentifier)
            .name("jumpToTrack");
    }

    /**
     * Pauses the player for the specified member.
     *
     * @param member The member who requested to pause the player.
     * @return A Mono that completes when the player is paused.
     */
    public Mono<Void> pause(Member member) {
        log.debug("Pausing player for member {}", member.getId());
        long guildId = member.getGuild().getIdLong();
        return playerControlService.pause(guildId)
            .name("pause");
    }

    /**
     * Resumes the player for the specified member.
     *
     * @param member The member who requested to resume the player.
     * @return A Mono that completes when the player is resumed.
     */
    public Mono<Void> resume(Member member) {
        log.debug("Resuming player for member {}", member.getId());
        long guildId = member.getGuild().getIdLong();
        return playerControlService.resume(guildId)
            .name("resume");
    }

    /**
     * Skips the current track for the specified member.
     *
     * @param member The member who requested to skip the track.
     * @return A Mono that completes when the skip operation is done.
     */
    public Mono<Void> skip(Member member) {
        log.debug("Skipping track for member {}", member.getId());
        long guildId = member.getGuild().getIdLong();
        return playerControlService.skip(guildId)
            .name("skip");
    }

    /**
     * Goes back to the previous track for the specified member.
     *
     * @param member The member who requested to go back to the previous track.
     * @return A Mono that completes when the previous track operation is done.
     */
    public Mono<Void> previous(Member member) {
        log.debug("Going back to previous track for member {}", member.getId());
        long guildId = member.getGuild().getIdLong();
        return playerControlService.previous(guildId)
            .name("previous");
    }

    /**
     * Shuffles the current queue for the specified member.
     *
     * @param member The member who requested to shuffle the queue.
     * @return A Mono that completes when the shuffle operation is done.
     */
    public Mono<Void> shuffle(Member member) {
        log.debug("Shuffling player for member {}", member.getId());
        long guildId = member.getGuild().getIdLong();
        return playerControlService.shuffle(guildId)
            .name("shuffle");
    }

    /**
     * Toggles the repeat mode for the player for the specified member.
     *
     * @param member The member who requested to toggle repeat mode.
     * @return A Mono that completes when the repeat mode is toggled.
     */
    public Mono<Void> toggleRepeat(Member member) {
        log.debug("Toggling repeat mode for member {}", member.getId());
        long guildId = member.getGuild().getIdLong();
        return playerControlService.toggleRepeat(guildId)
            .then()
            .name("toggleRepeat");
    }

    /**
     * Gets the current repeat state for the player for the specified member.
     *
     * @param member The member whose repeat state is requested.
     * @return A Mono that emits the current repeat state (true if repeat is enabled, false otherwise).
     */
    public Mono<Boolean> getRepeat(Member member) {
        long guildId = member.getGuild().getIdLong();
        return playerControlService.getRepeat(guildId);
    }

    /**
     * Stops the player for the specified member and disconnects from the voice channel.
     * Removes the player from the registry and deletes the player panel associated with the guild.
     *
     * @param member The member who requested to stop the player.
     * @return A Mono that completes when the player is stopped and disconnected.
     */
    public Mono<Void> stop(Member member) {
        long guildId = member.getGuild().getIdLong();
        log.debug("Stopping player for member {} in guild {}", member.getId(), guildId);

        return playerControlService.stop(guildId)
            .onErrorResume(error -> {
                log.warn("Player stop failed for guild {}", guildId, error);
                return Mono.empty();
            })
            .then(playerPanelService.deletePanel(guildId).onErrorResume(error -> {
                log.warn("Panel delete failed for guild {}", guildId, error);
                return Mono.empty();
            }))
            .then(discordVoiceConnector.disconnect(guildId))
            .doFinally(signal -> {
                playerRegistry.destroy(guildId);
                log.debug("Player removed from registry for guild {}", guildId);
            })
            .name("stop");
    }

    /**
     * Retrieves the audio channel of the member, throwing an exception if the member is not in a voice channel.
     *
     * @param member The member whose voice channel is to be retrieved.
     * @return The audio channel of the member.
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel.
     */
    private AudioChannel getAudioChannelOrThrow(Member member) {
        var voiceState = member.getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null) {
            throw new MemberNotInVoiceChannelException("Member must be in a voice channel to perform this action.");
        }
        return voiceState.getChannel();
    }
}
