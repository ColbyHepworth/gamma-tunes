package com.gammatunes.component.discord.interaction.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import reactor.core.publisher.Mono;

public interface BotCommand {

    /**
     * The main execution logic for the command.
     * @param event The JDA slash command event.
     */
    Mono<Void> execute(SlashCommandInteractionEvent event);

    /**
     * @return The JDA command data for registration.
     */
    CommandData getCommandData();

    /**
     * @return The name of the command.
     */
    default String name() {
        return getCommandData().getName();
    }
}
