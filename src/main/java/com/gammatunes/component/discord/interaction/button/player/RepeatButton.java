package com.gammatunes.component.discord.interaction.button.player;

import com.gammatunes.component.discord.interaction.button.AbstractButton;
import com.gammatunes.service.DiscordPlayerService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Button to toggle repeat mode in the Discord player.
 * This button is part of the player controls and allows users to repeat the current track or playlist.
 */
@Component
@RequiredArgsConstructor
public class RepeatButton extends AbstractButton {

    private final DiscordPlayerService controller;

    @Override
    public String id() {
        return "player:repeat";
    }

    @Override
    protected Mono<Void> handleWork(ButtonInteractionEvent event, Member member) {
        return controller.toggleRepeat(member).then();
    }
}
