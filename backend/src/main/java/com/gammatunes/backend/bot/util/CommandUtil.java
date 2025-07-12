package com.gammatunes.backend.bot.util;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.audio.lavalink.LavalinkPlayer;
import com.gammatunes.backend.bot.audio.AudioPlayerSendHandler;
import com.gammatunes.backend.common.model.Session;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;

/**
 * A utility class for common command-related checks and actions.
 */
public final class CommandUtil {

    // Private constructor to prevent instantiation
    private CommandUtil() {}

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CommandUtil.class);

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

    /**
     * A helper method to get the AudioPlayer for the current guild.
     * @param audioService The audio service to use.
     * @param event The command event.
     * @return The AudioPlayer for the guild.
     */
    public static AudioPlayer getPlayer(AudioService audioService, SlashCommandInteractionEvent event) {
        String guildId = Objects.requireNonNull(event.getGuild()).getId();
        return audioService.getOrCreatePlayer(new Session(guildId));
    }

    public static AudioPlayer getPlayer(AudioService audioService, ButtonInteractionEvent event) {
        String guildId = Objects.requireNonNull(event.getGuild()).getId();
        return audioService.getOrCreatePlayer(new Session(guildId));
    }

    /**
     * Connects the bot to the user's voice channel and sets up the audio sending handler.
     */
    public static void connectToChannel(AudioService audioService, SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannel userChannel = voiceState.getChannel();
        String guildId = event.getGuild().getId();

        com.gammatunes.backend.audio.api.AudioPlayer player = audioService.getOrCreatePlayer(new Session(guildId));
        com.sedmelluq.discord.lavaplayer.player.AudioPlayer realLavaPlayer = ((LavalinkPlayer) player).getRealPlayer();

        AudioManager audioManager = event.getGuild().getAudioManager();
        // Only set the handler if we're not already connected
        if (!audioManager.isConnected()) {
            audioManager.setSendingHandler(new AudioPlayerSendHandler(realLavaPlayer));
            audioManager.openAudioConnection(userChannel);
            logger.info("Joining voice channel {} in guild {}", userChannel.getName(), guildId);
        } else {
            logger.info("Already connected to voice channel in guild {}", guildId);
        }
    }

}
