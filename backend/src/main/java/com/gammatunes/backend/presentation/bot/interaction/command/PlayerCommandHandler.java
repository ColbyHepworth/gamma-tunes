package com.gammatunes.backend.presentation.bot.interaction.command;

import com.gammatunes.backend.infrastructure.source.exception.TrackLoadException;
import com.gammatunes.backend.presentation.bot.control.DiscordAudioController;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
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

    protected final DiscordAudioController discordAudioController;

    public PlayerCommandHandler(DiscordAudioController discordAudioController) {
        this.discordAudioController = discordAudioController;
    }

    /**
     * Runs the common checks and executes the player command.
     * This method is called when the command is invoked.
     * @param event The event containing the command interaction details.
     */
    @Override
    public final void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        try {
            Guild  g = event.getGuild();
            Member m = event.getMember();
            if (g == null || m == null) {
                event.getHook().sendMessage("❌ Guild only.").queue();
                return;
            }
            handle(m, event);                                // may throw
            event.getHook().editOriginal(getSuccessMessage()).queue();
        } catch (TrackLoadException x) {
            event.getHook().editOriginal("❌ Track failed: " + x.getMessage()).queue();
        } catch (MemberNotInVoiceChannelException x) {
            event.getHook().editOriginal("❌ Join a voice channel first.").queue();
        } catch (Exception x) {
            logger.error("Unhandled slash command error", x);
            event.getHook().editOriginal("❌ Unexpected error.").queue();
        }
    }


    protected abstract void handle(Member member, SlashCommandInteractionEvent event)
        throws TrackLoadException, MemberNotInVoiceChannelException;;

        /**
     * Returns the success message to be sent after executing the command.
     * Subclasses should implement this method to provide a specific success message.
     * @return The success message as a String.
     */
    protected abstract String getSuccessMessage();
}
