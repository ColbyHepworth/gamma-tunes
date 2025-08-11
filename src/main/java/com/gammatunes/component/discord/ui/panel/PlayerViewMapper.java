package com.gammatunes.component.discord.ui.panel;

import com.fasterxml.jackson.databind.JsonNode;
import com.gammatunes.component.audio.events.PlayerUIState;
import com.gammatunes.component.audio.events.PlayerPosition;
import com.gammatunes.model.dto.PlayerView;
import dev.arbjerg.lavalink.client.player.Track;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class to map PlayerUIState and PlayerPosition to a PlayerView.
 * This class handles the conversion of the current track, queue, and history
 * into a format suitable for the player view, including requester information
 * extracted from the track's user data.
 */
public final class PlayerViewMapper {

    public static PlayerView toView(PlayerUIState uiState, PlayerPosition position) {
        Optional<PlayerView.TrackView> currentTrackView = Optional.ofNullable(uiState.currentTrack())
            .map(PlayerViewMapper::toTrackView);

        List<PlayerView.TrackView> queueTrackViews = uiState.queue()
            .stream()
            .map(PlayerViewMapper::toTrackView)
            .toList();

        List<PlayerView.TrackView> historyTrackViews = uiState.history()
            .stream()
            .map(PlayerViewMapper::toTrackView)
            .toList();

        return new PlayerView(
            uiState.guildId(),
            uiState.state().name(),
            position != null ? position.positionMs() : 0L,
            uiState.repeat(),
            uiState.volume(),
            currentTrackView,
            queueTrackViews,
            historyTrackViews
        );
    }

    /**
     * Convert a Lavalink Track to a PlayerView.TrackView, including the requester info
     * if it was attached to track.getUserData() at enqueue-time.
     */
    private static PlayerView.TrackView toTrackView(Track track) {
        return new PlayerView.TrackView(
            track.getInfo().getIdentifier(),
            track.getInfo().getTitle(),
            track.getInfo().getAuthor(),
            track.getInfo().getUri(),
            track.getInfo().getArtworkUrl(),
            track.getInfo().getLength(),
            extractRequesterFromUserData(track.getUserData())
        );
    }

    /**
     * Attempts to extract requester information from userData.
     * Supports either a JsonNode payload or a Map<String,String>.
     */
    @SuppressWarnings("unchecked")
    private static Optional<PlayerView.RequesterView> extractRequesterFromUserData(Object userData) {
        switch (userData) {
            case null -> {
                return Optional.empty();
            }


            case JsonNode json -> {
                String userId = nullIfBlank(json.path("userId").asText(null));
                String displayName = nullIfBlank(json.path("displayName").asText(null));
                String avatarUrl = nullIfBlank(json.path("avatarUrl").asText(null));

                if (userId != null || displayName != null || avatarUrl != null) {
                    return Optional.of(new PlayerView.RequesterView(userId, displayName, avatarUrl));
                }
                return Optional.empty();
            }

            case Map<?, ?> mapRaw -> {
                try {
                    Map<String, String> map = (Map<String, String>) mapRaw;
                    String userId = nullIfBlank(map.get("userId"));
                    String displayName = nullIfBlank(map.get("displayName"));
                    String avatarUrl = nullIfBlank(map.get("avatarUrl"));

                    if (userId != null || displayName != null || avatarUrl != null) {
                        return Optional.of(new PlayerView.RequesterView(userId, displayName, avatarUrl));
                    }
                    return Optional.empty();
                } catch (ClassCastException ignored) {
                }
            }
            default -> {
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the value if it is not null or blank, otherwise returns null.
     */
    private static String nullIfBlank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private PlayerViewMapper() {
    }
}
