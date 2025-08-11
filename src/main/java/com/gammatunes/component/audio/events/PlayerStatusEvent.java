package com.gammatunes.component.audio.events;

/**
 * Represents an event related to a player's status in the audio component.
 * This record holds the guild ID and the player's status.
 */
public record PlayerStatusEvent(long guildId, PlayerStatus status) implements PlayerEvent { }
