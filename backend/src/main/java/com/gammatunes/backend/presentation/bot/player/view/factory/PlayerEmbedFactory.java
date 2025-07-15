package com.gammatunes.backend.presentation.bot.player.view.factory;

import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.presentation.bot.player.view.renderer.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * A utility class for building the rich MessageEmbed for the player.
 */
@Component
public class PlayerEmbedFactory {

    private static final List<FieldRenderer> FIELD_RENDERERS = List.of(
        new TrackInfoRenderer(),
        new ProgressBarRenderer(),
        new StatusFieldRenderer()
    );
    private static final ControlsRenderer CONTROLS = new ControlsRenderer();

    /** Build the embed from the player plus the latest status text. */
    public MessageEmbed buildEmbed(AudioPlayer player, String statusText) {

        EmbedBuilder eb = new EmbedBuilder();
        FIELD_RENDERERS.forEach(r -> r.render(eb, player, statusText));
        return eb.build();
    }

    public List<Button> buildButtons(AudioPlayer player) {
        return CONTROLS.buildButtons(player);
    }
}



