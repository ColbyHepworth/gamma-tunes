package com.gammatunes.backend.application.service;

import com.gammatunes.backend.application.port.in.AudioControlUseCase;
import com.gammatunes.backend.application.port.out.PlayerRegistryPort;
import com.gammatunes.backend.application.port.out.TrackResolverPort;
import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.model.Track;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.domain.exception.TrackLoadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AudioControlService implements AudioControlUseCase {

    private final PlayerRegistryPort playerRegistry;
    private final TrackResolverPort resolver;

    @Override
    public PlayerOutcome play(String sessionId, String query) throws TrackLoadException {
        Session session = new Session(sessionId);
        Track track = resolver.resolve(query);
        AudioPlayer player = playerRegistry.getOrCreatePlayer(session);
        return player.play(track);
    }

    @Override
    public PlayerOutcome playNow(String sessionId, String query) throws TrackLoadException {
        Session session = new Session(sessionId);
        Track track = resolver.resolve(query);
        AudioPlayer player = playerRegistry.getOrCreatePlayer(session);
        return player.playNow(track);
    }

    @Override
    public PlayerOutcome pause(String sessionId) {
        return player(sessionId).pause();
    }

    @Override
    public PlayerOutcome resume(String sessionId) {
        return player(sessionId).resume();
    }

    @Override
    public PlayerOutcome stop(String sessionId) {
        return player(sessionId).stop();
    }

    @Override
    public PlayerOutcome skip(String sessionId) {
        return player(sessionId).skip();
    }

    @Override
    public PlayerOutcome previous(String sessionId) {
        return player(sessionId).previous();
    }

    @Override
    public PlayerOutcome shuffle(String sessionId) {
        return player(sessionId).shuffle();
    }

    @Override
    public PlayerOutcome toggleRepeat(String sessionId) {
        return player(sessionId).toggleRepeat();
    }

    private AudioPlayer player(String sessionId) {
        return playerRegistry.getOrCreatePlayer(new Session(sessionId));
    }
}
