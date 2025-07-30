package com.gammatunes.backend.infrastructure.lavalink;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles events from the lavaplayer library by extending AudioEventAdapter.
 */
public class LavalinkEventHandler extends AudioEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(LavalinkEventHandler.class);
    private final LavaLinkAudioPlayer lavalinkPlayer;

    public LavalinkEventHandler(LavaLinkAudioPlayer lavalinkPlayer) {
        this.lavalinkPlayer = lavalinkPlayer;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log.debug("Track ended for session {}. Reason: {}", lavalinkPlayer.getSession().id(), endReason);
        if (endReason.mayStartNext) {
            lavalinkPlayer.onTrackEnd();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        log.debug("[{}] Track started: {}", lavalinkPlayer.getSession().id(), track.getInfo().title);
        lavalinkPlayer.gotoState(PlayerState.PLAYING, PlayerOutcome.PLAYING_NOW, false);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.error("Track exception for session {} on track '{}': {}",
            lavalinkPlayer.getSession().id(), track.getInfo().title, exception.getMessage());
        lavalinkPlayer.onTrackEnd();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
        log.warn("Track stuck for session {} on track '{}'. Threshold: {}ms. Skipping.",
            lavalinkPlayer.getSession().id(), track.getInfo().title, thresholdMs);
        lavalinkPlayer.onTrackEnd();
    }
}

