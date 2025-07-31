package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Component;

@Component
public final class TrackInfoRenderer implements FieldRenderer {

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String status) {

        var opt = player.getCurrentlyPlaying();
        if (opt.isEmpty()) {
            eb.setTitle("No track playing")
                .setDescription("Use **/play** to add a song!");       // colour elsewhere
            return;
        }
        var t = opt.get();

        eb.setTitle("Now Playing: " + t.title(), t.sourceUrl())
            .setDescription("by " + t.author());

        String imageUrl = t.thumbnailUrl();

        if (imageUrl != null) {
            eb.setImage(imageUrl);
        }
    }
}
