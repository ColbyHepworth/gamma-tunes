package com.gammatunes.backend.application.service;

import com.gammatunes.backend.application.port.in.AudioControlUseCase;
import com.gammatunes.backend.application.port.out.PlayerRegistryPort;
import com.gammatunes.backend.application.port.out.TrackResolverPort;
import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.model.Track;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.domain.player.event.PlayerStateChanged;
import com.gammatunes.backend.infrastructure.source.exception.TrackLoadException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AudioControlService implements AudioControlUseCase {

    private final PlayerRegistryPort playerRegistry;
    private final TrackResolverPort resolver;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    public PlayerOutcome play(String sessionId, String query) throws TrackLoadException {
        Session session = new Session(sessionId);
        Track track = resolver.resolve(query);
        AudioPlayer player = playerRegistry.getOrCreatePlayer(session);
        PlayerOutcome outcome = player.play(track);
        PlayerState state = player.getState();
        eventPublisher.publishEvent(new PlayerStateChanged(
                sessionId,
                state,
                outcome
        ));
        return outcome;
    }

    @Override
    public PlayerOutcome playNow(String sessionId, String query) throws TrackLoadException {
        Session session = new Session(sessionId);
        Track track = resolver.resolve(query);
        AudioPlayer player = playerRegistry.getOrCreatePlayer(session);
        PlayerOutcome outcome = player.playNow(track);
        PlayerState state = player.getState();
        eventPublisher.publishEvent(new PlayerStateChanged(
                sessionId,
                state,
                outcome
        ));
        return outcome;
    }

    @Override
    public PlayerOutcome pause(String sessionId) {
        AudioPlayer player = playerRegistry.getOrCreatePlayer(new Session(sessionId));
        PlayerOutcome outcome = player.pause();
        PlayerState state = player.getState();
        eventPublisher.publishEvent(new PlayerStateChanged(
                sessionId,
                state,
                outcome
        ));
        return outcome;
    }

    @Override
    public PlayerOutcome resume(String sessionId) {
        AudioPlayer player = playerRegistry.getOrCreatePlayer(new Session(sessionId));
        PlayerOutcome outcome = player.resume();
        PlayerState state = player.getState();
        eventPublisher.publishEvent(new PlayerStateChanged(
                sessionId,
                state,
                outcome
        ));
        return outcome;
    }

    @Override
    public PlayerOutcome stop(String sessionId) {
        AudioPlayer player = playerRegistry.getOrCreatePlayer(new Session(sessionId));
        PlayerOutcome outcome = player.stop();
        PlayerState state = player.getState();
        eventPublisher.publishEvent(new PlayerStateChanged(
                sessionId,
                state,
                outcome
        ));
        return outcome;
    }

    @Override
    public PlayerOutcome skip(String sessionId) {
        AudioPlayer player = playerRegistry.getOrCreatePlayer(new Session(sessionId));
        PlayerOutcome outcome = player.skip();
        PlayerState state = player.getState();
        eventPublisher.publishEvent(new PlayerStateChanged(
                sessionId,
                state,
                outcome
        ));
        return outcome;
    }

    @Override
    public PlayerOutcome previous(String sessionId) {
        AudioPlayer player = playerRegistry.getOrCreatePlayer(new Session(sessionId));
        PlayerOutcome outcome = player.previous();
        PlayerState state = player.getState();
        eventPublisher.publishEvent(new PlayerStateChanged(
                sessionId,
                state,
                outcome
        ));
        return outcome;
    }
}
