package com.gammatunes.backend.presentation.bot.interaction.command;

import com.gammatunes.backend.presentation.bot.interaction.command.player.PlayerCommand;
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

    private final Map<String, PlayerCommand> commands;

    public CommandInteractionHandler(List<PlayerCommand> commands) {

        this.commands = commands.stream().collect(Collectors.toMap(BotCommand::name, Function.identity()));

        log.info("Registered {} commands", this.commands.size());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        String cmd = event.getName();
        try {
            if (commands.containsKey(cmd)) {
                commands.get(cmd).execute(event);
            } else {
                log.warn("Unknown slash command '{}'", cmd);
                event.reply("Unknown command.").setEphemeral(true).queue();
            }
        } catch (Exception ex) {
            log.error("Unhandled slash command '{}'", cmd, ex);
            event.reply("❌ Unexpected error – try again later.")
                .setEphemeral(true).queue();
        }
    }
}
