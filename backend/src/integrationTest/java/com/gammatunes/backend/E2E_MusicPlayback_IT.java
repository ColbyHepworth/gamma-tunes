// File: backend/src/integrationTest/java/com/gammatunes/backend/E2E_MusicPlayback_IT.java
package com.gammatunes.backend;

import com.gammatunes.backend.common.model.PlayerState;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * A full End-to-End (E2E) integration test for the music playback flow.
 * This test uses Testcontainers to start the entire application stack from docker-compose.yml
 * and verifies the core user journey of playing a song.
 */
@Testcontainers
@Tag("e2e")
class E2E_MusicPlayback_IT {

    // This tells Testcontainers to find and run our docker-compose.yml file
    @Container
    private static final DockerComposeContainer<?> environment =
        new DockerComposeContainer<>(new File("docker-compose.yml"))
            // Wait for the backend service to be healthy before starting the test
            .withExposedService("backend", 8080, Wait.forHttp("/actuator/health").forStatusCode(200))
            // Wait for the lavalink service to be healthy
            .withExposedService("lavalink", 2333, Wait.forHttp("/v4/info").forStatusCode(200));

    /**
     * Sets up RestAssured to point to the dynamically assigned host and port of our backend service.
     */
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://" + environment.getServiceHost("backend", 8080);
        RestAssured.port = environment.getServicePort("backend", 8080);
    }

    @Test
    void playCommand_whenTrackIsQueued_updatesPlayerStatusCorrectly() {
        String sessionId = "e2e-test-guild";
        String query = "Never Gonna Give You Up";
        String requestBody = String.format("{\"query\": \"%s\"}", query);

        // 1. ACT: Send a request to the /play endpoint
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/v1/music/{sessionId}/play", sessionId)
            .then()
            .statusCode(200)
            .body("status", equalTo("Track enqueued for session " + sessionId));

        // 2. ASSERT: Poll the /status endpoint until the state is PLAYING
        Awaitility.await()
            .atMost(Duration.ofSeconds(20)) // Allow up to 20 seconds for the track to load
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() ->
                RestAssured.get("/api/v1/music/{sessionId}/status", sessionId)
                    .then()
                    .statusCode(200)
                    .body("state", equalTo(PlayerState.PLAYING.toString()))
                    .body("currentlyPlaying", notNullValue())
                    .body("currentlyPlaying.title", equalTo("Rick Astley - Never Gonna Give You Up (Official Music Video)"))
            );
    }
}
