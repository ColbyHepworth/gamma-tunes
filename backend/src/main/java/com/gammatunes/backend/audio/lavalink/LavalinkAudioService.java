package com.gammatunes.backend.audio.lavalink;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.audio.exception.TrackLoadException;
import com.gammatunes.backend.audio.source.AudioSourceManager;
import com.gammatunes.backend.common.model.Session;
import com.gammatunes.backend.common.model.Track;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The lavaplayer implementation of the AudioService.
 * This class uses the AudioPlayerManager from the lavaplayer library to create players.
 */
@Service
public class LavalinkAudioService implements AudioService {

    private static final Logger log = LoggerFactory.getLogger(LavalinkAudioService.class);
    private final AudioSourceManager audioSourceManager;
    private final AudioPlayerManager playerManager;
    private final Map<String, AudioPlayer> players = new ConcurrentHashMap<>();

    public LavalinkAudioService(AudioSourceManager audioSourceManager, AudioPlayerManager playerManager) {
        this.audioSourceManager = audioSourceManager;
        this.playerManager = playerManager;
    }

    @Override
    public AudioPlayer getOrCreatePlayer(Session session) {
        return players.computeIfAbsent(session.id(), id -> {
            log.info("Creating new lavaplayer adapter for session: {}", id);
            com.sedmelluq.discord.lavaplayer.player.AudioPlayer lavaplayer = playerManager.createPlayer();
            // Pass the playerManager to our adapter so it can load tracks
            return new LavalinkPlayer(session, lavaplayer, playerManager);
        });
    }

    @Override
    public void play(Session session, String query) throws TrackLoadException {
        AudioPlayer player = getOrCreatePlayer(session);
        Track track = audioSourceManager.resolveTrack(query);
        player.enqueue(track);
    }
}
