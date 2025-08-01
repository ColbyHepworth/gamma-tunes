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
    private static final String YOUTUBE_ICON_URL = "https://img.freepik.com/premium-vector/youtube-icon-illustration-youtube-app-logo-social-media-icon_561158-3674.jpg";
    private static final String SPOTIFY_ICON_URL = "https://www.citypng.com/public/uploads/preview/square-black-green-spotify-app-icon-png-701751694969849j7wtxvnrgo.png";


    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String status) {
        eb.setThumbnail(LOGO_URL);

        player.getCurrentItem().ifPresentOrElse(item -> {
            var track = item.track();

            String cleanedTitle = cleanTitle(track.title());

            eb.setTitle(cleanedTitle, track.sourceUrl());


            setSourceAuthor(eb, track.sourceUrl());

            String art = track.thumbnailUrl();
            if (art != null && !art.isBlank()) {
                eb.setImage(art);
            }
        }, () -> {
            eb.setTitle("No track playing")
                .setDescription("Use **/play** to add a song!");
            eb.setAuthor(null);
        });
    }

    private void setSourceAuthor(EmbedBuilder eb, String url) {
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            eb.setAuthor("YouTube", null, YOUTUBE_ICON_URL);
        } else if (url.contains("spotify.com")) {
            eb.setAuthor("Spotify", null, SPOTIFY_ICON_URL);
        }
        // Add more sources like SoundCloud here if you support them
    }

    private String cleanTitle(String title) {
        return title.replaceAll("(?i)\\s*\\(official.*video\\)|\\s*\\(official.*audio\\)|\\s*\\[official.*]|\\s*\\(audio\\)|\\s*\\(4k.*\\)|\\s*\\(hd\\)", "").trim();
    }
}
