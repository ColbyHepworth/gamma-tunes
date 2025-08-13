package com.gammatunes.service;

import com.gammatunes.component.discord.ui.PlayerPanelCache;
import com.gammatunes.model.dto.MessageRef;
import com.gammatunes.component.discord.ui.panel.PlayerPanelManager;
import com.gammatunes.component.audio.core.PlayerRegistry;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing player panels in Discord.
 * It handles creation, deletion, and refreshing of player panels,
 * as well as publishing status updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerPanelService {

    private final PlayerPanelManager gateway;
    private final PlayerPanelCache cache;
    private final PlayerRegistry playerRegistry;

    @Value("${gamma.bot.player.panel.min-refresh-gap-ms:1000}")
    private long minRefreshGapMs;

    private final ConcurrentHashMap<Long, Long> lastWriteAt = new ConcurrentHashMap<>();

    /**
     * Creates a new player panel in the specified guild and channel.
     * If a panel already exists, it will be deleted first.
     *
     * @param guildId The ID of the guild where the panel should be created.
     * @param channel The channel where the panel will be posted.
     * @return A Mono that completes when the panel is created.
     */
    public Mono<Void> createPanel(long guildId, TextChannel channel) {
        log.debug("createPanel guild={} channel={}", guildId, channel.getId());
        return Mono.justOrEmpty(cache.getMessage(guildId))
            .flatMap(gateway::deletePanel)
            .onErrorResume(e -> Mono.empty())
            .then(gateway.createPanel(guildId, channel, cache.getStatus(guildId) != null ? cache.getStatus(guildId) : "Initializing..."))
            .doOnNext(ref -> {
                cache.putMessage(guildId, ref);
                lastWriteAt.put(guildId, System.currentTimeMillis());
            })
            .then();
    }

    /**
     * Deletes the player panel for the specified guild.
     * If no panel exists, it will do nothing.
     *
     * @param guildId The ID of the guild whose panel should be deleted.
     * @return A Mono that completes when the panel is deleted.
     */
    public Mono<Void> deletePanel(long guildId) {
        log.debug("deletePanel guild={}", guildId);
        return Mono.justOrEmpty(cache.getMessage(guildId))
            .flatMap(gateway::deletePanel)
            .doFinally(sig -> {
                cache.removeMessage(guildId);
                lastWriteAt.remove(guildId);
            })
            .then();
    }

    /**
     * Refreshes the player panel for the specified guild.
     * If the panel does not exist, it will not perform any action.
     * It also respects a minimum refresh gap to avoid spamming updates.
     *
     * @param guildId The ID of the guild whose panel should be refreshed.
     * @return A Mono that completes when the refresh is done or skipped.
     */
    public Mono<Void> refreshPanel(long guildId) {
        Optional<MessageRef> maybeRef = cache.getMessage(guildId);
        log.debug("refreshPanel guild={} refPresent={}", guildId, maybeRef.isPresent());

        if (maybeRef.isEmpty()) {
            return Mono.empty();
        }

        // Simple per-guild throttle to avoid spamming edits
        long now = System.currentTimeMillis();
        Long last = lastWriteAt.get(guildId);
        if (last != null && (now - last) < minRefreshGapMs) {
            if (log.isDebugEnabled()) {
                log.debug("refreshPanel SKIPPED ({}ms < gap {}ms) guild={}", (now - last), minRefreshGapMs, guildId);
            }
            return Mono.empty();
        }

        MessageRef ref = maybeRef.get();

        return gateway.updatePanel(ref, cache.getStatus(guildId))
            .doOnSuccess(v -> lastWriteAt.put(guildId, System.currentTimeMillis()))
            .onErrorResume(e -> {
                if (!playerRegistry.exists(guildId)) {
                    log.debug("Player no longer exists for guild {}, cleaning up panel reference", guildId);
                    cache.removeMessage(guildId);
                    lastWriteAt.remove(guildId);
                    return Mono.empty();
                }

                log.warn("Update failed for {}: {} – attempting recreate", ref, e.toString());
                return gateway.recreatePanel(ref, cache.getStatus(guildId))
                    .doOnNext(newRef -> {
                        cache.putMessage(guildId, newRef);
                        lastWriteAt.put(guildId, System.currentTimeMillis());
                    })
                    .then();
            })
            .then();
    }

    /**
     * Sets the status text without triggering a panel refresh.
     * This is useful for updating the status without immediately refreshing the UI.
     *
     * @param guildId The ID of the guild to set the status for.
     * @param status  The status text to set.
     * @return A Mono that completes when the status is set.
     */
    public Mono<Void> setStatusNoRefresh(long guildId, String status) {
        cache.setStatus(guildId, status);
        return Mono.empty();
    }

    /**
     * Gets the message reference for a guild's player panel, if it exists.
     *
     * @param guildId The ID of the guild to get the message reference for.
     * @return An Optional containing the MessageRef if it exists, empty otherwise.
     */
    public Optional<MessageRef> getMessage(long guildId) {
        return cache.getMessage(guildId);
    }

    /**
     * Cleans up player panels on application shutdown.
     * It deletes all existing panels in the guilds where they were created.
     */
    @PreDestroy
    void cleanup() {
        log.info("Cleaning up player panels on shutdown");
        cache.guildIds().forEach(gid ->
            cache.getMessage(gid).ifPresent(ref -> gateway.deletePanel(ref).subscribe())
        );
    }

}
