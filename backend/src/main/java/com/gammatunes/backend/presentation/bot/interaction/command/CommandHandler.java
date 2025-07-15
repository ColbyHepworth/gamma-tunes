package com.gammatunes.backend.presentation.bot.interaction.command;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;


/**
 * An interface that all bot commands will implement.
 */
public interface CommandHandler {

    CommandData getCommandData();

    default String name() {
        return getCommandData().getName();
    }
}
