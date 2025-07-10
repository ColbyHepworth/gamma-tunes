package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.bot.command.Command;
import com.gammatunes.backend.bot.util.CommandUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Component;


/**
 * Command to stop the audio player, clear the queue, and disconnect the bot from the voice channel.
 * This command interacts with the audio service to manage playback and connection state.
 */
@Component
public class StopCommand implements Command {

    private final AudioService audioService;

    public StopCommand(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("stop", "Stops the player, clears the queue, and disconnects the bot.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        event.deferReply().queue();

        // 1. Get the player and tell the backend to stop and clear everything.
        AudioPlayer player = CommandUtil.getPlayer(audioService, event);
        player.stopAndClear();

        // 2. Get the JDA audio manager and tell it to disconnect.
        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.closeAudioConnection();

        event.getHook().sendMessage("⏹️ Player stopped and disconnected.").queue();
    }
}
