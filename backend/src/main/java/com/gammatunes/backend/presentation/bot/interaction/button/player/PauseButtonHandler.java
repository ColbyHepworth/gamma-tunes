package com.gammatunes.backend.presentation.bot.interaction.button.player;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordAudioController;
import com.gammatunes.backend.presentation.bot.interaction.button.ButtonHandler;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.entities.Member;

@Component
@RequiredArgsConstructor
public class PauseButtonHandler implements ButtonHandler {

    private final DiscordAudioController discordAudioController;

    @Override
    public String id() {
        return "player:pause";
    }

    @Override
    public void handle(ButtonInteractionEvent event, Member member) {
        discordAudioController.pause(member);
    }
}
