package com.gammatunes.backend.domain.model;

/**
 * Immutable identifier for a single Discord guild’s audio session.
 * The same {@code Session} value is passed from presentation
 * (slash command) → application → infrastructure.
 */
public record Session(String id) {
}
