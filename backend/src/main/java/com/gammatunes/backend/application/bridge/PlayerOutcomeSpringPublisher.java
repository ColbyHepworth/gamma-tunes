package com.gammatunes.backend.application.bridge;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.player.PlayerOutcomeListener;
import com.gammatunes.backend.domain.player.event.PlayerStateChanged;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * This listener publishes player state changes as application events.
 * It is used to notify other components of the system about changes in the player's state.
 */
@Component
@RequiredArgsConstructor
public class PlayerOutcomeSpringPublisher implements PlayerOutcomeListener {

    private final ApplicationEventPublisher publisher;

    @Override
    public void onOutcome(String sessionId,
                          PlayerState newState,
                          PlayerOutcome outcome) {
        publisher.publishEvent(new PlayerStateChanged(sessionId, newState, outcome));
    }
}
