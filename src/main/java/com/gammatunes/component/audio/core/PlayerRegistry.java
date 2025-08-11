package com.gammatunes.component.audio.core;

import com.gammatunes.component.lavalink.NodePlayer;
import dev.arbjerg.lavalink.client.LavalinkClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps exactly one {@link Player} instance per guild.
 *  • Caches the player so queue/repeat state survive between commands.
 *  • Lazily creates the player on first use.
 *  • Provides a cleanup hook when the bot leaves a guild.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerRegistry {

    /** guild ID → cached {@code Player} */
    private final Map<Long, Player> players = new ConcurrentHashMap<>();

    private final LavalinkClient lavalinkClient;
    private final PlayerStateStore playerStateStore;

    /**
     * Get the cached player for a guild, or create & cache it if it doesn’t exist.
     *
     * @param guildId Discord guild/server id
     * @return a Mono that emits the {@link Player}
     */
    public Mono<Player> getOrCreate(long guildId) {
        return Mono.fromCallable(() ->
            players.computeIfAbsent(guildId, id -> {
                log.debug("Creating Player for guild {}", id);
                NodePlayer nodePlayer = new NodePlayer(lavalinkClient, id);
                return new Player(nodePlayer, playerStateStore);
            })
        );
    }

    /**
     * Destroys the player for a guild, cleaning up resources and removing it from the cache.
     *
     * @param guildId Discord guild/server id
     */
    public void destroy(long guildId) {
        players.remove(guildId);
        lavalinkClient.getOrCreateLink(guildId)
            .destroy()
            .subscribe(
                null,
                error -> log.warn("Error destroying Lavalink link for guild {}: {}", guildId, error.toString()),
                () -> log.debug("Destroyed Lavalink link for guild {}", guildId)
            );
        log.debug("Destroyed player for guild {}", guildId);
    }
}
