package com.gammatunes.backend.web.controller;

import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.audio.exception.TrackLoadException;
import com.gammatunes.backend.common.ApiRoutes;
import com.gammatunes.backend.common.dto.PlayRequest;
import com.gammatunes.backend.common.dto.PlayerStatusResponse;
import com.gammatunes.backend.common.dto.StatusResponse;
import com.gammatunes.backend.common.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * A simple REST controller to expose our AudioService to the outside world.
 * This will be the entry point for our Discord bot or any other client.
 */
@RestController
public class MusicController {

    private final AudioService audioService;
    private static final Logger log = LoggerFactory.getLogger(MusicController.class);

    public MusicController(AudioService audioService) {
        this.audioService = audioService;
    }

    /**
     * Endpoint to play a track in a given session.
     *
     * @param sessionId The unique ID of the session (e.g., a Discord guild ID).
     * @param request   The request body containing the query.
     * @return A response entity with a status message.
     */
    @PostMapping(ApiRoutes.PLAY)
    public ResponseEntity<StatusResponse> play(
        @PathVariable String sessionId,
        @RequestBody PlayRequest request
    ) {
        try {
            log.info("Received request to play track for session: {}", sessionId);
            Session session = new Session(sessionId);
            audioService.play(session, request.query());
            return ResponseEntity.ok(new StatusResponse("Track enqueued for session " + sessionId));
        } catch (TrackLoadException e) {
            return ResponseEntity.badRequest().body(new StatusResponse("Error loading track: " + e.getMessage()));
        }
    }

    @PostMapping(ApiRoutes.PAUSE)
    public ResponseEntity<StatusResponse> pause(
        @PathVariable String sessionId
    ) {
        log.info("Received request to pause track for session: {}", sessionId);
        Session session = new Session(sessionId);
        var player = audioService.getOrCreatePlayer(session);
        player.pause();
        return ResponseEntity.ok(new StatusResponse("Player paused for session " + sessionId));
    }

    @PostMapping(ApiRoutes.RESUME)
    public ResponseEntity<StatusResponse> resume(
        @PathVariable String sessionId
    ) {
        log.info("Received request to resume track for session: {}", sessionId);
        Session session = new Session(sessionId);
        var player = audioService.getOrCreatePlayer(session);
        player.resume();
        return ResponseEntity.ok(new StatusResponse("Player resumed for session " + sessionId));
    }

    @PostMapping(ApiRoutes.STOP)
    public ResponseEntity<StatusResponse> stop(
        @PathVariable String sessionId
    ) {
        log.info("Received request to stop track for session: {}", sessionId);
        Session session = new Session(sessionId);
        var player = audioService.getOrCreatePlayer(session);
        player.stop();
        return ResponseEntity.ok(new StatusResponse("Player stopped for session " + sessionId));
    }

    @PostMapping(ApiRoutes.SKIP)
    public ResponseEntity<StatusResponse> skip(
        @PathVariable String sessionId
    ) {
        log.info("Received request to skip track for session: {}", sessionId);
        Session session = new Session(sessionId);
        var player = audioService.getOrCreatePlayer(session);
        player.skip();
        return ResponseEntity.ok(new StatusResponse("Player skipped for session " + sessionId));
    }

    @GetMapping(ApiRoutes.STATUS)
    public ResponseEntity<PlayerStatusResponse> getStatus(@PathVariable String sessionId) {
        log.info("Received status request for session {}", sessionId);
        Session session = new Session(sessionId);
        var player = audioService.getOrCreatePlayer(session);

        PlayerStatusResponse response = new PlayerStatusResponse(
            player.getState(),
            player.getCurrentlyPlaying().orElse(null),
            player.getQueue()
        );
        return ResponseEntity.ok(response);
    }
}
