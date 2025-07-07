package com.gammatunes.bot.service;

import com.gammatunes.common.dto.PlayerStatusResponse;
import com.gammatunes.common.model.PlayerState;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the BackendClient.
 * Uses a MockWebServer to simulate the backend API and test HTTP interactions.
 */
class BackendClientTest {

    private MockWebServer mockWebServer;
    private BackendClient backendClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        backendClient = new BackendClient(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void play_sendsCorrectRequest() throws Exception {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"status\":\"Track enqueued\"}")
            .addHeader("Content-Type", "application/json"));

        String sessionId = "123";
        String query = "test query";

        // Act
        backendClient.play(sessionId, query);

        // Assert
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/v1/music/123/play", recordedRequest.getPath());
        assertEquals("{\"query\":\"test query\"}", recordedRequest.getBody().readUtf8());
    }

    @Test
    void getStatus_whenSuccessful_deserializesResponseCorrectly() throws Exception {
        // Arrange
        String jsonResponse = """
                {
                    "state": "PLAYING",
                    "currentlyPlaying": {
                        "title": "Test Song"
                    },
                    "queue": []
                }
                """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));

        // Act
        PlayerStatusResponse status = backendClient.getStatus("456");

        // Assert
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/v1/music/456/status", recordedRequest.getPath());

        assertNotNull(status);
        assertEquals(PlayerState.PLAYING, status.state());
        assertEquals("Test Song", status.currentlyPlaying().title());
    }

    @Test
    void pause_whenRequestFails_throwsIOException() {
        // Arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(500)); // Simulate a server error

        // Act & Assert
        assertThrows(IOException.class, () -> {
            backendClient.pause("789");
        });
    }
}
