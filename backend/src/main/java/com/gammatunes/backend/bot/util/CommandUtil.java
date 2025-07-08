package com.gammatunes.backend.bot.util;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * A utility class for common command-related checks and actions.
 */
public final class CommandUtil {

    // Private constructor to prevent instantiation
    private CommandUtil() {}

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
            event.getHook().sendMessage("❌ This command can only be used in a server.").queue();
            return false;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            event.getHook().sendMessage("❌ You must be in a voice channel to use this command.").queue();
            return false;
        }

        return true;
    }
}
