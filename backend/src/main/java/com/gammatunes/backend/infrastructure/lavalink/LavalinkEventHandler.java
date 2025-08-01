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

    /**
     * Handles track end events, checking if the next track should be started or if the player should repeat.
     *
     * @param player The audio player instance.
     * @param track The track that ended.
     * @param endReason The reason why the track ended.
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log.debug("Track ended for session {}. Reason: {}", lavalinkPlayer.getSession().id(), endReason);

        if (endReason.mayStartNext) {
            if (lavalinkPlayer.isRepeatEnabled()) {
                log.info("Repeating track '{}' for session {}.",
                    track.getInfo().title, lavalinkPlayer.getSession().id());
                AudioTrack freshCopy = track.makeClone();
                player.playTrack(freshCopy);
            } else {
                lavalinkPlayer.onTrackEnd();
            }
        }
    }

    /**
     * Handles track start events, updating the player state to PLAYING.
     *
     * @param player The audio player instance.
     * @param track The track that started playing.
     */
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        log.debug("[{}] Track started: {}", lavalinkPlayer.getSession().id(), track.getInfo().title);
        lavalinkPlayer.gotoState(PlayerState.PLAYING, PlayerOutcome.PLAYING_NOW, false);
    }

    /**
     * Handles track load events, updating the player state and notifying listeners.
     *
     * @param player The audio player instance.
     * @param track The loaded track.
     */
    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.error("Track exception for session {} on track '{}': {}",
            lavalinkPlayer.getSession().id(), track.getInfo().title, exception.getMessage());
        lavalinkPlayer.onTrackEnd();
    }

    /**
     * Handles track stuck events, logging a warning and skipping the track.
     *
     * @param player The audio player instance.
     * @param track The track that got stuck.
     * @param thresholdMs The threshold in milliseconds after which the track is considered stuck.
     * @param stackTrace The stack trace at the time the track got stuck.
     */
    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
        log.warn("Track stuck for session {} on track '{}'. Threshold: {}ms. Skipping.",
            lavalinkPlayer.getSession().id(), track.getInfo().title, thresholdMs);
        lavalinkPlayer.onTrackEnd();
    }
}

