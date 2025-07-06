package com.gammatunes.backend.web.dto;

/**
 * Represents a request to play a track based on a search query.
 * The query can be a song title, artist name, or any other relevant search term.
 * @param query query The search query to find the track to play.
 */

public record PlayRequest(String query) {
}
