package com.gammatunes.component.audio.events;

/**
 * Represents an event related to a player in the audio component.
 * This is a sealed interface that can be implemented by specific player event types.
 */
public sealed interface PlayerEvent permits PlayerOutcomeEvent, PlayerStatusEvent { }

