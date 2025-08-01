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

    private final List<FieldRenderer> fieldRenderers;

    public PlayerEmbedFactory(List<FieldRenderer> fieldRenderers) {
        this.fieldRenderers = fieldRenderers;
    }

    public MessageEmbed buildEmbed(AudioPlayer player, String status) {
        var eb = new EmbedBuilder();
        fieldRenderers.forEach(r -> r.render(eb, player, status));
        return eb.build();
    }

    public List<Button> buildPrimaryButtons(AudioPlayer player) {
        return new ControlsRenderer().buildPrimaryButtons(player);
    }

    public List<Button> buildSecondaryButtons(AudioPlayer player) {
        return new ControlsRenderer().buildSecondaryButtons(player);
    }
}


