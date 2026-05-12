package com.gammatunes.service.playback.control;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class PlaybackControlService {

    private final List<PlaybackControlHandler> handlers;

    public PlaybackControlService(List<PlaybackControlHandler> handlers) {
        this.handlers = handlers.stream()
            .sorted(Comparator.comparingInt(PlaybackControlHandler::order))
            .toList();
    }

    public Mono<Void> skip(Member member) {
        return handle(member.getGuild().getIdLong(), PlaybackControlAction.SKIP);
    }

    public Mono<Void> previous(Member member) {
        return handle(member.getGuild().getIdLong(), PlaybackControlAction.PREVIOUS);
    }

    public Mono<Void> pause(Member member) {
        return handle(member.getGuild().getIdLong(), PlaybackControlAction.PAUSE);
    }

    public Mono<Void> resume(Member member) {
        return handle(member.getGuild().getIdLong(), PlaybackControlAction.RESUME);
    }

    public Mono<Void> stop(Member member) {
        return handle(member.getGuild().getIdLong(), PlaybackControlAction.STOP);
    }

    public Mono<Void> handle(long guildId, PlaybackControlAction action) {
        return handle(guildId, action, 0)
            .then();
    }

    private Mono<PlaybackControlResult> handle(long guildId, PlaybackControlAction action, int index) {
        if (index >= handlers.size()) {
            return Mono.empty();
        }

        PlaybackControlHandler handler = handlers.get(index);
        if (!handler.supports(action)) {
            return handle(guildId, action, index + 1);
        }

        return handler.handle(guildId, action)
            .flatMap(result -> {
                log.debug(
                    "Playback control action={} guild={} handler={} result={}",
                    action,
                    guildId,
                    handler.getClass().getSimpleName(),
                    result
                );

                if (result == PlaybackControlResult.HANDLED) {
                    return Mono.just(result);
                }

                return handle(guildId, action, index + 1)
                    .defaultIfEmpty(result);
            });
    }
}
