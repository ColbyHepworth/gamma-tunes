package com.gammatunes.component.audio.events;

/**
 * Represents the position of a player in a guild.
 * This record holds the guild ID, the current position in milliseconds,
 * and the total length of the track in milliseconds.
 *
 * @param guildId    The ID of the guild where the player is active.
 * @param positionMs The current position of the player in milliseconds.
 * @param lengthMs   The total length of the track in milliseconds.
 */
public record PlayerPosition(
    long guildId,
    long positionMs,
    long lengthMs
) {}
