package com.gammatunes.backend.bot.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * An interface that all bot commands will implement.
 */
public interface Command {
    /**
     * @return The command data used to register the slash command with Discord.
     */
    CommandData getCommandData();

    /**
     * The logic to be executed when the command is invoked.
     * @param event The event containing all the details of the command interaction.
     */
    void execute(SlashCommandInteractionEvent event);
}
