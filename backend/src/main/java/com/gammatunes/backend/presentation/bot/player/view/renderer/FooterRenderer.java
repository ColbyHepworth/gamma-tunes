package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Renders the footer of the embed with a status message.
 * This is typically used to display additional information or status updates.
 */
@Component
@Order(100)
public final class FooterRenderer implements FieldRenderer {

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String statusText) {
        if (statusText == null || statusText.isBlank()) {
            return;
        }

        player.getCurrentItem().ifPresentOrElse(item -> {
            var requester = item.requester();
            String footerText = "Queued by: " + requester.name();
            eb.setFooter(footerText, requester.avatarUrl());
        }, () -> {
            eb.setFooter(statusText);
        });
    }
}
