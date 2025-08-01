package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Renders the current track information in an embed.
 * Displays the track title, author, and thumbnail if available.
 * If no track is playing, it prompts the user to use the play command.
 */
@Component
@Order(1)
public final class TrackInfoRenderer implements FieldRenderer {

    private static final String LOGO_URL = "https://i.imgur.com/Aufpw9Q.png";

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String status) {

        eb.setThumbnail(LOGO_URL);

        player.getCurrentlyPlaying().ifPresentOrElse(track -> {
            eb.setTitle(track.title(), track.sourceUrl())
                .setDescription("by " + track.author());

            String art = track.thumbnailUrl();
            if (art != null && !art.isBlank()) {
                eb.setImage(art);
            }
        }, () -> eb.setTitle("No track playing")
            .setDescription("Use **/play** to add a song!"));
    }
}
