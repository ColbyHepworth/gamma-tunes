package com.gammatunes.backend.audio.api;

import java.time.Duration;

/**
 * Represents a single piece of audio that can be played.
 * <p>
 * This is a data-rich object that contains all the necessary information about a track,
 * regardless of its source (YouTube, Spotify, etc.). The {@link #identifier} is the key piece
 * of data that the underlying audio provider (e.g., Lavalink) will use to actually play the track.
 *
 * @param identifier    The unique, playable identifier for the track (e.g., a Lavalink track ID, a direct URL).
 * @param title         The display title of the track.
 * @param author        The artist or creator of the track.
 * @param duration      The length of the track.
 * @param sourceUrl     The original URL provided by the user (e.g., a YouTube or Spotify link).
 * @param thumbnailUrl  An optional URL to a thumbnail image for the track.
 */
public record Track(
    String identifier,
    String title,
    String author,
    Duration duration,
    String sourceUrl,
    String thumbnailUrl
) {
}
