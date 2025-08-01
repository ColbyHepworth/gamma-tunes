package com.gammatunes.backend.presentation.bot.interaction.button.player;

import com.gammatunes.backend.presentation.bot.interaction.button.Button;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.entities.Member;

@Component
@RequiredArgsConstructor
public class RepeatButton implements Button {

    private final DiscordPlayerController discordPlayerController;

    public String id() {
        return "player:repeat";
    }

    public void handle(ButtonInteractionEvent event, Member member) {
        discordPlayerController.toggleRepeat(member);
    }
}
