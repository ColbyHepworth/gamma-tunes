package com.gammatunes.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gammatunes.backend.common.model.PlayerState;
import com.gammatunes.backend.common.model.Track;

import java.util.List;

/**
 * A DTO for returning the full status of a player.
 *
 * @param state           The current state of the player (e.g., PLAYING, PAUSED).
 * @param currentlyPlaying The track that is currently playing, or null if none.
 * @param queue           An immutable list of tracks in the queue.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in the JSON response
public record PlayerStatusResponse(
    PlayerState state,
    Track currentlyPlaying,
    List<Track> queue
) {
}
