package com.gammatunes.backend.presentation.bot.player.service;

import com.gammatunes.backend.application.port.out.PlayerRegistryPort;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.presentation.bot.player.cache.PlayerPanelCache;
import com.gammatunes.backend.presentation.bot.player.gateway.PlayerPanelGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgressBarScheduler {

    private final PlayerRegistryPort registry;
    private final PlayerPanelCache cache;
    private final PlayerPanelGateway gateway;

    @Scheduled(fixedRate = 1000)
    void tick() {
        long now = System.currentTimeMillis();

        for (String guildId : cache.guildIds()) {

            AudioPlayer player = registry.getOrCreatePlayer(new Session(guildId));
            long pos = player.getTrackPosition();
            long dur = player.getCurrentlyPlaying()
                .map(t -> t.duration().toMillis())
                .orElse(0L);
            if (dur <= 0) continue;

            int headIdx = (int) (20 * pos / dur);

            long prevEditTs = cache.getEditTs(guildId);
            long minInt     = chooseInterval(dur);

            boolean intervalElapsed = now - prevEditTs >= minInt;

            if (intervalElapsed) {
                cache.setBarIdx(guildId, headIdx);
                cache.setEditTs(guildId, now);

                cache.getMessage(guildId).ifPresent(ref ->
                    gateway.updatePanel(ref, player, cache.getStatus(guildId))
                );
            }
        }
    }

    private long chooseInterval(long durMs) {
        return durMs <= 5 * 60_000 ? 5_000
            : durMs <= 15 * 60_000 ? 8_000
            : 12_000;
    }
}
