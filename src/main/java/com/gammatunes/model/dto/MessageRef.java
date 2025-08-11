package com.gammatunes.model.dto;

/**
 * Represents a reference to a message in a specific guild and channel.
 * This record contains the guild ID, channel ID, and message ID.
 *
 * @param guildId The ID of the guild where the message is located.
 * @param channelId The ID of the channel where the message is located.
 * @param messageId The ID of the message itself.
 */
public record MessageRef(long guildId, long channelId, long messageId) {}
