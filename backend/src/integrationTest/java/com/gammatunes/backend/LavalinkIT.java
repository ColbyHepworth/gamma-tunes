package com.gammatunes.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.time.Duration;
import java.util.UUID;

@Testcontainers
@SpringBootTest
@Tag("integration")
class LavalinkIT {

    /**
     * Generate a per-test random password – never committed
     */
    private static final String PASSWORD = UUID.randomUUID().toString();

    @Container
    static final GenericContainer<?> lavalink =
        new GenericContainer<>("ghcr.io/lavalink-devs/lavalink:4")
            .withExposedPorts(2333)
            .withEnv("LAVALINK_SERVER_PASSWORD", PASSWORD)
            .waitingFor(
                Wait.forLogMessage(".*Lavalink is ready.*", 1)
                    .withStartupTimeout(Duration.ofSeconds(60))
            );

    /**
     * Dynamically sets the application properties to connect to the Testcontainer instance.
     * @param r The dynamic property registry.
     */
    @DynamicPropertySource
    static void wireProps(DynamicPropertyRegistry r) {
        r.add("lavalink.host", lavalink::getHost);
        r.add("lavalink.port", () -> lavalink.getMappedPort(2333).toString());
        r.add("lavalink.password", () -> PASSWORD);
    }

    /**
     * This test simply verifies that the application context can load successfully
     * with the dynamic Lavalink properties. A more advanced test could try to
     * connect to the Lavalink server.
     */
    @Test
    void contextLoadsWithLavalinkContainer() {
        // Test passes if the context loads, proving the properties were set correctly.
    }
}
