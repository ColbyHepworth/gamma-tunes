package com.gammatunes.backend.presentation.bot.interaction.button;


import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.entities.Member;

/**
 * An interface that all button handlers will implement.
 * This interface is used to handle button interactions in the Discord bot.
 */
public interface ButtonHandler {

    String id();

    PlayerOutcomeResult handle(ButtonInteractionEvent event, Member member);
}
