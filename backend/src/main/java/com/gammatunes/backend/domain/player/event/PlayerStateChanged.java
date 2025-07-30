package com.gammatunes.backend.domain.player.event;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;


/**
 * Event indicating that the player state has changed.
 * This is used to notify the system when the player state changes, such as when it is paused, resumed, or stopped.
 */
public record PlayerStateChanged(String sessionId,
                                 PlayerState state,
                                 PlayerOutcome outcome) {}
