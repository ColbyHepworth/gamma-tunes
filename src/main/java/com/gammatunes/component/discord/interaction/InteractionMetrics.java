package com.gammatunes.component.discord.interaction;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Component for recording metrics related to Discord interactions such as commands, buttons, and selects.
 * It uses Micrometer to record invocation counts and latency for each interaction type.
 */
@Component
public record InteractionMetrics(MeterRegistry meterRegistry) {

    /**
     * Records the invocation of a command with its success status and duration.
     *
     * @param name          The name of the command.
     * @param success       Whether the command was successful.
     * @param durationNanos The duration of the command execution in nanoseconds.
     */
    public void recordCommand(String name, boolean success, long durationNanos) {
        meterRegistry.counter("bot.command.invocations", "name", name, "success", Boolean.toString(success)).increment();
        meterRegistry.timer("bot.command.latency", "name", name, "success", Boolean.toString(success))
            .record(durationNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Records the invocation of a button interaction with its success status and duration.
     *
     * @param id            The identifier of the button.
     * @param success       Whether the button interaction was successful.
     * @param durationNanos The duration of the button interaction in nanoseconds.
     */
    public void recordButton(String id, boolean success, long durationNanos) {
        meterRegistry.counter("bot.button.invocations", "id", id, "success", Boolean.toString(success)).increment();
        meterRegistry.timer("bot.button.latency", "id", id, "success", Boolean.toString(success))
            .record(durationNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Records the invocation of a select interaction with its success status and duration.
     *
     * @param id            The identifier of the select interaction.
     * @param success       Whether the select interaction was successful.
     * @param durationNanos The duration of the select interaction in nanoseconds.
     */
    public void recordSelect(String id, boolean success, long durationNanos) {
        meterRegistry.counter("bot.select.invocations", "id", id, "success", Boolean.toString(success)).increment();
        meterRegistry.timer("bot.select.latency", "id", id, "success", Boolean.toString(success))
            .record(durationNanos, TimeUnit.NANOSECONDS);
    }
}
