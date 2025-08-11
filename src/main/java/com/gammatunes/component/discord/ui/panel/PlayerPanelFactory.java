package com.gammatunes.component.discord.ui.panel;

import com.gammatunes.component.audio.events.PlayerUIState;
import com.gammatunes.component.audio.events.PlayerPosition;
import com.gammatunes.model.dto.PlayerView;
import com.gammatunes.component.discord.ui.renderer.ComponentRenderer;
import com.gammatunes.component.discord.ui.renderer.FieldRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory class to create a PlayerPanel for displaying the player state in Discord.
 * It builds the embed message and interactive components based on the current player state.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerPanelFactory {

    private final List<FieldRenderer> fieldRenderers;
    private final List<ComponentRenderer> componentRenderers;

    /**
     * Builds a PlayerPanel based on the provided UI state, player position, and status text.
     *
     * @param uiState The current player UI state containing player information.
     * @param position The position of the player in the guild.
     * @param statusText Additional status text to display in the embed.
     * @return A PlayerPanel containing the embed and components for the player view.
     */
    public PlayerPanel buildPanel(PlayerUIState uiState, PlayerPosition position, String statusText) {
        PlayerView playerView = PlayerViewMapper.toView(uiState, position);
        MessageEmbed embed = buildEmbed(playerView, statusText);
        List<ActionRow> components = buildComponents(playerView);
        log.debug("Panel render for guild {} state={} repeat={} queueSize={}",
            uiState.guildId(), playerView.state(), playerView.repeatEnabled(), playerView.queue().size());
        return new PlayerPanel(embed, components);
    }

    /**
     * Builds the embed message for the player view.
     *
     * @param playerView The current player view containing player state and information.
     * @param statusText Additional status text to display in the embed.
     * @return A MessageEmbed containing the player information.
     */
    private MessageEmbed buildEmbed(PlayerView playerView, String statusText) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        fieldRenderers.forEach(renderer -> renderer.render(embedBuilder, playerView, statusText));
        return embedBuilder.build();
    }

    /**
     * Builds the interactive components for the player view.
     *
     * @param playerView The current player view containing player state and information.
     * @return A list of ActionRows containing the interactive components.
     */
    private List<ActionRow> buildComponents(PlayerView playerView) {
        return componentRenderers.stream()
            .flatMap(renderer -> renderer.render(playerView).stream())
            .collect(Collectors.toList());
    }
}
