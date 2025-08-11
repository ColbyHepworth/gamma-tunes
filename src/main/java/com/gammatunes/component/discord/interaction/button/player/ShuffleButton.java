package com.gammatunes.component.discord.interaction.button.player;

import com.gammatunes.component.discord.interaction.button.AbstractButton;
import com.gammatunes.service.DiscordPlayerService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Button to shuffle the current queue in the Discord player.
 * This button is part of the player controls and allows users to randomize the order of tracks in the queue.
 */
@Component
@RequiredArgsConstructor
public class ShuffleButton extends AbstractButton {

    private final DiscordPlayerService controller;

    @Override
    public String id() {
        return "player:shuffle";
    }

    @Override
    protected Mono<Void> handleWork(ButtonInteractionEvent event, Member member) {
        return controller.shuffle(member).then();
    }
}

