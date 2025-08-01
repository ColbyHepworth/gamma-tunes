package com.gammatunes.backend.presentation.bot.player.gateway;

import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.presentation.bot.player.cache.MessageRef;
import com.gammatunes.backend.presentation.bot.player.view.factory.PlayerEmbedFactory;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Gateway for managing player panels in Discord.
 * This class provides methods to create, update, and delete player panels in a specified text channel.
 */
@Component
@RequiredArgsConstructor
public class PlayerPanelGateway {

    private static final Logger log = LoggerFactory.getLogger(PlayerPanelGateway.class);

    private final JDA jda;
    private final PlayerEmbedFactory embedFactory;

    /**
     * Creates a player panel message in the specified text channel.
     * This method sends an embed with the current player state and status, and adds action buttons for player controls.
     *
     * @param guildId The ID of the guild where the panel will be created.
     * @param channel The text channel where the panel will be posted.
     * @param player  The audio player instance containing the current state.
     * @param status  The initial status text to display in the panel.
     * @return A reference to the created message containing the panel.
     */
    public MessageRef createPanel(String guildId,
                                  TextChannel channel,
                                  AudioPlayer player,
                                  String status) {

        var action = channel.sendMessageEmbeds(embedFactory.buildEmbed(player, status))
            .addActionRow(embedFactory.buildPrimaryButtons(player))
            .addActionRow(embedFactory.buildSecondaryButtons(player));

        var msg = action.complete();  // choose queue() if you prefer async
        log.info("Created panel {} in channel {}", msg.getId(), channel.getId());
        return new MessageRef(guildId, channel.getId(), msg.getIdLong());
    }

    /**
     * Updates the player panel with the latest player state and status.
     *
     * @param ref    The reference to the message containing the player panel.
     * @param player The audio player instance with the current state.
     * @param status The latest status text to display in the panel.
     */
    public void updatePanel(MessageRef ref, AudioPlayer player, String status) {
        var channel = jda.getTextChannelById(ref.channelId());
        if (channel == null) return;

        channel.editMessageEmbedsById(ref.messageId(),
                embedFactory.buildEmbed(player, status))
            .setComponents(
                ActionRow.of(embedFactory.buildPrimaryButtons(player)),
                ActionRow.of(embedFactory.buildSecondaryButtons(player))
            )
            .queue(null, err -> log.warn("Failed to update panel {}", ref, err));
    }

    /**
     * Deletes the player panel message from the specified channel.
     *
     * @param ref The reference to the message containing the player panel to be deleted.
     */
    public void deletePanel(MessageRef ref) {
        var channel = jda.getTextChannelById(ref.channelId());
        if (channel != null) channel.deleteMessageById(ref.messageId()).queue();
    }
}
