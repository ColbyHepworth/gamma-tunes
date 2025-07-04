/* ───────────── 1. PLUGINS ───────────── */
plugins {
    java
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.7"
}

/* ───────────── 2. PROJECT INFO ──────── */
group = "com.gammatunes"
version = project.version
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

/* ───────────── 3. SOURCE SETS ───────── */
sourceSets {
    val main by getting {
        java.srcDir("backend/src/main/java")
        resources.srcDir("backend/src/main/resources")
    }
    val test by getting {
        java.srcDir("backend/src/test/java")
        resources.srcDir("backend/src/test/resources")
    }
    val integrationTest by creating {
        java.srcDir("backend/src/integrationTest/java")
        resources.srcDir("backend/src/integrationTest/resources")

        compileClasspath += sourceSets.main.get().output +
            configurations.testRuntimeClasspath.get()
        runtimeClasspath  += output + compileClasspath
    }
}

/* task that runs the integrationTest source‑set */
tasks.register<Test>("integrationTest") {
    description = "Runs Docker‑backed integration tests"
    group       = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath       = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

/* Make the integrationTest configurations inherit the regular test ones */
configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

/* ───────────── 4. REPOSITORIES ─────── */
repositories { mavenCentral() }

/* ───────────── 5. VERSIONS ─────────── */
val testcontainersVersion = "1.21.3"
val redisTcVersion        = "2.2.4"

/* ───────────── 6. DEPENDENCIES ─────── */
dependencies {
    /* application */
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    /* unit‑test */
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")

    /* integration‑test */
    "integrationTestImplementation"(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    "integrationTestImplementation"("org.testcontainers:junit-jupiter")
    "integrationTestImplementation"("com.redis:testcontainers-redis:$redisTcVersion")
    "integrationTestImplementation"("io.rest-assured:rest-assured:5.4.0")
    "integrationTestImplementation"("org.awaitility:awaitility:4.2.0")
}

tasks.withType<Test> { useJUnitPlatform() }

/* ───── 7. DOCKER‑COMPOSE HELPER TASKS ─ */
val composeUp by tasks.registering(Exec::class) {
    group = "verification"
    description = "Build (if needed) & start the full stack"
    commandLine("docker", "compose", "up", "--build", "-d")
}

val composeDown by tasks.registering(Exec::class) {
    group = "verification"
    description = "Stop stack and remove volumes"
    commandLine("docker", "compose", "down", "-v")
}

/* link compose to integrationTest */
tasks.named("integrationTest") {
    dependsOn(composeUp)
    finalizedBy(composeDown)
}

/* include smoke tests in `check` */
tasks.named("check") { dependsOn("integrationTest") }

/* ───── 8. SINGLE LOCAL GREEN‑LIGHT TASK ─ */
tasks.register("verifyAll") {
    group = "verification"
    description = "clean → test → composeUp → integrationTest → composeDown"
    dependsOn("clean", "test", "integrationTest")
}
