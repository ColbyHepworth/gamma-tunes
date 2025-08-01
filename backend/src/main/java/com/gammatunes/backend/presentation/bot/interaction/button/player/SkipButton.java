package com.gammatunes.backend.presentation.bot.interaction.button.player;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.presentation.bot.interaction.button.Button;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SkipButton implements Button {

    private final DiscordPlayerController discordPlayerController;

    @Override
    public String id() {
        return "player:skip";
    }

    @Override
    public PlayerOutcome handle(ButtonInteractionEvent event, Member member) {
        return discordPlayerController.skip(member);
    }
}
