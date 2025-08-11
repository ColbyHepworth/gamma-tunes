package com.gammatunes.component.audio.events;

import com.gammatunes.model.domain.PlayerState;
import dev.arbjerg.lavalink.client.player.Track;

import java.util.List;

/**
 * Represents the UI state of a player in the audio component.
 * This record contains information about the player's state, volume, repeat status,
 * current track, queue, and history.
 */
public record PlayerUIState(
    long guildId,
    PlayerState state,
    int volume,
    boolean repeat,
    Track currentTrack,
    List<Track> queue,
    List<Track> history
) {}
