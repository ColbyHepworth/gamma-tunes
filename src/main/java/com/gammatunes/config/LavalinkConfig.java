package com.gammatunes.config;

import dev.arbjerg.lavalink.client.*;
import dev.arbjerg.lavalink.client.event.ReadyEvent;
import dev.arbjerg.lavalink.client.event.StatsEvent;
import dev.arbjerg.lavalink.client.loadbalancing.IRegionFilter;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;

/**
 * Configuration for Lavalink client and properties.
 * <p>
 * This class combines both the properties configuration and the client setup
 * in a single configuration class for better organization.
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "lavalink")
@Data
public class LavalinkConfig {
    
    private long userId;
    private List<Node> nodes;

    /**
     * Creates and configures a Lavalink client with the specified nodes.
     *
     * @return a configured Lavalink client
     */
    @Bean
    public LavalinkClient lavalinkClient() {
        log.info("Creating Lavalink client for user ID {}", userId);
        LavalinkClient client = new LavalinkClient(userId);
        client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());

        nodes.forEach(n ->
            client.addNode(new NodeOptions.Builder()
                .setName(n.getName())
                .setServerUri(n.getUri())
                .setPassword(n.getPassword())
                .setRegionFilter(n.getRegion())
                .setHttpTimeout(n.getHttpTimeout())
                .build())
        );

        client.on(ReadyEvent.class).subscribe(readyEvent ->
            log.info("Node '{}' ready (session {})", readyEvent.getNode().getName(), readyEvent.getSessionId()));

        client.on(StatsEvent.class).subscribe(statsEvent ->
            log.info("Stats {}  players: {}/{}  links: {}",
                statsEvent.getNode().getName(), statsEvent.getPlayingPlayers(),
                statsEvent.getPlayers(), client.getLinks().size()));

        return client;
    }

    @Data
    public static class Node {
        private String name;
        private URI uri;
        private String password;
        private IRegionFilter region = RegionGroup.US;
        private long httpTimeout = 5000;
    }
}
