package com.gammatunes.backend.presentation.bot.player.view;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import java.util.List;

/**
 * A data object representing the complete visual state of the player panel,
 * including the embed and all interactive components.
 */
public record PlayerPanel(
    MessageEmbed embed,
    List<ActionRow> components
) {}
