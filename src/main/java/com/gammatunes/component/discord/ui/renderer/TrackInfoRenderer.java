package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Renderer for displaying track information in the Discord player view.
 * This component formats the current track's title, author, and artwork,
 * and sets appropriate icons based on the source (YouTube or Spotify).
 */
@Component
@Order(1)
public final class TrackInfoRenderer implements FieldRenderer {

    private static final String LOGO_URL = "https://i.imgur.com/Aufpw9Q.png";
    private static final String YOUTUBE_ICON_URL = "https://img.freepik.com/premium-vector/youtube-icon-illustration-youtube-app-logo-social-media-icon_561158-3674.jpg";
    private static final String SPOTIFY_ICON_URL = "https://www.citypng.com/public/uploads/preview/square-black-green-spotify-app-icon-png-701751694969849j7wtxvnrgo.png";

    /**
     * Unique identifier for this field renderer.
     * Used to identify the component in interactions.
     */
    @Override
    public void render(EmbedBuilder embedBuilder, PlayerView playerView, String statusText) {
        embedBuilder.setThumbnail(LOGO_URL);

        playerView.currentTrack().ifPresentOrElse(trackView -> {
            String cleanedTitle = cleanTitle(trackView.title());
            embedBuilder.setTitle(cleanedTitle, trackView.uri());

            setSourceAuthor(embedBuilder, trackView.author());

            String artworkUrl = trackView.artworkUrl();
            if (artworkUrl != null && !artworkUrl.isBlank()) {
                embedBuilder.setImage(artworkUrl);
            }
        }, () -> {
            embedBuilder.setTitle("No track playing")
                .setDescription("Use **/play** to add a song!");
            embedBuilder.setAuthor(null);
        });
    }

    /**
     * Sets the author of the embed based on the source of the track.
     * If the author contains "youtube", it sets the author to YouTube with its icon.
     * If it contains "spotify", it sets the author to Spotify with its icon.
     *
     * @param embedBuilder The EmbedBuilder to modify.
     * @param author       The author string from the track.
     */
    private void setSourceAuthor(EmbedBuilder embedBuilder, String author) {
        String source = author == null ? "" : author.toLowerCase();
        if (source.contains("youtube")) {
            embedBuilder.setAuthor("YouTube", null, YOUTUBE_ICON_URL);
        } else if (source.contains("spotify")) {
            embedBuilder.setAuthor("Spotify", null, SPOTIFY_ICON_URL);
        }
    }

    /**
     * Cleans the track title by removing common suffixes like "(official video)", "(audio)", etc.
     *
     * @param title The original track title.
     * @return The cleaned title without unnecessary suffixes.
     */
    private String cleanTitle(String title) {
        return title.replaceAll("(?i)\\s*\\(official.*video\\)|\\s*\\(official.*audio\\)|\\s*\\[official.*]|\\s*\\(audio\\)|\\s*\\(4k.*\\)|\\s*\\(hd\\)", "")
            .trim();
    }
}
