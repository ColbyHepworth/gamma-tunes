package com.gammatunes.component.discord.interaction.button.player;

import com.gammatunes.component.discord.interaction.button.AbstractButton;
import com.gammatunes.service.DiscordPlayerService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Button to skip to the previous track in the Discord player.
 * This button is part of the player controls and allows users to go back to the last track.
 */
@Component
@RequiredArgsConstructor

public class PreviousButton extends AbstractButton {

    private final DiscordPlayerService controller;

    @Override
    public String id() {
        return "player:previous";
    }

    @Override
    protected Mono<Void> handleWork(ButtonInteractionEvent event, Member member) {
        return controller.previous(member).then();
    }
}
