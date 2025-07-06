package com.gammatunes.backend.audio.lavalink.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the configuration for Lavaplayer.
 * This class is responsible for creating and configuring the AudioPlayerManager bean
 * so that Spring can inject it into other components like our TrackResolvers.
 */
@Configuration
public class LavaPlayerConfig {

    /**
     * Creates and configures an AudioPlayerManager bean for the application.
     * This manager is the entry point for all audio operations with lavaplayer.
     *
     * @return A configured AudioPlayerManager instance.
     */
    @Bean
    public AudioPlayerManager audioPlayerManager() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(playerManager);

        return playerManager;
    }
}

