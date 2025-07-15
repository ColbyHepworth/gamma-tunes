package com.gammatunes.backend.presentation.bot.interaction.button.player;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.presentation.bot.interaction.button.ButtonHandler;

import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResumeButtonHandler implements ButtonHandler {

    private final DiscordPlayerController discordPlayerController;

    @Override
    public String id() {
        return "player:resume";
    }

    @Override
    public PlayerOutcomeResult handle(ButtonInteractionEvent event, Member member) {
        return new PlayerOutcomeResult(discordPlayerController.resume(member), null);
    }
}
