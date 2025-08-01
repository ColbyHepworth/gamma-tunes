package com.gammatunes.backend.presentation.bot.interaction.button.player;

import com.gammatunes.backend.presentation.bot.interaction.button.Button;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShuffleButton implements Button {

    private final DiscordPlayerController discordPlayerController;

    @Override
    public String id() {
        return "player:shuffle";
    }

    @Override
    public void handle(ButtonInteractionEvent event, Member member) {
        discordPlayerController.shuffle(member);
    }
}
