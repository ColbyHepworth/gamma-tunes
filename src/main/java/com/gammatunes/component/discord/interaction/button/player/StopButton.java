package com.gammatunes.component.discord.interaction.button.player;

import com.gammatunes.component.discord.interaction.button.AbstractButton;
import com.gammatunes.service.DiscordPlayerService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class StopButton extends AbstractButton {

    private final DiscordPlayerService controller;

    @Override
    public String id() {
        return "player:stop";
    }

    @Override
    protected Mono<Void> handleWork(ButtonInteractionEvent event, Member member) {
        return controller.stop(member).then();
    }
}
