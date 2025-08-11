package com.gammatunes.component.discord.ui.panel;

import com.gammatunes.service.PlayerPanelService;
import com.gammatunes.component.audio.events.PlayerUIState;
import com.gammatunes.model.domain.PlayerState;
import com.gammatunes.component.audio.core.PlayerStateStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;


/**
 * Projection that listens to player state changes and updates the player panel accordingly.
 * It handles UI state changes, refreshes the panel, and optionally announces state transitions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerPanelProjection {

    private final PlayerStateStore playerStateStore;
    private final PlayerPanelService panelCoordinator;

    @Value("${gamma.bot.player.announce-outcomes:false}")
    private boolean announceOutcomes;

    private Disposable subscription;


    /**
     * Starts the projection by subscribing to player state changes and handling UI updates.
     * It sets up a stream that refreshes the panel and optionally announces state transitions.
     */
    @PostConstruct
    void start() {
        log.info("PlayerPanelProjection starting; stateStore#={}", System.identityHashCode(playerStateStore));


        Flux<PlayerUIState> uiStates =
            playerStateStore.streamAllUI()
                .onBackpressureLatest()
                .doOnNext(s -> log.debug("[panel] UI change guild={} state={}",
                    s.guildId(), s.state()));

        Flux<Void> refreshFlow =
            uiStates
                .groupBy(PlayerUIState::guildId)
                .flatMap(group ->
                    group
                        .delayElements(Duration.ofMillis(100))
                        .concatMap(s -> {
                            long guildId = group.key();
                            log.debug("[panel] refresh submit guild={}", guildId);
                            return panelCoordinator.refreshPanel(guildId)
                                .onErrorResume(e -> {
                                    log.warn("Panel refresh failed for guild {}: {}", guildId, e.toString());
                                    return Mono.empty();
                                });
                        })
                );

        Flux<Void> announceFlow = announceOutcomes
            ? uiStates
            .groupBy(PlayerUIState::guildId)
            .flatMap(group ->
                group
                    .map(PlayerUIState::state)
                    .distinctUntilChanged()
                    .concatMap(state -> {
                        long guildId = group.key();
                        String text = humanizeState(state);
                        return panelCoordinator.setStatusNoRefresh(guildId, text)
                            .onErrorResume(e -> {
                                log.warn("Publish status failed for guild {}: {}", guildId, e.toString());
                                return Mono.empty();
                            });
                    })
            )
            : Flux.empty();

        Flux<Void> projection =
            Flux.merge(refreshFlow, announceFlow)
                .retryWhen(
                    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofMinutes(1))
                        .doBeforeRetry(rs -> log.warn(
                            "PlayerPanelProjection retrying (attempt {} cause={})",
                            rs.totalRetriesInARow() + 1,
                            rs.failure() == null
                                ? "unknown"
                                : rs.failure().getClass().getSimpleName() + ": " + rs.failure().getMessage()))
                );

        subscription = projection.subscribe(
            null,
            e -> log.error("PlayerPanelProjection stream error", e),
            () -> log.info("PlayerPanelProjection completed")
        );
    }

    /**
     * Stops the projection by disposing of the subscription to player state changes.
     * This is called when the application context is shutting down.
     */
    @PreDestroy
    void stop() {
        if (subscription != null) {
            subscription.dispose();
        }
    }

    /**
     * Converts the player state to a human-readable string with an emoji prefix.
     *
     * @param state The player state to convert.
     * @return A string representation of the player state with an emoji.
     */
    private static String humanizeState(PlayerState state) {
        return switch (state) {
            case PLAYING -> "▶️ Playing";
            case PAUSED  -> "⏸️ Paused";
            case STOPPED -> "⏹️ Stopped";
            case IDLE    -> "💤 Idle";
            case ERROR   -> "⚠️ Error";
        };
    }
}
