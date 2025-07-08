package com.gammatunes.backend.bot.command;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.command.impl.player.ResumeCommand;
import com.gammatunes.backend.bot.util.CommandUtil;
import com.gammatunes.backend.common.model.Session;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Abstract base class for commands that interact with the audio player.
 * Provides common functionality for executing player commands and sending success messages.
 */
public abstract class PlayerCommand implements Command {

    protected final AudioService audioService;
    private static final Logger log = LoggerFactory.getLogger(PlayerCommand.class);


    public PlayerCommand(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public final void execute(SlashCommandInteractionEvent event) {
        if (!CommandUtil.preliminaryChecks(event)) {
            return;
        }

        String guildId = Objects.requireNonNull(event.getGuild()).getId();
        AudioPlayer player = audioService.getOrCreatePlayer(new Session(guildId));

        log.info("Executing command '{}' for user {}", event.getName(), event.getUser().getName());
        executePlayerCommand(player);
        event.getHook().sendMessage(getSuccessMessage()).queue();
    }

    protected abstract void executePlayerCommand(AudioPlayer player);

    protected abstract String getSuccessMessage();
}
