package com.gammatunes.backend.presentation.bot.interaction.command;

import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.presentation.bot.player.service.PlayerMessageService;
import com.gammatunes.backend.presentation.bot.player.view.StatusMessageMapper;
import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class CommandInteractionHandler extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandInteractionHandler.class);
    private final Map<String, PlayerCommandHandler> playerCommands;
    private final Map<String, SimpleCommandHandler> commands;
    private final PlayerMessageService playerView;


    public CommandInteractionHandler(List<PlayerCommandHandler> playerBeans, List<SimpleCommandHandler> simpleBeans, PlayerMessageService       playerView) {

        this.playerCommands = playerBeans.stream()
            .collect(Collectors.toMap(CommandHandler::name, Function.identity()));

        this.commands = simpleBeans.stream()
            .collect(Collectors.toMap(CommandHandler::name, Function.identity()));

        this.playerView = playerView;

        log.info("Registered {} player commands, {} utility commands.",
            playerCommands.size(), playerCommands.size());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {

        String cmd = e.getName();
        try {
            if (playerCommands.containsKey(cmd)) {
                e.deferReply(true).queue();                       // ack
                PlayerOutcomeResult r = playerCommands.get(cmd).execute(e);

                String status = StatusMessageMapper.toStatus(r.outcome(), r.details());
                playerView.publishStatus(
                    new Session(Objects.requireNonNull(e.getGuild()).getId()), status);

                e.getHook().deleteOriginal().queue();

            } else if (commands.containsKey(cmd)) {
                commands.get(cmd).execute(e);

            } else {
                log.warn("Unknown slash command '{}'", cmd);
                e.reply("Unknown command.").setEphemeral(true).queue();
            }

        } catch (Exception ex) {
            log.error("Unhandled slash command '{}'", cmd, ex);
            e.reply("❌ Unexpected error – try again later.")
                .setEphemeral(true).queue();
        }
    }
}
