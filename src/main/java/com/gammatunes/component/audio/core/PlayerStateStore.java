package com.gammatunes.component.audio.core;

import com.gammatunes.component.audio.events.PlayerUIState;
import com.gammatunes.component.audio.events.PlayerPosition;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlayerStateStore is a component that manages the state of the audio player for different guilds.
 * It provides methods to set and get the current UI state and position, as well as to stream these states.
 * The states are stored in a thread-safe manner using ConcurrentHashMap and Sinks for reactive streaming.
 */
@Component
public class PlayerStateStore {

    private final ConcurrentHashMap<Long, PlayerUIState> currentUI = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, PlayerPosition> currentPosition = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, Sinks.Many<PlayerUIState>> perGuildUI = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Sinks.Many<PlayerPosition>> perGuildPosition = new ConcurrentHashMap<>();

    private final Sinks.Many<PlayerUIState> globalUI = Sinks.many().multicast().directBestEffort();
    private final Sinks.Many<PlayerPosition> globalPosition = Sinks.many().multicast().directBestEffort();

    /**
     * Retrieves the sink for PlayerUIState for a specific guild, creating it if it does not exist.
     *
     * @param guildId The ID of the guild.
     * @return The Sinks.Many instance for PlayerUIState.
     */
    private Sinks.Many<PlayerUIState> sinkUI(long guildId) {
        return perGuildUI.computeIfAbsent(guildId, id -> Sinks.many().replay().latest());
    }

    /**
     * Retrieves the sink for PlayerPosition for a specific guild, creating it if it does not exist.
     *
     * @param guildId The ID of the guild.
     * @return The Sinks.Many instance for PlayerPosition.
     */
    private Sinks.Many<PlayerPosition> sinkPosition(long guildId) {
        return perGuildPosition.computeIfAbsent(guildId, id -> Sinks.many().replay().latest());
    }

    /**
     * Gets the current UI state for a specific guild.
     *
     * @param guildId The ID of the guild.
     * @return The PlayerUIState for the guild, or null if not set.
     */
    public PlayerUIState getUI(long guildId) {
        return currentUI.get(guildId);
    }

    /**
     * Gets the current position for a specific guild.
     *
     * @param guildId The ID of the guild.
     * @return The PlayerPosition for the guild, or null if not set.
     */
    public PlayerPosition getPosition(long guildId) {
        return currentPosition.get(guildId);
    }

    /**
     * Sets the UI state for a specific guild and emits it to the corresponding sinks.
     *
     * @param uiState The PlayerUIState to set.
     */
    public void setUIState(PlayerUIState uiState) {
        currentUI.put(uiState.guildId(), uiState);
        sinkUI(uiState.guildId()).tryEmitNext(uiState);
        globalUI.tryEmitNext(uiState);
    }

    /**
     * Sets the position for a specific guild and emits it to the corresponding sinks.
     *
     * @param position The PlayerPosition to set.
     */
    public void setPosition(PlayerPosition position) {
        currentPosition.put(position.guildId(), position);
        sinkPosition(position.guildId()).tryEmitNext(position);
        globalPosition.tryEmitNext(position);
    }

    /**
     * Streams the PlayerPosition for a specific guild.
     *
     * @return A Flux that emits PlayerPosition updates for the guild.
     */
    public Flux<PlayerUIState> streamAllUI() {
        return globalUI.asFlux();
    }

}
