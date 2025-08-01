package com.gammatunes.backend.presentation.bot.interaction.button;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.entities.Member;

/**
 * An interface that all button handlers will implement.
 * This interface is used to handle button interactions in the Discord bot.
 */
public interface Button {

    /**
     * Returns the unique identifier for the button.
     * This ID is used to differentiate between different buttons in the interaction event.
     *
     * @return The unique ID of the button.
     */
    String id();

    /**
     * Handles the button interaction event.
     * This method is called when a button is clicked in the Discord bot.
     *
     * @param event  The button interaction event containing details about the interaction.
     * @param member The member who clicked the button.
     * @return A {@link PlayerOutcome} indicating the result of the button interaction.
     */
    PlayerOutcome handle(ButtonInteractionEvent event, Member member);
}
