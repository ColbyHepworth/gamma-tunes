package com.gammatunes.backend.presentation.bot.player.view.dto;

import com.gammatunes.backend.domain.model.PlayerOutcome;

public record PlayerOutcomeResult(
    PlayerOutcome outcome,
    String details
) {}
