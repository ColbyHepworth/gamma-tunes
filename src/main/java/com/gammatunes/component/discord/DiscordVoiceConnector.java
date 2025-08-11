package com.gammatunes.component.discord;

import com.gammatunes.exception.discord.GuildNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

/**
 * Component for managing voice connections in Discord guilds.
 * Provides methods to connect and disconnect from voice channels.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordVoiceConnector {

    private final JDA jda;

    /**
     * Connects to a voice channel in the specified guild.
     *
     * @param guildId   The ID of the guild to connect to.
     * @param channelId The ID of the voice channel to connect to.
     * @return A Mono that completes when the connection is established.
     * @throws IllegalArgumentException if the voice channel is not found or permissions are insufficient.
     */
    public Mono<Void> connect(long guildId, long channelId) {
        return Mono.fromRunnable(() -> {
            Guild guild = getGuild(guildId);
            AudioChannel channel = guild.getVoiceChannelById(channelId);
            if (channel == null) throw new IllegalArgumentException("Voice channel " + channelId + " not found");

            if (!guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
                throw new InsufficientPermissionException(channel, Permission.VOICE_CONNECT, "Need CONNECT & SPEAK in " + channel.getName());
            }

            AudioManager am = guild.getAudioManager();
            if (am.isConnected() && Objects.equals(am.getConnectedChannel(), channel)) return;

            am.openAudioConnection(channel);
            log.info("Joined {} in {}", channel.getName(), guild.getName());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Disconnects from the voice channel in the specified guild.
     *
     * @param guildId The ID of the guild to disconnect from.
     * @return A Mono that completes when the disconnection is successful.
     */
    public Mono<Void> disconnect(long guildId) {
        return Mono.fromRunnable(() -> {
            Guild guild = getGuild(guildId);
            AudioManager am = guild.getAudioManager();

            if (am.isConnected()) {
                var ch = Objects.requireNonNull(am.getConnectedChannel());
                log.info("Disconnecting {} in {}", ch.getName(), guild.getName());
                am.closeAudioConnection();
            } else {
                log.debug("Already disconnected in guild {}", guild.getName());
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Retrieves the guild by its ID, throwing an exception if not found.
     *
     * @param guildId The ID of the guild to retrieve.
     * @return The Guild object if found.
     * @throws GuildNotFoundException if the guild is not found.
     */
    private Guild getGuild(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            log.error("Guild not found for ID: {}", guildId);
            throw new GuildNotFoundException(String.valueOf(guildId));
        }
        return guild;
    }
}
