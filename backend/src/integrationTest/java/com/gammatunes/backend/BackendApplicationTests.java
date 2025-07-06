package com.gammatunes.backend;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * Integration tests for the GammaTunes backend application.
 * This class contains tests that verify the application context loads correctly.
 */
@Tag("integration")
@SpringBootTest
class BackendApplicationTests {

    /**
     * A simple "smoke test" that verifies the Spring application context can load successfully.
     */
    @Test
    void contextLoads() {
        // Test passes if the application context loads without throwing an exception.
    }
}
