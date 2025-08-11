package com.gammatunes.component.discord.interaction.command;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CommandInteractionHandler extends ListenerAdapter {

    private final Map<String, BotCommand> commands;

    public CommandInteractionHandler(List<BotCommand> commands) {
        this.commands = commands.stream().collect(Collectors.toMap(BotCommand::name, Function.identity()));
        log.info("Registered {} commands", this.commands.size());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var guild = event.getGuild();
        log.info("Slash in guild={} channel={} type={}",
            guild != null ? guild.getId() : "DM",
            event.getChannel().getId(),
            event.getChannel().getType());

        BotCommand command = commands.get(event.getName());
        if (command == null) {
            event.reply("Unknown command.").setEphemeral(true).queue();
            return;
        }

        command.execute(event)
            .doOnError(err -> log.error("Error executing command '{}'", event.getName(), err))
            .subscribe();
    }
}
