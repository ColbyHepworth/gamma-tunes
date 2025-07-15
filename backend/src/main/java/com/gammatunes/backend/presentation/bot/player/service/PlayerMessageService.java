package com.gammatunes.backend.presentation.bot.player.service;

import com.gammatunes.backend.application.port.out.PlayerRegistryPort;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.domain.player.event.PlayerStateChanged;
import com.gammatunes.backend.presentation.bot.player.view.factory.PlayerEmbedFactory;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PlayerMessageService {

    private static final Logger log = LoggerFactory.getLogger(PlayerMessageService.class);

    private final PlayerRegistryPort registry;
    private final JDA                jda;
    private final PlayerEmbedFactory embedFactory;

    private record MessageRef(String guildId, String channelId, long messageId) {}

    private final Map<String, MessageRef> messages   = new ConcurrentHashMap<>();
    private final Map<String, String>     lastStatus = new ConcurrentHashMap<>();
    private final Map<String, Integer> lastBarIndex = new ConcurrentHashMap<>();
    private final Map<String, Long>    lastEditTime = new ConcurrentHashMap<>();

    public void create(String guildId, TextChannel channel) {
        delete(guildId);
        Session     session = new Session(guildId);
        AudioPlayer player  = registry.getOrCreatePlayer(session);
        channel.sendMessageEmbeds(embedFactory.buildEmbed(player, lastStatus.get(guildId)))
            .addActionRow(embedFactory.buildButtons(player))
            .queue(msg -> {
                messages.put(guildId, new MessageRef(guildId, channel.getId(), msg.getIdLong()));
                log.info("Created player embed {} in channel {}", msg.getId(), channel.getId());
            });
    }

    public void delete(String guildId) {
        MessageRef ref = messages.remove(guildId);
        if (ref == null) return;
        lastBarIndex.remove(guildId);
        lastEditTime.remove(guildId);
        lastStatus.remove(guildId);
        TextChannel ch = jda.getTextChannelById(ref.channelId());
        if (ch != null) {
            ch.deleteMessageById(ref.messageId()).queue();
        }
    }

    public void publishStatus(Session session, String status) {
        lastStatus.put(session.id(), status);
        refresh(session);
    }

    public void refresh(Session session) {
        MessageRef ref = messages.get(session.id());
        if (ref != null) {
            updateEmbed(session, ref);
        }
    }

    @EventListener
    public void onPlayerStateChanged(PlayerStateChanged ev) {
        refresh(new Session(ev.getGuildId()));
    }

    @Scheduled(fixedRate = 1000)
    void scheduledRefresh() {
        long now = System.currentTimeMillis();
        messages.values().forEach(ref -> {
            Session     session    = new Session(ref.guildId());
            AudioPlayer player = registry.getOrCreatePlayer(session);
            long        pos    = player.getTrackPosition();
            long        dur    = player.getCurrentlyPlaying()
                .map(t -> t.duration().toMillis())
                .orElse(0L);

            if (dur <= 0) return;

            int barLen   = 20;
            int indexNow = (int) ((barLen * pos) / dur);
            int  indexPrev = lastBarIndex.getOrDefault(session.id(), -1);
            long tPrev     = lastEditTime.getOrDefault(session.id(), 0L);
            long minInterval = chooseInterval(dur);

            if (indexNow != indexPrev && now - tPrev >= minInterval) {
                lastBarIndex.put(session.id(), indexNow);
                lastEditTime.put(session.id(), now);
                refresh(session);
            }
        });
    }

    private long chooseInterval(long durationMs) {
        if (durationMs <= 5 * 60_000)   return 5_000;
        if (durationMs <= 15 * 60_000)  return 8_000;
        return 12_000;
    }

    private void updateEmbed(Session session, MessageRef ref) {
        TextChannel ch = jda.getTextChannelById(ref.channelId());
        if (ch == null) {
            messages.remove(ref.guildId());
            return;
        }

        AudioPlayer player = registry.getOrCreatePlayer(session);
        // MODIFICATION: Reverted to .get() for "sticky" status messages.
        // The status is now fetched but not removed from the map.
        String currentStatus = lastStatus.get(session.id());

        ch.editMessageEmbedsById(
                ref.messageId(),
                embedFactory.buildEmbed(player, currentStatus))
            .setActionRow(embedFactory.buildButtons(player))
            .queue(success -> {
                log.trace("Successfully updated embed for guild {}", session.id());
            }, err -> {
                log.warn("Failed to update embed {}; removing from cache.", ref.messageId(), err);
                messages.remove(ref.guildId());
            });
    }

    /**
     * A new method for manually and reliably clearing a player panel.
     * Call this from a bot command (e.g., !cleanup) before shutting down the bot.
     */
    public void manuallyClearPlayer(String guildId) {
        log.info("Attempting to manually clear player for guild {}", guildId);
        // Use the 'delete' method which already contains the necessary logic.
        delete(guildId);
    }

    public void purgeBotMessages(TextChannel channel) {
        // This is not used for player cleanup but kept for other potential uses.
        long selfId = jda.getSelfUser().getIdLong();
        messages.remove(channel.getGuild().getId());
        channel.getIterableHistory()
            .cache(false)
            .forEachAsync(msg -> {
                if (msg.getAuthor().getIdLong() == selfId) {
                    channel.deleteMessageById(msg.getId()).queue();
                }
                return true;
            });
    }

    @PreDestroy
    void shutdown() {
        // This will attempt to run on graceful shutdown (Ctrl+C).
        // If it fails, it's because the app is killed too quickly.
        // Use the manual !cleanup command for 100% reliability.
        log.info("Shutting down PlayerMessageService – attempting to clear {} embeds...", messages.size());
        messages.keySet().forEach(this::manuallyClearPlayer);
        log.info("Cleanup attempt finished.");
    }
}
