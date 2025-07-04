package com.gammatunes.backend;

import io.restassured.RestAssured;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

class SmokeIT {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    void healthEndpointUp() {
        Awaitility.await()                       // ⏳ keep trying …
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(2))
            .untilAsserted(() ->
                get("/actuator/health")
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("UP"))
            );
    }
}
