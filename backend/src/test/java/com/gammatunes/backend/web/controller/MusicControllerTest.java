package com.gammatunes.backend.web.controller;

import com.gammatunes.backend.audio.api.AudioPlayer;
import com.gammatunes.backend.audio.api.AudioService;
import com.gammatunes.backend.audio.exception.TrackLoadException;
import com.gammatunes.common.dto.PlayRequest;
import com.gammatunes.common.model.PlayerState;
import com.gammatunes.common.model.Session;
import com.gammatunes.common.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * A Unit Test for the MusicController.
 *
 * @WebFluxTest loads only the web layer (controllers, DTOs, etc.), not the whole application.
 * This makes the test much faster than a full @SpringBootTest.
 *
 * @MockBean creates a mock of the AudioService, so we can test the controller in isolation.
 */
@WebFluxTest(MusicController.class)
class MusicControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AudioService audioService;

    @MockBean
    private AudioPlayer audioPlayer; // A mock player to be returned by the service

    @BeforeEach
    void setUp() {
        // Common setup for all tests that need a player:
        // Tell the audioService mock to return the audioPlayer mock for any session.
        when(audioService.getOrCreatePlayer(any(Session.class))).thenReturn(audioPlayer);
    }

    @Test
    void play_whenSuccessful_returnsOk() throws Exception {
        doNothing().when(audioService).play(any(Session.class), eq("test query"));

        PlayRequest request = new PlayRequest("test query");

        webTestClient.post().uri("/api/v1/music/12345/play")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("Track enqueued for session 12345");
    }

    @Test
    void play_whenTrackLoadFails_returnsBadRequest() throws Exception {
        doThrow(new TrackLoadException("Could not find track."))
            .when(audioService).play(any(Session.class), eq("bad query"));

        PlayRequest request = new PlayRequest("bad query");

        webTestClient.post().uri("/api/v1/music/12345/play")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.status").isEqualTo("Error loading track: Could not find track.");
    }

    @Test
    void pause_whenSuccessful_returnsOk() {
        webTestClient.post().uri("/api/v1/music/12345/pause")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("Player paused for session 12345");
    }

    @Test
    void resume_whenSuccessful_returnsOk() {
        webTestClient.post().uri("/api/v1/music/12345/resume")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("Player resumed for session 12345");
    }

    @Test
    void stop_whenSuccessful_returnsOk() {
        webTestClient.post().uri("/api/v1/music/12345/stop")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("Player stopped for session 12345");
    }

    @Test
    void skip_whenSuccessful_returnsOk() {
        webTestClient.post().uri("/api/v1/music/12345/skip")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("Player skipped for session 12345");
    }

    @Test
    void getStatus_whenPlayerHasState_returnsStatusResponse() {
        // Arrange
        Track nowPlaying = new Track("identifier1", "Now Playing Song", "Artist", Duration.ofMinutes(3), "url1", null);
        Track queuedTrack = new Track("identifier2", "Queued Song", "Artist 2", Duration.ofMinutes(4), "url2", null);

        when(audioPlayer.getState()).thenReturn(PlayerState.PLAYING);
        when(audioPlayer.getCurrentlyPlaying()).thenReturn(Optional.of(nowPlaying));
        when(audioPlayer.getQueue()).thenReturn(List.of(queuedTrack));

        // Act & Assert
        webTestClient.get().uri("/api/v1/music/12345/status")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.state").isEqualTo("PLAYING")
            .jsonPath("$.currentlyPlaying.title").isEqualTo("Now Playing Song")
            .jsonPath("$.queue[0].title").isEqualTo("Queued Song");
    }
}
