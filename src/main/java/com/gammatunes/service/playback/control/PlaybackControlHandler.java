package com.gammatunes.service.playback.control;

import reactor.core.publisher.Mono;

public interface PlaybackControlHandler {

    int order();

    boolean supports(PlaybackControlAction action);

    Mono<PlaybackControlResult> handle(long guildId, PlaybackControlAction action);
}
