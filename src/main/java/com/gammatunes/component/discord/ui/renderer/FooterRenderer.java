package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * FooterRenderer is responsible for rendering the footer of the player embed.
 * It displays the current status text, or the requester of the currently playing track.
 */
@Component
@Order(100)
public final class FooterRenderer implements FieldRenderer {

    /**
     * Renders the footer of the embed with either the provided status text or the requester of the current track.
     *
     * @param embedBuilder The EmbedBuilder to which the footer will be added.
     * @param playerView The current player view containing track and requester information.
     * @param statusText Optional status text to display in the footer.
     */
    @Override
    public void render(EmbedBuilder embedBuilder, PlayerView playerView, String statusText) {
        if (statusText != null && !statusText.isBlank()) {
            embedBuilder.setFooter(statusText);
            return;
        }

        playerView.currentTrack().flatMap(PlayerView.TrackView::requestedBy).ifPresent(requesterView -> {
            String footerText = "Queued by: " + requesterView.displayName();
            String avatarUrl = requesterView.avatarUrl();
            if (avatarUrl == null || avatarUrl.isBlank()) {
                embedBuilder.setFooter(footerText);
            } else {
                embedBuilder.setFooter(footerText, avatarUrl);
            }
        });
    }
}
