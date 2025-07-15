package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Renders one field of the player embed.
 * <p>Most renderers ignore {@code statusText} – they just receive it so the
 * {@link StatusFieldRenderer} can use it without a special case in the factory.</p>
 */
@FunctionalInterface
public interface FieldRenderer {

    /**
     * @param eb          embed builder to mutate
     * @param player      audio player snapshot
     * @param statusText  latest one-line status (possible{@code null})
     */
    void render(EmbedBuilder eb, AudioPlayer player, String statusText);
}
