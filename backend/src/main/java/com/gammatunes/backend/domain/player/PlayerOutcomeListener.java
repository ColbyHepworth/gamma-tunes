package com.gammatunes.backend.domain.player;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;

public interface PlayerOutcomeListener {
    void onOutcome(String sessionId,
                   PlayerState newState,
                   PlayerOutcome outcome);
}
