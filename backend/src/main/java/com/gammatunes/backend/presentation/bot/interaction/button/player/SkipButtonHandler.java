package com.gammatunes.backend.presentation.bot.interaction.button.player;

import com.gammatunes.backend.presentation.bot.interaction.button.ButtonHandler;
import com.gammatunes.backend.presentation.bot.control.DiscordAudioController;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SkipButtonHandler implements ButtonHandler {

    private final DiscordAudioController discordAudioController;

    @Override
    public String id() {
        return "player:skip";
    }

    @Override
    public void handle(ButtonInteractionEvent event, Member member) {
        discordAudioController.skip(member);
        // TODO: Add success message handling
    }
}
