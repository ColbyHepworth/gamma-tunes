package com.gammatunes.backend.presentation.bot.interaction.button.player;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.presentation.bot.interaction.button.ButtonHandler;

import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.entities.Member;

@Component
@RequiredArgsConstructor
public class PauseButtonHandler implements ButtonHandler {

    private final DiscordPlayerController discordPlayerController;

    @Override
    public String id() {
        return "player:pause";
    }

    @Override
    public PlayerOutcomeResult handle(ButtonInteractionEvent event, Member member) {
        return new PlayerOutcomeResult(discordPlayerController.pause(member), null);
    }
}
