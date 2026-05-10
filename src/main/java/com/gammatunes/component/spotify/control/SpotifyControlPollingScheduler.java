package com.gammatunes.component.spotify.control;

import com.gammatunes.service.SpotifyControlPlaybackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotifyControlPollingScheduler {

    private final SpotifyControlPlaybackService spotifyControlPlaybackService;

    @Scheduled(fixedDelayString = "${gamma.spotify.control.poll-delay-ms:5000}")
    void poll() {
        spotifyControlPlaybackService.syncControlledGuilds()
            .doOnError(error -> log.warn("Spotify control polling failed: {}", error.toString()))
            .onErrorComplete()
            .subscribe();
    }
}
