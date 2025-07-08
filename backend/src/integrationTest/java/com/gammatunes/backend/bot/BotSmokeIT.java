package com.gammatunes.backend.bot;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A smoke test for the JDA Bot container.
 * This test runs *after* `docker compose up` and verifies that the bot
 * can successfully communicate with the backend service over the Docker network.
 */
@Tag("integration")
class BotSmokeIT {

    @Test
    void bot_canConnectToBackend() {
        // Arrange: Get the backend URL from the environment variable set in docker-compose.yml
        String backendUrl = System.getenv("BACKEND_API_URL");
        assertNotNull(backendUrl, "BACKEND_API_URL environment variable must be set for this test.");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(backendUrl + "/actuator/health")
            .build();

        // Act & Assert: Use Awaitility to retry the connection until the backend is ready.
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                try (Response response = client.newCall(request).execute()) {
                    assertEquals(200, response.code(), "Backend health endpoint should return 200 OK");
                } catch (IOException e) {
                    // This will cause Awaitility to retry
                    throw new AssertionError("Failed to connect to backend: " + e.getMessage());
                }
            });
    }
}
