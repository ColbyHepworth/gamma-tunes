package com.gammatunes.component.audio.events;

/**
 * Represents an event related to a player's outcome in the audio component.
 * This record holds the guild ID and the outcome of the player.
 */
public record PlayerOutcomeEvent(long guildId, PlayerOutcome outcome) implements PlayerEvent { }
