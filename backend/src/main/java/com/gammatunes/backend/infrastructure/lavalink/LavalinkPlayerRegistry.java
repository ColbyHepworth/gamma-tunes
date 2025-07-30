package com.gammatunes.backend.infrastructure.lavalink;

import com.gammatunes.backend.application.port.out.PlayerRegistryPort;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.player.AudioPlayer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible for managing {@link AudioPlayer} instances for each session.
 * It uses a {@link ConcurrentHashMap} to store players, ensuring thread safety.
 * The players are created using the provided {@link AudioPlayerManager} from Lavaplayer.
 * It implements the {@link PlayerRegistryPort} interface, allowing it to be used
 */
@Component
@RequiredArgsConstructor
public class LavalinkPlayerRegistry implements PlayerRegistryPort {

    private static final Logger log = LoggerFactory.getLogger(LavalinkPlayerRegistry.class);

    private final Map<String, AudioPlayer> players = new ConcurrentHashMap<>();
    private final AudioPlayerManager lavaManager;

    /**
     * Retrieves or creates a new AudioPlayer for the given session.
     * If a player already exists for the session, it returns that player.
     * Otherwise, it creates a new Lavaplayer instance and wraps it in a LavalinkPlaybackAdapter.
     *
     * @param session The session for which to retrieve or create an AudioPlayer.
     * @return An AudioPlayer instance associated with the session.
     */
    @Override
    public AudioPlayer getOrCreatePlayer(Session session) {
        return players.computeIfAbsent(session.id(), id -> {
            log.trace("Creating new Lavaplayer instance for session {}", id);
            var lava = lavaManager.createPlayer();
            return new LavalinkPlaybackAdapter(
                session,
                lava,
                lavaManager
            );
        });
    }

    /**
     * Removes the AudioPlayer associated with the given session.
     * If the player is an instance of LavalinkPlaybackAdapter, it destroys the underlying Lavaplayer instance.
     *
     * @param session The session for which to remove the AudioPlayer.
     */
    @Override
    public void removePlayer(Session session) {
        AudioPlayer player = players.remove(session.id());
        if (player instanceof LavalinkPlaybackAdapter adapter) {
            log.info("Removing Lavaplayer instance for session {}", session.id());
            adapter.getLavaPlayer().destroy();
        }
    }
}
