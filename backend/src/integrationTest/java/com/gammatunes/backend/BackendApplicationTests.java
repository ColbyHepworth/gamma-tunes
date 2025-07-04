package com.gammatunes.backend;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.redis.testcontainers.RedisContainer;      // ← change package
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

@Testcontainers          // ① tell JUnit to manage containers
@Tag("integration")      // optional – see previous advice about splitting ITs
@SpringBootTest
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class BackendApplicationTests {

    /**
     * This defines a Redis container that will be started before our tests run.
     * Using the specific RedisContainer class is a best practice.
     */
    @Container
    static final RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    /**
     * This method dynamically sets the Spring properties for Redis to point to the
     * randomly assigned host and port of our test container.
     * @param registry The registry for adding dynamic properties.
     */
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @Autowired
    private ReactiveRedisConnectionFactory connectionFactory;

    /**
     * A simple "smoke test" that verifies the Spring application context can load successfully.
     */
    @Test
    void contextLoads() {
        // Test passes if the application context loads without throwing an exception.
    }

    /**
     * This is an integration test that verifies the application can successfully
     * connect to the Redis container started by Testcontainers.
     */
    @Test
    void redisConnectionIsSuccessful() {
        ReactiveRedisConnection connection = connectionFactory.getReactiveConnection();

        StepVerifier.create(connection.ping())
            .expectNext("PONG")
            .verifyComplete();
    }
}
