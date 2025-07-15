package com.gammatunes.backend.presentation.bot.player.view.dto;

import com.gammatunes.backend.domain.model.PlayerState;

/**
 * Snapshot of everything the UI needs to render the player.
 * Domain-agnostic, presentation-friendly.
 */
public record PlayerViewModel(
    String guildId,               // for routing updates
    String textChannelId,         // where the embed lives
    /* ─ current track ─ */
    String title,
    String author,
    String sourceUrl,
    String thumbnailUrl,
    /* ─ playback ─ */
    long   positionMs,
    long   durationMs,
    PlayerState state,
    /* ─ UI feedback ─ */
    String statusText             // “Queued”, “Skipped”, “Nothing left”, …
) { }
