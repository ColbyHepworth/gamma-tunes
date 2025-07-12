package com.gammatunes.backend.bot.view;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.common.model.Session;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class PlayerMessageManager {

    private static final Logger log = LoggerFactory.getLogger(PlayerMessageManager.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private record PlayerMessageInfo(String channelId, long messageId) {}
    private final Map<String, PlayerMessageInfo> playerMessages = new ConcurrentHashMap<>();

    private final AudioService audioService;
    private final JDA jda;

    public PlayerMessageManager(AudioService audioService, JDA jda) {
        this.audioService = audioService;
        this.jda = jda;
    }

    @PostConstruct
    public void startUpdateLoop() {
        scheduler.scheduleAtFixedRate(this::updateAllActivePlayers, 0, 1, TimeUnit.SECONDS);
    }

    public void create(String guildId, TextChannel channel) {
        delete(guildId);
        AudioPlayer player = audioService.getOrCreatePlayer(new Session(guildId));

        channel.sendMessageEmbeds(PlayerEmbedBuilder.build(player))
            .addActionRow(PlayerEmbedBuilder.buildButtons(player))
            .queue(message -> {
                playerMessages.put(guildId, new PlayerMessageInfo(channel.getId(), message.getIdLong()));
                log.info("Created player message {} for guild {}", message.getId(), guildId);
            });
    }

    /**
     * Deletes the player message for the specified guild.
     * If the message exists, it will be removed from the map and deleted from Discord.
     *
     * @param guildId The ID of the guild whose player message should be deleted.
     */
    public void delete(String guildId) {
        if (playerMessages.containsKey(guildId)) {
            PlayerMessageInfo info = playerMessages.remove(guildId);
            TextChannel channel = jda.getTextChannelById(info.channelId());
            if (channel != null) {
                channel.deleteMessageById(info.messageId()).queue();
            }
            log.info("Deleting player message for guild {}", guildId);
        }
    }

    /**
     * @param player The player whose message needs updating.
     * @param guildId The ID of the guild.
     */
    public void update(AudioPlayer player, String guildId) {
        PlayerMessageInfo info = playerMessages.get(guildId);
        if (info == null) return;

        TextChannel channel = jda.getTextChannelById(info.channelId());
        if (channel != null) {
            channel.editMessageEmbedsById(info.messageId(), PlayerEmbedBuilder.build(player))
                .setActionRow(PlayerEmbedBuilder.buildButtons(player))
                .queue(
                    success -> log.trace("Updated player message for guild {}", guildId),
                    error -> {
                        log.warn("Failed to update player message for guild {}. It may have been deleted.", guildId);
                        playerMessages.remove(guildId);
                    });
        }
    }

    /**
     * Updates all active player messages in a separate thread to avoid blocking the main thread.
     * This method is scheduled to run periodically.
     */
    private void updateAllActivePlayers() {
        playerMessages.forEach((guildId, info) -> {
            AudioPlayer player = audioService.getOrCreatePlayer(new Session(guildId));
            update(player, guildId);
        });
    }
}
