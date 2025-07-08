package com.gammatunes.backend.bot.listener;

import com.gammatunes.backend.bot.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Listens for slash command interactions and routes them to the appropriate command handler.
 */
public class CommandListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);
    private final Map<String, Command> commandMap;

    public CommandListener(List<Command> commands) {
        // Create a map of command names to command objects for easy lookup.
        this.commandMap = commands.stream()
            .collect(Collectors.toMap(cmd -> cmd.getCommandData().getName(), Function.identity()));
        log.info("Registered {} commands.", commandMap.size());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command command = commandMap.get(event.getName());
        if (command != null) {
            try {
                log.info("Executing command '{}' for user {}", event.getName(), event.getUser().getName());
                command.execute(event);
            } catch (Exception e) {
                log.error("An error occurred while executing command '{}'", event.getName(), e);
                event.reply("An unexpected error occurred. Please try again later.").setEphemeral(true).queue();
            }
        } else {
            log.warn("No handler found for command '{}'", event.getName());
            event.reply("Unknown command.").setEphemeral(true).queue();
        }
    }
}
