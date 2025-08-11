package com.gammatunes.model.dto;

import java.util.List;
import java.util.Optional;

/**
 * Represents the current state of the music player in a guild, including the current track,
 * queue, history, and player settings such as volume and repeat mode.
 * This record is used to encapsulate all relevant information for rendering the player view.
 */
public record PlayerView(
    long guildId,
    String state,
    long positionMillis,
    boolean repeatEnabled,
    int volume,
    Optional<TrackView> currentTrack,
    List<TrackView> queue,
    List<TrackView> history
) {

    public record RequesterView(
        String userId,
        String displayName,
        String avatarUrl
    ) { }

    public record TrackView(
        String identifier,
        String title,
        String author,
        String uri,
        String artworkUrl,
        long lengthMillis,
        Optional<RequesterView> requestedBy
    ) { }

    public PlayerView(
        long guildId,
        String state,
        long positionMillis,
        boolean repeatEnabled,
        int volume,
        TrackView currentTrack,
        List<TrackView> queue,
        List<TrackView> history
    ) {
        this.guildId = guildId;
        this.state = state;
        this.positionMillis = positionMillis;
        this.repeatEnabled = repeatEnabled;
        this.volume = volume;
        this.currentTrack = Optional.ofNullable(currentTrack);
        this.queue = List.copyOf(queue);
        this.history = List.copyOf(history);
    }
}
