package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import java.util.List;

/**
 * Renders the interactive components (buttons, select menus) for the player message.
 */
public interface ComponentRenderer {
    /**
     * @param player The current audio player state.
     * @return A list of ActionRows containing the components for the message.
     */
    List<ActionRow> render(AudioPlayer player);
}
