package com.gammatunes.backend.presentation.bot.interaction.command;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.infrastructure.source.exception.TrackLoadException;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for commands that interact with the audio player.
 * Provides common functionality for executing player commands and sending success messages.
 */
public abstract class PlayerCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCommandHandler.class);

    protected final DiscordPlayerController discordPlayerController;


    public PlayerCommandHandler(DiscordPlayerController discordPlayerController) {
        this.discordPlayerController = discordPlayerController;
    }

    /**
     * Returns the command data for this player command.
     * Subclasses should implement this method to provide specific command details.
     *
     * @return PlayerOutcomeResult for the slash command
     */
    public final PlayerOutcomeResult execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        try {
            Guild  g = event.getGuild();
            Member m = event.getMember();
            if (g == null || m == null) {
                event.getHook().sendMessage("❌ Guild only.").queue();
                return new PlayerOutcomeResult(PlayerOutcome.ERROR, null);
            }
            return handle(m, event);
        } catch (TrackLoadException x) {
            logger.error(x.getMessage(), x);
            return new PlayerOutcomeResult(PlayerOutcome.ERROR, "❌ Track failed: " + x.getMessage());
        } catch (MemberNotInVoiceChannelException x) {
            logger.warn("Member not in voice channel: {}", x.getMessage());
            return new PlayerOutcomeResult(PlayerOutcome.ERROR, null);
        } catch (Exception x) {
            logger.error("Unhandled slash command error", x);
            return new PlayerOutcomeResult(PlayerOutcome.ERROR, null);
        }
    }

    protected abstract PlayerOutcomeResult handle(Member member, SlashCommandInteractionEvent event)
        throws TrackLoadException, MemberNotInVoiceChannelException;;
}
