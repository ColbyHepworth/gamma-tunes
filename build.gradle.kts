// build.gradle.kts
/* ───────────── 1. PLUGINS ───────────── */
plugins {
    java
    id("org.springframework.boot") version "3.3.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

/* ───────────── 2. SHARED CONFIGURATION FOR ALL SUB-PROJECTS ───────────── */
subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

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

/* ───────────── 3. BACKEND-SPECIFIC CONFIGURATION ───────────── */
project(":backend") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    val integrationTest by tasks.registering(Test::class) {
        description = "Runs component integration tests using Testcontainers"
        group = "verification"
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        shouldRunAfter(tasks.named("test"))
        filter {
            excludeTestsMatching("*E2E*")
            excludeTestsMatching("*SmokeIT")
        }
    }

    val e2eTest by tasks.registering(Test::class) {
        description = "Runs slow E2E tests against the full stack via Testcontainers"
        group = "verification"
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        shouldRunAfter(tasks.named("integrationTest"))
        filter {
            includeTestsMatching("*E2E*")
        }
    }
}

/* ───────────── 4. ROOT-LEVEL HELPER TASKS ───────────── */
tasks.register("composeUp", Exec::class) {
    group = "verification"
    description = "Build (if needed) & start the full stack via Docker Compose"
    // Depends on the bootJar task from the 'backend' subproject
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
    dependsOn("clean", "check")
}
