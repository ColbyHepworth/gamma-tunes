package com.gammatunes.backend.infrastructure.lavalink.event;

import com.gammatunes.backend.infrastructure.lavalink.LavalinkPlaybackAdapter;
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
    private final LavalinkPlaybackAdapter ourPlayerAdapter;

    public LavalinkEventHandler(LavalinkPlaybackAdapter ourPlayerAdapter) {
        this.ourPlayerAdapter = ourPlayerAdapter;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log.debug("Track ended for session {}. Reason: {}", ourPlayerAdapter.getSession().id(), endReason);
        if (endReason.mayStartNext) {
            ourPlayerAdapter.onTrackEnd();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.error("Track exception for session {} on track '{}': {}",
            ourPlayerAdapter.getSession().id(), track.getInfo().title, exception.getMessage());
        ourPlayerAdapter.onTrackEnd();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
        log.warn("Track stuck for session {} on track '{}'. Threshold: {}ms. Skipping.",
            ourPlayerAdapter.getSession().id(), track.getInfo().title, thresholdMs);
        ourPlayerAdapter.onTrackEnd();
    }
}

