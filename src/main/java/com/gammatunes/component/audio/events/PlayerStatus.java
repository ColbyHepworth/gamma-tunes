package com.gammatunes.component.audio.events;

import com.gammatunes.model.domain.PlayerState;
import dev.arbjerg.lavalink.client.player.Track;

/**
 * Represents the status of a player in the audio component.
 * This record contains information about the player's state, volume, repeat status,
 * current track, queue size, and position in the track.
 */
public record PlayerStatus(
    long guildId,
    PlayerState state,
    int volume,
    boolean repeat,
    Track currentTrack,
    int queueSize,
    long positionMs,
    long lengthMs
) {}
