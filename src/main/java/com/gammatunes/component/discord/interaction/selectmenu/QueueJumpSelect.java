package com.gammatunes.component.discord.interaction.selectmenu;

import com.gammatunes.service.DiscordPlayerService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QueueJumpSelect extends AbstractSelectMenu {

    private final DiscordPlayerService controller;

    @Override public String id() { return "player:queue-jump"; }

    @Override
    protected Mono<Void> handleWork(StringSelectInteractionEvent event, Member member, List<String> values) {
        if (values == null || values.isEmpty()) {
            return Mono.error(new IllegalArgumentException("No track selected."));
        }
        String trackIdentifier = values.getFirst();
        return controller.jumpToTrack(member, trackIdentifier);
    }
}
