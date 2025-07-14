package com.gammatunes.backend.presentation.bot.util;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for common command-related checks and actions.
 */
public final class CommandUtil {

    // Private constructor to prevent instantiation
    private CommandUtil() {}

    private static final Logger logger = LoggerFactory.getLogger(CommandUtil.class);

    /**
     * Performs preliminary checks for any command that requires the user to be in a voice channel.
     * It defers the reply and checks if the user is in a voice channel.
     *
     * @param event The command event.
     * @return true if all checks pass, false otherwise. If it returns false, a reply has already been sent.
     */
    public static boolean preliminaryChecks(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Member member = event.getMember();
        if (member == null) {
            logger.info("Member is not in a voice channel.");
            event.getHook().sendMessage("❌ This command can only be used in a server.").queue();
            return false;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            logger.info("Member is not in a voice channel.");
            event.getHook().sendMessage("❌ You must be in a voice channel to use this command.").queue();
            return false;
        }
        logger.info("Member is in a voice channel.");
        return true;
    }
}
