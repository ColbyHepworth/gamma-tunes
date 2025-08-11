package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Functional interface for rendering fields in an embed.
 * Implementations should define how to populate the embed with specific player information.
 */
@FunctionalInterface
public interface FieldRenderer {

    /**
     * Renders fields in the provided EmbedBuilder based on the current player view and status text.
     *
     * @param embedBuilder The EmbedBuilder to populate with fields.
     * @param playerView The current player view containing player state and information.
     * @param statusText Additional status text to include in the embed.
     */
    void render(EmbedBuilder embedBuilder, PlayerView playerView, String statusText);
}
