package com.gammatunes.backend.presentation.bot.interaction.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class CommandInteractionHandler extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandInteractionHandler.class);
    private final Map<String, CommandHandler> handlers;   // name → handler

    public CommandInteractionHandler(List<CommandHandler> beans) {
        handlers = beans.stream()
            .collect(Collectors.toMap(
                h -> h.getCommandData().getName(),   // "play"
                Function.identity()));
        log.info("Registered {} command handlers.", handlers.size());
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            log.warn("Slash command interaction from a non member user: {}", event.getUser().getName());
            event.reply("This command can only be used by members.").setEphemeral(true).queue();
            return;
        }
        CommandHandler handler = handlers.get(event.getName());
        if (handler == null) {
            log.warn("No handler found for command '{}'", event.getName());
            event.reply("Unknown command.").setEphemeral(true).queue();
            return;
        }
        try {
            log.info("Executing command '{}' for user {}", event.getName(), event.getUser().getName());
            handler.execute(event);
        } catch (Exception e) {
            log.error("An error occurred while executing command '{}'", event.getName(), e);
            event.reply("An unexpected error occurred. Please try again later.").setEphemeral(true).queue();
        }
    }
}
