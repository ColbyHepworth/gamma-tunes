
/* ───────────── 1. PLUGINS ───────────── */
plugins {
    java
    id("org.springframework.boot") version "3.5.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    idea // The IDEA plugin to help the IDE recognize source sets
}

/* ───────────── 2. SHARED CONFIGURATION FOR ALL SUB-PROJECTS ───────────── */
subprojects {
    apply(plugin = "java")
    // The 'idea' plugin is now applied in the specific subproject build file

    group = "com.gammatunes"
    version = "0.0.1-SNAPSHOT"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://m2.dv8tion.net/releases") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.lavalink.dev/releases") }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

/* ───────────── 3. ROOT-LEVEL HELPER TASKS ───────────── */
tasks.register("composeUp", Exec::class) {
    group = "verification"
    description = "Build (if needed) & start the full stack via Docker Compose"
    dependsOn(":backend:bootJar")
    commandLine("docker", "compose", "up", "--build", "-d")
}

tasks.register("composeDown", Exec::class) {
    group = "verification"
    description = "Stop stack and remove volumes"
    commandLine("docker", "compose", "down", "-v")
}

// Define the root verifyAll task that runs all tests
tasks.register("verifyAll") {
    group = "verification"
    description = "Runs all checks and tests for the project."
    dependsOn(":backend:clean", ":backend:check")
}
