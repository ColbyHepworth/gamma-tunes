package com.gammatunes.backend.presentation.bot.player.service;

import com.gammatunes.backend.application.port.out.PlayerRegistryPort;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.domain.player.event.PlayerStateChanged;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.presentation.bot.player.cache.MessageRef;
import com.gammatunes.backend.presentation.bot.player.cache.PlayerPanelCache;
import com.gammatunes.backend.presentation.bot.player.gateway.PlayerPanelGateway;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerPanelCoordinator {

    private static final Logger log = LoggerFactory.getLogger(PlayerPanelCoordinator.class);

    private final PlayerRegistryPort registry;
    private final PlayerPanelGateway gateway;
    private final PlayerPanelCache   cache;


    public void createPanel(String guildId, TextChannel channel) {
        cache.removeMessage(guildId);

        AudioPlayer p = registry.getOrCreatePlayer(new Session(guildId));
        MessageRef  ref = gateway.createPanel(guildId, channel, p, cache.getStatus(guildId));
        cache.putMessage(guildId, ref);
    }


    public void publishStatus(Session session, String status) {
        log.info("Publishing status for session {}", session.id());
        cache.setStatus(session.id(), status);
        refreshPanel(session);
    }

    public void deletePanel(String guildId) {
        log.info("Deleting player panel for guild {}", guildId);
        cache.getMessage(guildId).ifPresent(gateway::deletePanel);
        cache.removeMessage(guildId);
    }


    @EventListener
    public void onPlayerState(PlayerStateChanged ev) {
        refreshPanel(new Session(ev.sessionId()));
    }

    public void refreshPanel(Session session) {
        cache.getMessage(session.id()).ifPresent(ref -> {
            AudioPlayer p = registry.getOrCreatePlayer(session);
            gateway.updatePanel(ref, p, cache.getStatus(session.id()));
        });
    }


    @PreDestroy
    void cleanup() {
    log.info("Cleaning up player panels on shutdown");
        cache.guildIds().forEach(gid ->
            cache.getMessage(gid).ifPresent(gateway::deletePanel)
        );
    }
}
