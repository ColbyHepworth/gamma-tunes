package com.gammatunes.component.discord.interaction.button.player;

import com.gammatunes.component.discord.interaction.button.AbstractButton;
import com.gammatunes.service.DiscordPlayerService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.entities.Member;
import reactor.core.publisher.Mono;

/**
 * Button to pause the currently playing track in the Discord player.
 * This button is part of the player controls and allows users to pause playback.
 */
@Component
@RequiredArgsConstructor
public class PauseButton extends AbstractButton {

    private final DiscordPlayerService controller;

    @Override public String id() { return "player:pause"; }

    @Override protected Mono<Void> handleWork(ButtonInteractionEvent buttonInteractionEvent, Member member) {
        return controller.pause(member).then();
    }
}
