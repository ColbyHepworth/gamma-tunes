package com.gammatunes.backend.presentation.bot.player.view.factory;

import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.presentation.bot.player.view.PlayerPanel;
import com.gammatunes.backend.presentation.bot.player.view.renderer.ComponentRenderer;
import com.gammatunes.backend.presentation.bot.player.view.renderer.FieldRenderer;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlayerPanelFactory {

    private final List<FieldRenderer> fieldRenderers;
    private final List<ComponentRenderer> componentRenderers; // Now injects component renderers

    /**
     * Builds the complete PlayerPanel DTO, including the embed and all components.
     */
    public PlayerPanel buildPanel(AudioPlayer player, String status) {
        MessageEmbed embed = buildEmbed(player, status);
        List<ActionRow> components = buildComponents(player);
        return new PlayerPanel(embed, components);
    }

    /**
     * Builds the embed message for the player panel, including all fields.
     */
    private MessageEmbed buildEmbed(AudioPlayer player, String status) {
        var eb = new EmbedBuilder();
        fieldRenderers.forEach(r -> r.render(eb, player, status));
        return eb.build();
    }

    /**
     * Builds the interactive components (buttons, select menus) for the player message.
     */
    private List<ActionRow> buildComponents(AudioPlayer player) {
        return componentRenderers.stream()
            .flatMap(r -> r.render(player).stream())
            .collect(Collectors.toList());
    }
}
