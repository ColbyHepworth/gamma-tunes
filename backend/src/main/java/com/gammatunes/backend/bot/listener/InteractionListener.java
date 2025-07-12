package com.gammatunes.backend.bot.listener;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.command.Command;
import com.gammatunes.backend.bot.util.CommandUtil;
import com.gammatunes.backend.bot.view.PlayerMessageManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Listens for slash command interactions and routes them to the appropriate command handler.
 */
@Component
public class InteractionListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(InteractionListener.class);
    private final Map<String, Command> commandMap;
    private final AudioService audioService;
    private final PlayerMessageManager playerMessageManager;


    public InteractionListener(List<Command> commands, AudioService audioService, PlayerMessageManager playerMessageManager) {
        // Create a map of command names to command objects for easy lookup.
        this.commandMap = commands.stream()
            .collect(Collectors.toMap(cmd -> cmd.getCommandData().getName(), Function.identity()));
        log.info("Registered {} commands.", commandMap.size());

        this.audioService = audioService;
        this.playerMessageManager = playerMessageManager;
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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        Member member = event.getMember();

        if (member == null) {
            log.warn("Button interaction received without a member.");
            event.reply("You must be a member of the server to use this button.").setEphemeral(true).queue();
            return;
        }

        if (!parts[0].equals("player")) {
            log.warn("Received button interaction with unknown prefix: {}", parts[0]);
            event.reply("Unknown action.").setEphemeral(true).queue();
            return;
        }
        log.info("Received button interaction for player action: {}", parts[1]);

        event.deferEdit().queue(); // Acknowledge the button click

        AudioPlayer player = CommandUtil.getPlayer(audioService, event);

        switch (parts[1]) {
            case "previous" -> player.previous();
            case "pause" -> player.pause();
            case "skip" -> player.skip();
            case "resume" -> player.resume();
            case "stop" -> {
                player.stopAndClear();
                Objects.requireNonNull(event.getGuild()).getAudioManager().closeAudioConnection();
            }
        }
        playerMessageManager.update(player, event.getGuild().getId());
    }
}
