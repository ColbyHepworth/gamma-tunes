package com.gammatunes.component.discord.interaction;


public record InteractionSupport(
    InteractionErrorHandler interactionErrorHandler,
    InteractionMetrics interactionMetrics
) {}
