# gamma-tunes

> Reactive Spring Boot ⚡ Redis 🎵 Testcontainers ♦️ Docker Compose
>
> **One command to green‑light the whole stack:** `./gradlew verifyAll`

---

## 🖥️ Prerequisites

* **Docker Desktop (v20+)** – Compose V2 enabled.
* **Git**
* *(Optional)* A local JDK 21.  If not installed, the Gradle wrapper will auto‑download one via the toolchain feature.

---

## 🚀 First‑time checkout & bootstrap

```bash
# Clone the repo
$ git clone https://github.com/colbyhepworth/gamma-tunes.git
$ cd gamma-tunes

# Install Git hooks, pre‑commit linters, etc.
$ ./scripts/bootstrap

# Prove everything works (build, unit + integration tests)
$ ./gradlew verifyAll
```

The first run will download Gradle 8.13, the Spring & Testcontainers BOMs, and pull Docker images.  Subsequent runs are **fast**.

---

## 🔄 Every‑day workflow

```bash
# Fast feedback while coding
$ ./gradlew test               # unit tests only

# Need to poke the running stack
$ ./gradlew composeUp          # builds image & starts Redis + app
… hit http://localhost:8080 …
$ ./gradlew composeDown        # stop & clean volumes

# Before pushing / in CI
$ ./gradlew verifyAll          # or just ./gradlew check
```

`verifyAll` takes care of:

1. cleaning previous outputs
2. running all unit tests
3. **building & standing up** the Docker Compose stack
4. running integration/smoke tests
5. always tearing the stack down (even if tests fail)
---

## 🛠️ Gradle task cheat‑sheet

| Task                        | What it does                                     |
| --------------------------- | ------------------------------------------------ |
| `test`                      | Unit tests only                                  |
| `integrationTest`           | Starts Compose stack → runs containerised tests  |
| `composeUp` / `composeDown` | Manually manage the stack                        |
| `verifyAll`                 | `clean` → `test` → `integrationTest`             |
| `bootJar`                   | Builds the runnable fat‑jar (`build/libs/*.jar`) |


