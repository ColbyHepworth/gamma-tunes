# Gamma Tunes

A Discord music bot built with Java and Spring Boot. Plays music from YouTube and provides basic player controls.

## Features

- Play music from YouTube (search or direct URLs)
- Basic controls: pause, resume, skip, stop, previous
- REST API for potential web frontend
- Docker deployment

## Tech Stack

- **Backend**: Spring Boot 3 with WebFlux
- **Discord**: JDA
- **Audio**: Lavalink + Lavaplayer
- **Build**: Gradle with Kotlin DSL
- **Deployment**: Docker & Docker Compose
- **Testing**: JUnit 5, Mockito, Testcontainers

## Architecture Notes

The bot is structured around a few key interfaces to keep things modular:

- `AudioPlayer` interface abstracts away the audio backend (currently Lavalink)
- Commands extend `PlayerCommand` for common functionality
- Package structure follows domain boundaries rather than technical layers

This makes it easier to swap out components or add new features without touching unrelated code.

## Testing

Tests are split into two main categories:

- **Unit tests** (`src/test/`) - Fast tests that mock dependencies
- **Integration tests** (`src/integrationTest/`) - Slower tests using Testcontainers for real dependencies

Run all tests with `./gradlew verifyAll`.

## Development Setup

You'll need:
- Docker Desktop
- Git with Git LFS
- JDK 21

```bash
git clone https://github.com/colbyhepworth/gamma-tunes.git
cd gamma-tunes

# Set up dev environment
./scripts/bootstrap

# Create config file
cp .env.example .env
# Add your Discord bot token to .env

# Run everything
docker compose up --build
```

## License

MIT
