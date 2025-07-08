package com.gammatunes.backend.audio.lavalink.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the configuration for Lavaplayer.
 * This class is responsible for creating and configuring the AudioPlayerManager bean
 * so that Spring can inject it into other components within the lavalink module.
 * By placing this inside the lavalink package, we keep the audio module self-contained.
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

        // Manually register the YouTube source manager from the new plugin
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());

        // Register the other remote sources like SoundCloud, Bandcamp, etc.
        AudioSourceManagers.registerRemoteSources(playerManager);

        return playerManager;
    }
}
