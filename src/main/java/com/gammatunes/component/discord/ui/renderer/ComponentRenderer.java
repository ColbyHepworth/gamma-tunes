package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.List;

/**
 * Functional interface for rendering interactive components (buttons, select menus) in the player view.
 * Implementations should define how to create and return a list of ActionRows based on the current player view.
 */
public interface ComponentRenderer {

    /**
     * Renders the interactive components for the player view.
     *
     * @param playerView The current player view containing player state and information.
     * @return A list of ActionRows containing the interactive components.
     */
    List<ActionRow> render(PlayerView playerView);
}
