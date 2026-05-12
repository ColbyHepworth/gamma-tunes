package com.gammatunes.service;

import com.gammatunes.component.spotify.api.request.SpotifyPausePlaybackRequest;
import com.gammatunes.component.spotify.api.request.SpotifyPlaybackVolumeRequest;
import com.gammatunes.component.spotify.api.request.SpotifyStartPlaybackRequest;
import com.gammatunes.component.spotify.api.response.SpotifyDevice;
import com.gammatunes.component.spotify.control.SpotifyControlAudioSnapshot;
import com.gammatunes.component.spotify.control.SpotifyControlSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyControlAudioGuard {

    private final SpotifyPlayerService spotifyPlayerService;

    public Mono<SpotifyControlAudioSnapshot> capture(long discordUserId) {
        return spotifyPlayerService.getPlaybackState(discordUserId)
            .flatMap(playbackState -> Mono.justOrEmpty(playbackState.device())
                .map(device -> capture(device, playbackState.isPlaying())))
            .defaultIfEmpty(new SpotifyControlAudioSnapshot(null, null, false));
    }

    public Mono<Void> mute(SpotifyControlSession session) {
        if (session.spotifyDeviceId() == null) {
            return Mono.error(new IllegalStateException("No active Spotify device is available to mute."));
        }
        if (session.originalVolume() == null) {
            return Mono.error(new IllegalStateException("The active Spotify device does not support volume control."));
        }

        return spotifyPlayerService.setPlaybackVolume(
            session.controllingDiscordUserId(),
            new SpotifyPlaybackVolumeRequest(0, session.spotifyDeviceId())
        );
    }

    public Mono<Void> resume(SpotifyControlSession session) {
        if (session.spotifyDeviceId() == null) {
            return Mono.error(new IllegalStateException("No active Spotify device is available to sync."));
        }

        return spotifyPlayerService.startPlayback(
            session.controllingDiscordUserId(),
            new SpotifyStartPlaybackRequest(session.spotifyDeviceId(), null, null, null, null)
        );
    }

    public Mono<Void> pauseIfResumedByControlStart(SpotifyControlSession session) {
        if (!session.resumedByControlStart() || session.originallyPlaying()) {
            return Mono.empty();
        }

        return pause(session);
    }

    public Mono<Void> restoreVolume(SpotifyControlSession session) {
        if (session.originalVolume() == null || session.spotifyDeviceId() == null) {
            return Mono.empty();
        }

        return restoreVolume(session.controllingDiscordUserId(), session.spotifyDeviceId(), session.originalVolume());
    }

    public Mono<Void> release(SpotifyControlSession session) {
        Mono<Void> pause = pause(session);
        Mono<Void> restore = session.originalVolume() != null && session.spotifyDeviceId() != null
            ? restoreVolume(session.controllingDiscordUserId(), session.spotifyDeviceId(), session.originalVolume())
            : Mono.empty();

        return pause.then(restore);
    }

    private Mono<Void> pause(SpotifyControlSession session) {
        long discordUserId = session.controllingDiscordUserId();
        return spotifyPlayerService.pausePlayback(
                discordUserId,
                new SpotifyPausePlaybackRequest(session.spotifyDeviceId())
            )
            .onErrorResume(error -> {
                log.warn("Could not pause Spotify for user {}", discordUserId, error);
                return Mono.empty();
            });
    }

    private SpotifyControlAudioSnapshot capture(SpotifyDevice device, boolean playing) {
        if (device == null || !device.supportsVolume()) {
            return new SpotifyControlAudioSnapshot(deviceId(device), null, playing);
        }

        String deviceId = deviceId(device);
        Integer originalVolume = device.volumePercent().orElse(null);
        if (deviceId == null || originalVolume == null) {
            return new SpotifyControlAudioSnapshot(deviceId, null, playing);
        }

        return new SpotifyControlAudioSnapshot(deviceId, originalVolume, playing);
    }

    private Mono<Void> restoreVolume(long discordUserId, String deviceId, int volume) {
        return spotifyPlayerService.setPlaybackVolume(
                discordUserId,
                new SpotifyPlaybackVolumeRequest(volume, deviceId)
            )
            .onErrorResume(error -> {
                log.warn("Could not restore Spotify volume for user {}", discordUserId, error);
                return Mono.empty();
            });
    }

    private String deviceId(SpotifyDevice device) {
        if (device == null) {
            return null;
        }
        return device.id().orElse(null);
    }
}
