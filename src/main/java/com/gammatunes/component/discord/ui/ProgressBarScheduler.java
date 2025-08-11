package com.gammatunes.component.discord.ui;

import com.gammatunes.model.domain.PlayerState;
import com.gammatunes.component.audio.core.PlayerStateStore;
import com.gammatunes.component.audio.events.PlayerUIState;
import com.gammatunes.component.audio.events.PlayerPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler that updates the progress bar for the Discord player UI.
 * It periodically checks the current position of the track and updates the visual representation
 * of the progress bar in the player panel.
 */
@Component
@RequiredArgsConstructor
public class ProgressBarScheduler {

    private final PlayerStateStore stateStore;
    private final PlayerPanelCache cache;

    @Scheduled(fixedRate = 1000)
    void tick() {
        long now = System.currentTimeMillis();

        for (Long guildId : cache.guildIds()) {
            PlayerUIState uiState = stateStore.getUI(guildId);
            PlayerPosition position = stateStore.getPosition(guildId);

            if (uiState != null && uiState.state() == PlayerState.PLAYING && position != null) {
                long pos = position.positionMs();
                long dur = position.lengthMs();

                // Get the previous and current visual index of the progress bar head.
                int prevHeadIdx = cache.getBarIdx(guildId);
                int headIdx = dur > 0 ? (int) (20 * pos / dur) : 0;

                // Check the time of the last edit.
                long prevEditTs = cache.getEditTs(guildId);
                long minInt = chooseInterval(dur);
                boolean intervalElapsed = now - prevEditTs >= minInt;

                // Only update if the interval has passed AND the bar has moved.
                if (intervalElapsed && headIdx != prevHeadIdx) {
                    cache.setBarIdx(guildId, headIdx);
                    cache.setEditTs(guildId, now);
                }
            }
        }
    }

    private long chooseInterval(long durMs) {
        return durMs <= 5 * 60_000 ? 5_000
            : durMs <= 15 * 60_000 ? 8_000
            : 12_000;
    }
}
