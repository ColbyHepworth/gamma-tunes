package com.gammatunes.service;

import com.gammatunes.exception.player.MemberNotInVoiceChannelException;
import com.gammatunes.model.dto.RequesterInfo;
import com.gammatunes.service.playback.PlaybackMode;
import com.gammatunes.service.playback.PlaybackRequestFactory;
import com.gammatunes.service.playback.PlaybackService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service for managing player interactions in Discord.
 * Provides methods to control playback, manage voice connections, and handle player state.
 */
@Service
@RequiredArgsConstructor
public class DiscordPlayerService {

    private final PlayInputResolverService playInputResolverService;
    private final PlaybackService playbackService;
    private final PlaybackRequestFactory playbackRequestFactory;

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
        return Mono.defer(() -> {
            AudioChannel audioChannel = getAudioChannelOrThrow(member);
            long guildId = member.getGuild().getIdLong();
            RequesterInfo requesterInfo = RequesterInfo.fromMember(member);

            return playInputResolverService.resolveAll(member.getIdLong(), query)
                .flatMap(tracks -> playbackService.play(playbackRequestFactory.create(
                    guildId,
                    audioChannel.getIdLong(),
                    textChannel,
                    requesterInfo,
                    tracks,
                    PlaybackMode.QUEUE
                )));
        });
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
        return Mono.defer(() -> {
            AudioChannel audioChannel = getAudioChannelOrThrow(member);
            long guildId = member.getGuild().getIdLong();
            RequesterInfo requesterInfo = RequesterInfo.fromMember(member);

            return playInputResolverService.resolveOne(member.getIdLong(), query)
                .flatMap(track -> playbackService.play(playbackRequestFactory.create(
                    guildId,
                    audioChannel.getIdLong(),
                    textChannel,
                    requesterInfo,
                    List.of(track),
                    PlaybackMode.PLAY_NOW
                )));
        });
    }

    /**
     * Jumps to a specific track in the queue for the specified member.
     *
     * @param member The member who requested to jump to a track.
     * @param trackIdentifier The identifier of the track to jump to.
     * @return A Mono that completes when the jump operation is done.
     */
    public Mono<Void> jumpToTrack(Member member, String trackIdentifier) {
        long guildId = member.getGuild().getIdLong();
        return playbackService.jumpToTrack(guildId, trackIdentifier);
    }

    /**
     * Pauses the player for the specified member.
     *
     * @param member The member who requested to pause the player.
     * @return A Mono that completes when the player is paused.
     */
    public Mono<Void> pause(Member member) {
        long guildId = member.getGuild().getIdLong();
        return pause(guildId);
    }

    public Mono<Void> pause(long guildId) {
        return playbackService.pause(guildId);
    }

    /**
     * Resumes the player for the specified member.
     *
     * @param member The member who requested to resume the player.
     * @return A Mono that completes when the player is resumed.
     */
    public Mono<Void> resume(Member member) {
        long guildId = member.getGuild().getIdLong();
        return resume(guildId);
    }

    public Mono<Void> resume(long guildId) {
        return playbackService.resume(guildId);
    }

    /**
     * Skips the current track for the specified member.
     *
     * @param member The member who requested to skip the track.
     * @return A Mono that completes when the skip operation is done.
     */
    public Mono<Void> skip(Member member) {
        long guildId = member.getGuild().getIdLong();
        return skip(guildId);
    }

    public Mono<Void> skip(long guildId) {
        return playbackService.skip(guildId);
    }

    /**
     * Goes back to the previous track for the specified member.
     *
     * @param member The member who requested to go back to the previous track.
     * @return A Mono that completes when the previous track operation is done.
     */
    public Mono<Void> previous(Member member) {
        long guildId = member.getGuild().getIdLong();
        return previous(guildId);
    }

    public Mono<Void> previous(long guildId) {
        return playbackService.previous(guildId);
    }

    /**
     * Shuffles the current queue for the specified member.
     *
     * @param member The member who requested to shuffle the queue.
     * @return A Mono that completes when the shuffle operation is done.
     */
    public Mono<Void> shuffle(Member member) {
        long guildId = member.getGuild().getIdLong();
        return playbackService.shuffle(guildId);
    }

    /**
     * Toggles the repeat mode for the player for the specified member.
     *
     * @param member The member who requested to toggle repeat mode.
     * @return A Mono that completes when the repeat mode is toggled.
     */
    public Mono<Void> toggleRepeat(Member member) {
        long guildId = member.getGuild().getIdLong();
        return playbackService.toggleRepeat(guildId);
    }

    /**
     * Gets the current repeat state for the player for the specified member.
     *
     * @param member The member whose repeat state is requested.
     * @return A Mono that emits the current repeat state (true if repeat is enabled, false otherwise).
     */
    public Mono<Boolean> getRepeat(Member member) {
        long guildId = member.getGuild().getIdLong();
        return playbackService.getRepeat(guildId);
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
        return stop(guildId);
    }

    public Mono<Void> stop(long guildId) {
        return playbackService.stop(guildId);
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
