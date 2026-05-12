package com.gammatunes.service.playback.control;

import com.gammatunes.service.playback.PlaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LavalinkPlaybackControlHandler implements PlaybackControlHandler {

    private final PlaybackService playbackService;

    @Override
    public int order() {
        return 1_000;
    }

    @Override
    public boolean supports(PlaybackControlAction action) {
        return true;
    }

    @Override
    public Mono<PlaybackControlResult> handle(long guildId, PlaybackControlAction action) {
        Mono<Void> control = switch (action) {
            case SKIP -> playbackService.skip(guildId);
            case PREVIOUS -> playbackService.previous(guildId);
            case PAUSE -> playbackService.pause(guildId);
            case RESUME -> playbackService.resume(guildId);
            case STOP -> playbackService.stop(guildId);
        };

        return control.thenReturn(PlaybackControlResult.HANDLED);
    }
}
