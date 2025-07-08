package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.audio.exception.TrackLoadException;
import com.gammatunes.backend.audio.lavalink.LavalinkPlayer;
import com.gammatunes.backend.bot.audio.AudioPlayerSendHandler;
import com.gammatunes.backend.bot.command.Command;
import com.gammatunes.backend.bot.util.CommandUtil;
import com.gammatunes.backend.common.model.Session;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer; // Import the lavaplayer class
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Command to play a song or add it to the queue in the voice channel.
 * This command interacts with the audio service to handle track loading and playback.
 */
@Component
public class PlayCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(PlayCommand.class);
    private final AudioService audioService;

    public PlayCommand(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("play", "Plays a song or adds it to the queue.")
            .addOption(OptionType.STRING, "query", "The song URL or search term.", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!CommandUtil.preliminaryChecks(event)) {
            return;
        }

        String query = Objects.requireNonNull(event.getOption("query")).getAsString();

        try {
            connectToChannel(event);
            enqueueTrack(event, query);
            event.getHook().sendMessage("✅ Enqueued: `" + query + "`").queue();
        } catch (TrackLoadException e) {
            log.error("Failed to load track for guild {}: {}", event.getGuild().getId(), e.getMessage());
            event.getHook().sendMessage("❌ An error occurred while loading the track: " + e.getMessage()).queue();
        } catch (Exception e) {
            log.error("An unexpected error occurred in play command for guild {}", event.getGuild().getId(), e);
            event.getHook().sendMessage("❌ An unexpected error occurred.").queue();
        }
    }

    /**
     * Connects the bot to the user's voice channel and sets up the audio sending handler.
     */
    private void connectToChannel(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannel userChannel = voiceState.getChannel();
        String guildId = event.getGuild().getId();

        com.gammatunes.backend.audio.api.AudioPlayer player = audioService.getOrCreatePlayer(new Session(guildId));
        AudioPlayer realLavaPlayer = ((LavalinkPlayer) player).getRealPlayer();

        AudioManager audioManager = event.getGuild().getAudioManager();
        // Only set the handler if we're not already connected
        if (!audioManager.isConnected()) {
            audioManager.setSendingHandler(new AudioPlayerSendHandler(realLavaPlayer));
            audioManager.openAudioConnection(userChannel);
            log.info("Joining voice channel {} in guild {}", userChannel.getName(), guildId);
        }
    }

    /**
     * Calls the backend audio service to enqueue the requested track.
     */
    private void enqueueTrack(SlashCommandInteractionEvent event, String query) throws TrackLoadException {
        String guildId = event.getGuild().getId();
        audioService.play(new Session(guildId), query);
    }
}

