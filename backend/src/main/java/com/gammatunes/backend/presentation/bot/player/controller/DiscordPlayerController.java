package com.gammatunes.backend.presentation.bot.player.controller;

import com.gammatunes.backend.application.port.in.AudioControlUseCase;
import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.VoiceConnectRequest;
import com.gammatunes.backend.domain.model.VoiceDisconnectRequest;
import com.gammatunes.backend.infrastructure.source.exception.TrackLoadException;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.player.service.PlayerMessageService;
import com.gammatunes.backend.presentation.bot.voice.DiscordVoiceGateway;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



/**
 * Controller for managing audio playback in Discord voice channels.
 * This controller handles commands such as play, pause, stop, resume, skip, and previous tracks.
 * It interacts with the AudioControlUseCase to perform these actions.
 */
@Component
@RequiredArgsConstructor
public class DiscordPlayerController {

    private static final Logger logger = LoggerFactory.getLogger(DiscordPlayerController.class);

    private final AudioControlUseCase player;
    private final DiscordVoiceGateway discordVoiceGateway;
    private final PlayerMessageService playerMessageService;

    /**
     * Plays a track in the voice channel of the specified member.
     * If the member is not in a voice channel, it throws an exception.
     *
     * @param member The member whose voice channel to play the track in.
     * @param query  The track URL or search term to play.
     * @throws TrackLoadException if there is an error loading the track.
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel.
     */
    public PlayerOutcome play(Member member, String query) throws TrackLoadException, MemberNotInVoiceChannelException {
        logger.debug("Attempting to play track '{}' for member '{}'", query, member.getEffectiveName());
        AudioChannel channel = getAudioChannel(member);
        discordVoiceGateway.connect(new VoiceConnectRequest(member.getGuild().getId(), channel.getId()));
        return player.play(member.getGuild().getId(), query);
    }

    /**
     * Plays a track immediately in the voice channel of the specified member.
     * If the member is not in a voice channel, it throws an exception.
     *
     * @param member The member whose voice channel to play the track in.
     * @param query  The track URL or search term to play immediately.
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel.
     */
    public PlayerOutcome playNow(Member member, String query) throws TrackLoadException, MemberNotInVoiceChannelException {
        logger.debug("Attempting to play track '{}' immediately for member '{}'", query, member.getEffectiveName());
        AudioChannel channel = getAudioChannel(member);
        discordVoiceGateway.connect(new VoiceConnectRequest(member.getGuild().getId(), channel.getId()));
        return player.playNow(member.getGuild().getId(), query);
    }

    /**
     * Pauses the currently playing track in the voice channel of the specified member.
     * @param member The member whose voice channel to pause playback in.
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel.
     */
    public PlayerOutcome pause(Member member) throws MemberNotInVoiceChannelException {
        logger.debug("Attempting to pause playback for member '{}'", member.getEffectiveName());
        return player.pause(member.getGuild().getId());
    }

    /**
     * Stops the audio player and clears the current track.
     * This method will stop playback and clear the queue.
     *
     * @param member The member whose audio player to stop.
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel.
     */
    public PlayerOutcome stop(Member member) throws MemberNotInVoiceChannelException {
        logger.debug("Attempting to stop playback for member '{}'", member.getEffectiveName());
        player.stop(member.getGuild().getId());
        playerMessageService.manuallyClearPlayer(member.getGuild().getId());
        discordVoiceGateway.disconnect(new VoiceDisconnectRequest(member.getGuild().getId()));
        return PlayerOutcome.STOPPED;
    }

    /**
     * Resumes playback of the currently paused track in the voice channel of the specified member.
     * If the member is not in a voice channel, it throws an exception.
     *
     * @param member The member whose voice channel to resume playback in.
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel.
     */
    public PlayerOutcome resume(Member member) throws MemberNotInVoiceChannelException {
        logger.debug("Attempting to resume playback for member '{}'", member.getEffectiveName());
        return player.resume(member.getGuild().getId());
    }

    /**
     * Skips the currently playing track in the voice channel of the specified member.
     * If the member is not in a voice channel, it throws an exception.
     *
     * @param member The member whose voice channel to skip the track in.
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel.
     */
    public PlayerOutcome skip(Member member) throws MemberNotInVoiceChannelException {
        logger.debug("Attempting to skip track for member '{}'", member.getEffectiveName());
        return player.skip(member.getGuild().getId());
    }

    /**
     * Goes back to the previous track in the voice channel of the specified member.
     * If the member is not in a voice channel, it throws an exception.
     *
     * @param member The member whose voice channel to go back in.
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel.
     */
    public PlayerOutcome previous(Member member) throws MemberNotInVoiceChannelException {
        logger.debug("Attempting to go back to previous track for member '{}'", member.getEffectiveName());
        return player.previous(member.getGuild().getId());
    }

    private AudioChannel getAudioChannel(Member member) throws MemberNotInVoiceChannelException {
        logger.debug("Checking voice channel for member '{}'", member.getEffectiveName());
        GuildVoiceState memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || memberVoiceState.getChannel() == null) {
            logger.info("Member {} is not in a voice channel.", member.getEffectiveName());
            throw new MemberNotInVoiceChannelException("Member must be in a voice channel to perform this action.");
        }
        return memberVoiceState.getChannel();
    }
}
