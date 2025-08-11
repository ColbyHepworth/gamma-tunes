package com.gammatunes.component.discord.ui.panel;

import com.gammatunes.component.audio.core.PlayerStateStore;
import com.gammatunes.component.audio.events.PlayerUIState;
import com.gammatunes.component.audio.events.PlayerPosition;
import com.gammatunes.model.dto.MessageRef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Manages player panels in Discord guilds.
 * Handles creation, updating, deletion, and recreation of player panels.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerPanelManager {

    private final JDA jda;
    private final PlayerPanelFactory panelFactory;
    private final PlayerStateStore stateStore;

    /**
     * Creates a new player panel in the specified channel with the given status.
     *
     * @param guildId The ID of the guild where the panel will be created.
     * @param channel The channel where the panel will be sent.
     * @param status  The initial status to display on the panel.
     * @return A Mono that emits the reference to the created message.
     */
    public Mono<MessageRef> createPanel(long guildId, TextChannel channel, String status) {
        log.info("createPanel guild={} channel={} type={}", guildId, channel.getId(), channel.getType());

        PlayerPanel panel = buildPlayerPanel(guildId, status);

        return Mono.fromFuture(
                channel.sendMessageEmbeds(panel.embed())
                    .setComponents(panel.components())
                    .submit()
            )
            .map(Message::getIdLong)
            .doOnNext(id -> log.info("Created panel {} in channel {}", id, channel.getId()))
            .map(id -> new MessageRef(guildId, channel.getIdLong(), id));
    }

    /**
     * Updates the existing player panel message with the new status.
     * If the message is missing, it will log a warning and ignore the error.
     *
     * @param ref    The reference to the message to update.
     * @param status The new status to set on the panel.
     * @return A Mono that completes when the update is done.
     */
    public Mono<Void> updatePanel(MessageRef ref, String status) {
        log.debug("updatePanel ref={} guild={} channel={}", ref, ref.guildId(), ref.channelId());

        return Mono.fromCallable(() -> buildPlayerPanel(ref.guildId(), status))
            .flatMap(panel -> resolveMessageChannel(ref)
                .flatMap(ch -> {
                    log.debug("Editing message {} in channel {}", ref.messageId(), ch.getId());
                    return Mono.fromFuture(
                        ch.editMessageEmbedsById(ref.messageId(), panel.embed())
                            .setComponents(panel.components())
                            .submit()
                    );
                })
            )
            .doOnError(err -> log.warn("Failed to update panel {} (will bubble to coordinator): {}", ref, err.toString()))
            .then();
    }

    /**
     * Deletes the panel message in the specified channel.
     * If the message is already missing, it will log a warning and ignore the error.
     *
     * @param ref The reference to the message to delete.
     * @return A Mono that completes when the deletion is done.
     */
    public Mono<Void> deletePanel(MessageRef ref) {
        log.debug("deletePanel ref={} guild={} channel={}", ref, ref.guildId(), ref.channelId());
        return resolveMessageChannel(ref)
            .flatMap(ch -> {
                log.debug("Deleting message {} in channel {}", ref.messageId(), ch.getId());
                return Mono.fromFuture(ch.deleteMessageById(ref.messageId()).submit());
            })
            .doOnSuccess(v -> log.info("Deleted panel {} in channel {}", ref.messageId(), ref.channelId()))
            .onErrorResume(ex -> {
                if (ex instanceof ErrorResponseException err &&
                    (err.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE ||
                        err.getErrorResponse() == ErrorResponse.UNKNOWN_CHANNEL)) {
                    log.warn("Panel already missing for {} ({}). Ignoring.", ref, err.getErrorResponse());
                    return Mono.empty();
                }
                log.warn("Failed to delete panel {}: {}", ref, ex.toString());
                return Mono.empty(); // swallow delete failures
            })
            .then();
    }

    /**
     * Recreates the player panel message in the specified channel with the given status.
     * This is useful when the original message is missing or needs to be refreshed.
     *
     * @param oldRef The reference to the old message to recreate.
     * @param status The new status to set on the panel.
     * @return A Mono that emits the reference to the recreated message.
     */
    public Mono<MessageRef> recreatePanel(MessageRef oldRef, String status) {
        log.info("recreatePanel oldRef={} guild={} channel={}", oldRef, oldRef.guildId(), oldRef.channelId());

        PlayerPanel panel = buildPlayerPanel(oldRef.guildId(), status);

        return resolveMessageChannel(oldRef)
            .flatMap(ch ->
                Mono.fromFuture(
                        ch.sendMessageEmbeds(panel.embed())
                            .setComponents(panel.components())
                            .submit()
                    )
                    .map(Message::getIdLong)
                    .doOnNext(id -> log.info("Recreated panel {} in channel {}", id, ch.getId()))
                    .map(id -> new MessageRef(oldRef.guildId(), ch.getIdLong(), id))
            );
    }

    /**
     * Builds a player panel for the specified guild with the given status.
     * This method retrieves the UI state and position from the state store.
     *
     * @param guildId The ID of the guild for which to build the panel.
     * @param status  The status text to display on the panel.
     * @return A PlayerPanel containing the embed and components for the player view.
     */
    private PlayerPanel buildPlayerPanel(long guildId, String status) {
        PlayerUIState uiState = stateStore.getUI(guildId);
        PlayerPosition position = stateStore.getPosition(guildId);

        if (uiState == null) {
            throw new IllegalStateException("No UI state for guild: " + guildId);
        }

        return panelFactory.buildPanel(uiState, position, status);
    }

    /**
     * Resolve a message-capable guild channel using only cache-based APIs available in your JDA.
     * Order: global GuildMessageChannel -> global TextChannel -> guild cache (generic -> check instanceof).
     * If not found or not message-capable, fail fast with a descriptive error.
     */
    private Mono<GuildMessageChannel> resolveMessageChannel(MessageRef ref) {
        final long gid = ref.guildId();
        final long cid = ref.channelId();


        GuildMessageChannel gmc = jda.getChannelById(GuildMessageChannel.class, cid);
        if (gmc != null) {
            log.debug("[resolve] global GuildMessageChannel cache HIT id={} type={}", gmc.getId(), gmc.getType());
            return Mono.just(gmc);
        }
        log.debug("[resolve] global GuildMessageChannel cache MISS id={}", cid);

        TextChannel textGlobal = jda.getTextChannelById(cid);
        if (textGlobal != null) {
            log.debug("[resolve] global TextChannel cache HIT id={} type={}", textGlobal.getId(), textGlobal.getType());
            return Mono.just(textGlobal);
        }
        log.debug("[resolve] global TextChannel cache MISS id={}", cid);

        Guild guild = jda.getGuildById(gid);
        if (guild == null) {
            log.warn("[resolve] guild cache MISS gid={} while resolving cid={}", gid, cid);
            return Mono.error(new IllegalStateException("Guild missing: " + gid));
        }

        GuildChannel generic = guild.getGuildChannelById(cid);
        if (generic instanceof GuildMessageChannel gmGuild) {
            log.debug("[resolve] guild channel cache HIT gid={} cid={} type={}", gid, gmGuild.getId(), gmGuild.getType());
            return Mono.just(gmGuild);
        }

        if (generic != null) {
            log.warn("[resolve] channel gid={} cid={} is type={} (not message-capable)", gid, generic.getId(), generic.getType());
        } else {
            log.warn("[resolve] channel gid={} cid={} not found in guild cache", gid, cid);
        }

        return Mono.error(new IllegalStateException("Channel missing: " + cid));
    }
}
