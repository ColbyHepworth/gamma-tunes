package com.gammatunes.backend.presentation.bot.voice;

import com.gammatunes.backend.application.port.out.PlayerRegistryPort;
import com.gammatunes.backend.application.port.out.VoiceGateway;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.model.VoiceConnectRequest;
import com.gammatunes.backend.domain.model.VoiceDisconnectRequest;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.infrastructure.lavalink.LavaLinkAudioPlayer;
import com.gammatunes.backend.presentation.bot.audio.AudioPlayerSendHandler;
import com.gammatunes.backend.presentation.bot.exception.GuildNotFoundException;
import com.gammatunes.backend.presentation.bot.exception.VoiceChannelNotFoundException;
import com.gammatunes.backend.presentation.bot.player.service.PlayerPanelCoordinator;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Service for managing voice connections in Discord.
 * This service handles connecting the bot to a member's voice channel and disconnecting it when necessary.
 */
@Component
@RequiredArgsConstructor
public class DiscordVoiceGateway implements VoiceGateway {

    private static final Logger logger = LoggerFactory.getLogger(DiscordVoiceGateway.class);

    private final JDA jda;
    private final PlayerRegistryPort playerRegistry;
    private final PlayerPanelCoordinator panelCoordinator;

    /**
     * Connects the bot to the voice channel of the specified member.
     * If the bot is already connected, it does nothing.
     *
     * @param voiceConnectRequest The request containing the session and member information for connection.
     * @throws InsufficientPermissionException if the bot lacks permissions to connect or speak in the voice channel.
     * @throws VoiceChannelNotFoundException if the specified voice channel does not exist.
     */
    @Override
    public void connect(VoiceConnectRequest voiceConnectRequest) {

        Guild guild = getGuild(voiceConnectRequest.guildId());
        AudioManager audioManager = guild.getAudioManager();

        if (audioManager.isConnected()) {
            logger.info("Already connected to voice channel in guild {}", voiceConnectRequest.guildId());
            return;
        }

        AudioChannel channel = guild.getVoiceChannelById(voiceConnectRequest.channelId());
        if (channel == null) {
            throw new VoiceChannelNotFoundException(voiceConnectRequest.channelId());
        }

        if (!guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
            throw new InsufficientPermissionException(
                guild, Permission.VOICE_CONNECT,
                "Need CONNECT & SPEAK in " + channel.getName());
        }

        AudioPlayer domainPlayer = playerRegistry.getOrCreatePlayer(new Session(voiceConnectRequest.guildId()));
        audioManager.setSendingHandler(new AudioPlayerSendHandler(((LavaLinkAudioPlayer) domainPlayer).getLavaPlayer()));


        audioManager.openAudioConnection(channel);
        logger.info("Joined voice channel {} in guild {}", channel.getName(), guild.getId());
        TextChannel textChannel = Objects.requireNonNull(guild.getDefaultChannel())           // or pick a “music” channel
            .asTextChannel();
        panelCoordinator.createPanel(voiceConnectRequest.guildId(), textChannel);
    }


    /**
     * Disconnects the bot from the voice channel of the specified member.
     * If the bot is not connected, it does nothing.
     *
     * @param voiceDisconnectRequest The request containing the session and member information for disconnection.
     * @throws GuildNotFoundException if the guild with the specified ID does not exist.
     */
    @Override
    public void disconnect(VoiceDisconnectRequest voiceDisconnectRequest) {

        Guild guild = getGuild(voiceDisconnectRequest.guildId());
        AudioManager audioManager = guild.getAudioManager();

        if (audioManager.isConnected()) {
            audioManager.closeAudioConnection();
            logger.info("Disconnected from voice channel in guild {}", voiceDisconnectRequest.guildId());
        } else {
            logger.info("Not connected to any voice channel in guild {}", voiceDisconnectRequest.guildId());
        }
    }


    /**
     * Retrieves the guild by its ID.
     *
     * @param guildId The ID of the guild to retrieve.
     * @return The Guild object if found.
     * @throws GuildNotFoundException if the guild with the specified ID does not exist.
     */
    private Guild getGuild(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            logger.error("Guild not found for ID: {}", guildId);
            throw new GuildNotFoundException(guildId);
        }
        return guild;
    }
}
