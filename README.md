# 🎵 Gamma Tunes - A Modern, Scalable Discord Music Bot

[![CI/CD](https://github.com/colbyhepworth/gamma-tunes/actions/workflows/ci.yml/badge.svg)](https://github.com/colbyhepworth/gamma-tunes/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)

**Gamma Tunes** is a high-performance Discord music bot built on a modern, decoupled Java backend. This project serves as a comprehensive demonstration of enterprise-level software architecture principles, including SOLID design, a robust multi-layered testing strategy, and a fully automated CI/CD pipeline using Docker and GitHub Actions.

## ✨ Core Features

- 🎶 **YouTube Integration** - Play music from YouTube via search or direct URL
- 🎛️ **Full Player Controls** - Pause, resume, skip, stop, and previous track functionality
- 🌐 **REST API** - Ready for future web or mobile clients
- 🐳 **Fully Containerized** - Consistent, reliable deployments with Docker
- 🔧 **Enterprise Architecture** - Built with SOLID principles and modern design patterns

## 🛠️ Technology Stack

| Category | Technology |
|----------|------------|
| **Backend Framework** | Spring Boot 3 (Reactive with WebFlux) |
| **Discord API** | JDA (Java Discord API) |
| **Audio Engine** | Lavalink via the Lavaplayer Client |
| **Build & Tooling** | Gradle (Kotlin DSL), Git LFS, pre-commit |
| **Containerization** | Docker & Docker Compose |
| **Testing** | JUnit 5, Mockito, Testcontainers, RestAssured |
| **CI/CD** | GitHub Actions |

## 🏛️ Architectural Design

This project was designed from the ground up to be **modular**, **scalable**, and **maintainable**, following key software engineering principles.

### Core Principles

- **🎯 SOLID Design** - The entire application is built on SOLID principles. The Dependency Inversion Principle is central to the design, with high-level modules depending on abstractions (`AudioPlayer` interface) rather than concrete low-level implementations (`LavalinkPlayer`).

- **📦 Package-by-Feature** - The codebase is organized by business capability (`audio`, `bot`, `web`, `common`) rather than by technical layers (`services`, `models`). This makes the system easier to navigate and reason about.

- **🔧 Composition over Inheritance** - The command structure favors composition by using helper utilities (`CommandUtil`) over a rigid, deep inheritance hierarchy. This provides maximum flexibility for creating new and unique commands in the future.

### Design Patterns in Practice

- **🔌 Adapter Pattern** - The `LavalinkPlayer` class acts as an Adapter between our application's generic `AudioPlayer` interface and the specific API of the external lavaplayer library. This completely decouples our business logic from the audio provider, making it possible to swap out lavaplayer in the future with minimal code changes.

- **📋 Template Method Pattern** - The abstract `PlayerCommand` class uses the Template Method pattern to handle boilerplate logic for simple commands (like permission checks and deferring replies), allowing concrete commands like `PauseCommand` and `SkipCommand` to be incredibly concise and focused on their specific action.

- **🎯 Singleton Pattern** - Critical, stateless services like `AudioService` and `JdaManager` are managed as singletons by the Spring IoC container for efficiency and a centralized point of control.

## 🧪 Testing Strategy: The Testing Pyramid
The project employs a multi-layered testing strategy that follows the "Testing Pyramid" model to provide maximum confidence with optimal performance.

**Unit Tests:** A large suite of fast-running unit tests verifies individual classes in isolation. These tests use Mockito to mock dependencies and live in the src/test directory. They are run on every commit.

**Integration Tests:** More complex tests verify the interaction between different components. These tests use Testcontainers to spin up real dependencies (like a Lavalink server) in a controlled environment. They live in the src/integrationTest directory and are run on every pull request.

**End-to-End (E2E) Tests:** A small number of high-value E2E tests validate a full user journey through the entire application stack. These tests use Testcontainers to run the project's docker-compose.yml file and then make real API calls to the running backend service, providing the ultimate confidence that all services work together correctly.

### Test Layers

- **🔬 Unit Tests** - A large suite of fast-running unit tests verifies individual classes in isolation. These tests use Mockito to mock dependencies and live in the `src/test` directory. They are run on every commit.

- **🔗 Integration Tests** - More complex tests verify the interaction between different components. These tests use Testcontainers to spin up real dependencies (like a Lavalink server) in a controlled environment. They live in the `src/integrationTest` directory and are run on every pull request.

- **🎭 End-to-End (E2E) Tests** - A small number of high-value E2E tests validate a full user journey through the entire application stack. These tests use Testcontainers to run the project's `docker-compose.yml` file and then make real API calls to the running backend service, providing the ultimate confidence that all services work together correctly.

## 🚀 Local Development & Usage

### Prerequisites

- 🐳 **Docker Desktop** (v20+) – Compose V2 enabled
- 🔧 **Git & Git LFS**
- 🟢 **Node.js LTS** & **Python 3.10+** (for developer tooling)
- ☕ **Local JDK 21**

### First-Time Setup

```bash
# Clone the repo and navigate into it
git clone https://github.com/colbyhepworth/gamma-tunes.git
cd gamma-tunes

# Run the developer bootstrap script
./scripts/bootstrap

# Create a .env file from the example
cp .env.example .env

# Add your Discord Bot Token to the .env file
```

### Running the Application

```bash
# To run the entire stack (Lavalink + Backend/Bot)
docker compose up --build

# To run all unit and integration tests
./gradlew verifyAll
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
