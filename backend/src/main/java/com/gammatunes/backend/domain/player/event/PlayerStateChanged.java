package com.gammatunes.backend.domain.player.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event indicating that the player state has changed.
 * This is used to notify the system when the player state changes, such as when it is paused, resumed, or stopped.
 */
@Getter
@RequiredArgsConstructor
public final class PlayerStateChanged {
    private final String guildId;
}
