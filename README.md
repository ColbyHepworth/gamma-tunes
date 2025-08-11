# Gamma Tunes

A high-performance Discord music bot showcasing modern Java architecture with reactive programming, event-driven design, and microservice-ready component organization.

## Key Features

- **Seamless YouTube Integration** - Search and stream music with real-time queue management
- **Advanced Player Controls** - Queue jumping, shuffle, repeat modes, and track history
- **Real-time Discord UI** - Dynamic embeds with progress bars and interactive controls
- **Reactive Architecture** - Non blocking operations using Spring WebFlux and Project Reactor
- **Multi-Guild Support** - Concurrent player instances with isolated state management
- **Event-Driven Updates** - Real time UI synchronization through reactive event streams

## Technical Highlights

- **Spring Boot 3 + WebFlux**: Fully reactive, non-blocking architecture
- **JDA 5**: Advanced Discord API integration with comprehensive event handling
- **Lavalink 4**: Distributed audio processing with load balancing
- **Project Reactor**: Reactive streams for concurrent player state management
- **Component-Based Design**: Domain-driven package structure with clear boundaries
- **Docker Compose**: Multi  service orchestration with Lavalink audio nodes
- **Java 21**: Modern Java features with records, sealed interfaces, and pattern matching

## Architecture

Built with a sophisticated component-based architecture that separates concerns while maintaining cohesion:

- **Audio Components** - Core player logic, queue management, and event processing
- **Discord Components** - Interaction handling, UI rendering, and voice integration  
- **Lavalink Components** - Distributed audio node communication and load balancing
- **Reactive Services** - Business logic coordination with non-blocking operations

The modular design enables independent scaling of audio processing, Discord interactions, and future web interfaces.

## Testing

Tests are split into two main categories:

- **Unit tests** (`src/test/`) - Fast tests that mock dependencies
- **Integration tests** (`src/integrationTest/`) - Slower tests using Testcontainers for real dependencies

Run all tests with `./gradlew verifyAll`.


```bash
git clone https://github.com/colbyhepworth/gamma-tunes.git
cd gamma-tunes

# Create config file
cp .env.example .env
# Add your Discord bot token to .env

# Run everything
docker compose up --build
```

## License

MIT
