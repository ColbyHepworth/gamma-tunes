package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.audio.exception.TrackLoadException;
import com.gammatunes.backend.bot.command.Command;
import com.gammatunes.backend.bot.util.CommandUtil;
import com.gammatunes.backend.common.model.Session;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PlayNowCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(PlayNowCommand.class);
    private final AudioService audioService;

    public PlayNowCommand(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("playnow", "Plays the current track immediately in the voice channel.")
            .addOption(OptionType.STRING, "query", "The song URL or search term.", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!CommandUtil.preliminaryChecks(event)) {
            return;
        }

        String query = Objects.requireNonNull(event.getOption("query")).getAsString();

        try {
            CommandUtil.connectToChannel(audioService, event);
            String guildId = event.getGuild().getId();
            audioService.playNow(new Session(guildId), query);
            event.getHook().sendMessage("✅ Playing now: `" + query + "`").queue();
        } catch (TrackLoadException e) {
            logger.error("Failed to load track for guild {}: {}", event.getGuild().getId(), e.getMessage());
            event.getHook().sendMessage("❌ An error occurred while loading the track: " + e.getMessage()).queue();
        } catch (Exception e) {
            logger.error("An unexpected error occurred in play command for guild {}", event.getGuild().getId(), e);
            event.getHook().sendMessage("❌ An unexpected error occurred.").queue();
        }
    }
}
