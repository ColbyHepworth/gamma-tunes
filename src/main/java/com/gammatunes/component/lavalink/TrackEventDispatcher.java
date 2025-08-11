package com.gammatunes.component.lavalink;

import com.gammatunes.component.audio.core.PlayerRegistry;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.PlayerUpdateEvent;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackExceptionEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.event.TrackStuckEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * This component listens to various Lavalink track events and dispatches them to the appropriate player event handlers.
 * It handles events such as TrackStart, TrackEnd, PlayerUpdate, TrackException, and TrackStuck.
 * Each event is processed per guild, allowing for efficient handling of multiple guilds in a single application instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrackEventDispatcher {

    private final LavalinkClient lavalinkClient;
    private final PlayerRegistry playerRegistry;

    private Disposable.Composite subscriptions;

    @PostConstruct
    void init() {
        subscriptions = Disposables.composite();

        // === TrackStart ========================================================
        Disposable trackStartSubscription = lavalinkClient.on(TrackStartEvent.class)
            .doOnSubscribe(subscription -> log.info("TrackStart stream subscribed"))
            .doOnError(throwable -> log.error("TrackStart stream error (root)", throwable))
            .groupBy(TrackStartEvent::getGuildId)
            .flatMap(groupByGuild ->
                groupByGuild
                    .doOnSubscribe(subscription ->
                        log.debug("TrackStart group[{}] subscribed", groupByGuild.key()))
                    .doOnNext(event ->
                        log.debug("TrackStart recv guild={} title='{}' thread={}",
                            groupByGuild.key(),
                            safe(event.getTrack().getInfo().getTitle()),
                            Thread.currentThread().getName()))
                    .concatMap(event -> {
                        long startedNanos = System.nanoTime();
                        return playerRegistry.getOrCreate(groupByGuild.key())
                            .flatMap(player -> player.getEventHandler().onTrackStart(event.getTrack()))
                            .doOnSuccess(ignored ->
                                log.debug("TrackStart handled guild={} in {}ms",
                                    groupByGuild.key(), msSince(startedNanos)))
                            .onErrorResume(handlerError -> {
                                log.warn("TrackStart handler failed guild={}: {}",
                                    groupByGuild.key(), describe(handlerError));
                                return Mono.empty();
                            });
                    })
            )
            .retryWhen(retryBackoff("TrackStart"))
            .doOnTerminate(() -> log.info("TrackStart stream terminated"))
            .subscribe(
                ignored -> { },
                subscriberError -> log.error("TrackStart subscriber error", subscriberError)
            );
        subscriptions.add(trackStartSubscription);

        // === TrackEnd ========================================================
        Disposable trackEndSubscription = lavalinkClient.on(TrackEndEvent.class)
            .doOnSubscribe(subscription -> log.info("TrackEnd stream subscribed"))
            .doOnError(throwable -> log.error("TrackEnd stream error (root)", throwable))
            .groupBy(TrackEndEvent::getGuildId)
            .flatMap(groupByGuild ->
                groupByGuild
                    .doOnSubscribe(subscription ->
                        log.debug("TrackEnd group[{}] subscribed", groupByGuild.key()))
                    .doOnNext(event ->
                        log.debug("TrackEnd recv guild={} reason={} thread={}",
                            groupByGuild.key(), event.getEndReason(),
                            Thread.currentThread().getName()))
                    .concatMap(event -> {
                        long startedNanos = System.nanoTime();
                        return playerRegistry.getOrCreate(groupByGuild.key())
                            .flatMap(player ->
                                player.getEventHandler().onTrackEnd(event.getTrack(), event.getEndReason()))
                            .doOnSuccess(ignored ->
                                log.debug("TrackEnd handled guild={} in {}ms",
                                    groupByGuild.key(), msSince(startedNanos)))
                            .onErrorResume(handlerError -> {
                                log.warn("TrackEnd handler failed guild={}: {}",
                                    groupByGuild.key(), describe(handlerError));
                                return Mono.empty();
                            });
                    })
            )
            .retryWhen(retryBackoff("TrackEnd"))
            .doOnTerminate(() -> log.info("TrackEnd stream terminated"))
            .subscribe(
                ignored -> { },
                subscriberError -> log.error("TrackEnd subscriber error", subscriberError)
            );
        subscriptions.add(trackEndSubscription);

        // === PlayerUpdate (position/connected/ping) ==========================
        Disposable playerUpdateSubscription = lavalinkClient.on(PlayerUpdateEvent.class)
            .onBackpressureDrop(event ->
                log.debug("Dropped PlayerUpdate guild={} pos={}",
                    event.getGuildId(),
                    event.getState().getPosition()))
            .sample(Duration.ofMillis(350))
            .groupBy(PlayerUpdateEvent::getGuildId)
            .flatMap(groupByGuild ->
                groupByGuild.concatMap(event ->
                    playerRegistry.getOrCreate(groupByGuild.key())
                        .flatMap(player ->
                            player.getEventHandler()
                                .onPlayerUpdate(event.getState().getPosition()))
                        .onErrorResume(handlerError -> {
                            log.warn("PlayerUpdate handler failed guild={}: {}",
                                groupByGuild.key(), describe(handlerError));
                            return Mono.empty();
                        })
                )
            )
            .doOnSubscribe(subscription -> log.info("PlayerUpdate stream subscribed"))
            .doOnTerminate(() -> log.info("PlayerUpdate stream terminated"))
            .subscribe(
                ignored -> { },
                subscriberError -> log.error("PlayerUpdate subscriber error", subscriberError)
            );
        subscriptions.add(playerUpdateSubscription);

        // === TrackException ==================================================
        Disposable trackExceptionSubscription = lavalinkClient.on(TrackExceptionEvent.class)
            .doOnSubscribe(subscription -> log.info("TrackException stream subscribed"))
            .doOnError(throwable -> log.error("TrackException stream error (root)", throwable))
            .groupBy(TrackExceptionEvent::getGuildId)
            .flatMap(groupByGuild ->
                groupByGuild
                    .doOnSubscribe(subscription ->
                        log.debug("TrackException group[{}] subscribed", groupByGuild.key()))
                    .doOnNext(event ->
                        log.warn("TrackException recv guild={} msg={}",
                            groupByGuild.key(),
                            event.getException().getMessage()))
                    .concatMap(event -> {
                        long startedNanos = System.nanoTime();
                        return playerRegistry.getOrCreate(groupByGuild.key())
                            .flatMap(player ->
                                player.getEventHandler()
                                    .onTrackException(event.getTrack(), event.getException()))
                            .doOnSuccess(ignored ->
                                log.debug("TrackException handled guild={} in {}ms",
                                    groupByGuild.key(), msSince(startedNanos)))
                            .onErrorResume(handlerError -> {
                                log.warn("TrackException handler failed guild={}: {}",
                                    groupByGuild.key(), describe(handlerError));
                                return Mono.empty();
                            });
                    })
            )
            .retryWhen(retryBackoff("TrackException"))
            .doOnTerminate(() -> log.info("TrackException stream terminated"))
            .subscribe(
                ignored -> { },
                subscriberError -> log.error("TrackException subscriber error", subscriberError)
            );
        subscriptions.add(trackExceptionSubscription);

        // === TrackStuck ======================================================
        Disposable trackStuckSubscription = lavalinkClient.on(TrackStuckEvent.class)
            .doOnSubscribe(subscription -> log.info("TrackStuck stream subscribed"))
            .doOnError(throwable -> log.error("TrackStuck stream error (root)", throwable))
            .groupBy(TrackStuckEvent::getGuildId)
            .flatMap(groupByGuild ->
                groupByGuild
                    .doOnSubscribe(subscription ->
                        log.debug("TrackStuck group[{}] subscribed", groupByGuild.key()))
                    .doOnNext(event ->
                        log.warn("TrackStuck recv guild={} thresholdMs={}",
                            groupByGuild.key(), event.getThresholdMs()))
                    .concatMap(event -> {
                        long startedNanos = System.nanoTime();
                        return playerRegistry.getOrCreate(groupByGuild.key())
                            .flatMap(player ->
                                player.getEventHandler()
                                    .onTrackStuck(event.getTrack(), event.getThresholdMs()))
                            .doOnSuccess(ignored ->
                                log.debug("TrackStuck handled guild={} in {}ms",
                                    groupByGuild.key(), msSince(startedNanos)))
                            .onErrorResume(handlerError -> {
                                log.warn("TrackStuck handler failed guild={}: {}",
                                    groupByGuild.key(), describe(handlerError));
                                return Mono.empty();
                            });
                    })
            )
            .retryWhen(retryBackoff("TrackStuck"))
            .doOnTerminate(() -> log.info("TrackStuck stream terminated"))
            .subscribe(
                ignored -> { },
                subscriberError -> log.error("TrackStuck subscriber error", subscriberError)
            );
        subscriptions.add(trackStuckSubscription);

        log.info("TrackEventBridge subscriptions initialized.");
    }

    /**
     * Creates a retry strategy with exponential backoff for stream retries.
     * Logs the retry attempts and their causes.
     *
     * @param label A label to identify the stream in the logs.
     * @return A Retry strategy with backoff and logging.
     */
    private Retry retryBackoff(String label) {
        return Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
            .maxBackoff(Duration.ofMinutes(1))
            .doBeforeRetry(retrySignal -> {
                Throwable failure = retrySignal.failure();
                String cause = (failure == null)
                    ? "unknown"
                    : failure.getClass().getSimpleName() + ": " + failure.getMessage();
                log.warn("{} stream retrying (attempt {} cause={})",
                    label, retrySignal.totalRetriesInARow() + 1, cause);
            });
    }

    /**
     * Calculates the milliseconds since a given timestamp in nanoseconds.
     *
     * @param startedNanos The timestamp in nanoseconds to calculate from.
     * @return The elapsed time in milliseconds.
     */
    private static long msSince(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000L;
    }

    /**
     * Returns a safe string representation of the input, returning an empty string if the input is null.
     *
     * @param input The input string to check.
     * @return The input string if not null, or an empty string if it is null.
     */
    private static String safe(String input) {
        return input == null ? "" : input;
    }

    /**
     * Describes a Throwable by its class name and message.
     * If the Throwable is null, returns "null".
     *
     * @param throwable The Throwable to describe.
     * @return A string representation of the Throwable.
     */
    private static String describe(Throwable throwable) {
        return throwable == null
            ? "null"
            : throwable.getClass().getSimpleName() + ": " + (throwable.getMessage() == null ? "" : throwable.getMessage());
    }

    /**
     * Shuts down the TrackEventBridge by disposing of all subscriptions.
     * This is called when the application context is shutting down.
     */
    @PreDestroy
    void shutdown() {
        if (subscriptions != null) {
            subscriptions.dispose();
            log.info("TrackEventBridge subscriptions disposed.");
        }
    }
}
