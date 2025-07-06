gamma-tunesReactive Spring Boot 🎵 Testcontainers ♦️ Docker ComposeOne command to verify the application code: ./gradlew verifyAll🖥️ PrerequisitesDocker Desktop (v20+) – Compose V2 enabled.Git & Git LFSNode.js LTS & Python 3.10+ (for developer tooling)(Optional) A local JDK 21. If not installed, the Gradle wrapper will auto-download one via the toolchain feature.🚀 First-time checkout & bootstrap# Clone the repo
$ git clone https://github.com/colbyhepworth/gamma-tunes.git
$ cd gamma-tunes

# Install Git hooks, pre-commit linters, etc.
$ ./scripts/bootstrap

# Prove everything works (build, unit + Testcontainers integration tests)
$ ./gradlew verifyAll
The first run will download Gradle 8.13, the Spring & Testcontainers BOMs, and pull Docker images. Subsequent runs are fast.🔄 Every-day workflow# Fast feedback while coding (unit tests only)
$ ./gradlew test

# Run all self-contained tests (unit + integration) before pushing
$ ./gradlew verifyAll          # or just ./gradlew check
```verifyAll` is the primary command for local development and CI. It runs:

1.  `clean`: cleaning previous outputs
2.  `test`: running all unit tests
3.  `integrationTest`: running all self-contained integration tests using Testcontainers

---

## 💻 Local Development

There are two main ways to run the application locally for development.

### Option 1: From Your IDE (Fastest)

For rapid development and debugging, you can run the application directly from your IDE (like IntelliJ IDEA).

1.  Open the project in your IDE.
2.  Navigate to `backend/src/main/java/com/gammatunes/backend/BackendApplication.java`.
3.  Run the `main` method.

The Spring Boot application will start up, but it will not be connected to a Lavalink server. This is useful for working on API endpoints or other non-audio features.

### Option 2: Full Stack with Docker Compose

To run the entire stack, including the Lavalink server, just as it would run in production:

```bash
# Build the latest image and start all services
$ docker compose up --build

# To stop the stack and remove volumes
$ docker compose down -v
You can now hit the API at http://localhost:8080 and the application will be fully functional.🛠️ Gradle task cheat-sheetTaskWhat it doesWhen to usetestRuns fast unit tests only.Constantly during development.integrationTestRuns self-contained tests using Testcontainers.As a high-confidence check.smokeTestStarts Compose stack → runs smoke tests → stops stack.To validate the full deployed environment.verifyAllclean → test → integrationTestBefore pushing and in CI pull requests.bootJarBuilds the runnable fat-jar (build/libs/*.jar).For manual deployments.
