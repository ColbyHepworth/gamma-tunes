// backend/build.gradle.kts
plugins {
    id("org.springframework.boot")
}

/* ───────────── 1. BACKEND-SPECIFIC CONFIGURATION ───────────── */
// This task configures the project to build a "fat jar" that includes all dependencies.
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("backend-app.jar")
    mainClass.set("com.gammatunes.backend.BackendApplication")
}

// Define the integrationTest source set
sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

// Configure dependencies for the new source set
configurations {
    val integrationTestImplementation by getting { extendsFrom(configurations.testImplementation.get()) }
    val integrationTestRuntimeOnly by getting { extendsFrom(configurations.testRuntimeOnly.get()) }
}

// Define the integrationTest task
val integrationTest by tasks.registering(Test::class) {
    description = "Runs Docker‑backed integration tests using Testcontainers"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    filter {
        excludeTestsMatching("*SmokeIT")
    }
}

// Define the smokeTest task
val smokeTest by tasks.registering(Test::class) {
    description = "Runs smoke tests against a running application stack"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    filter {
        includeTestsMatching("*SmokeIT")
    }
    // Link to tasks in the root project
    dependsOn(rootProject.tasks.named("composeUp"))
    finalizedBy(rootProject.tasks.named("composeDown"))
}

// Add integrationTest to the 'check' lifecycle task
tasks.check {
    dependsOn(integrationTest)
}

/* ───────────── 2. DEPENDENCIES ───────────── */
dependencies {
    val testcontainersVersion = "1.19.8"

    // application
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("dev.arbjerg:lavaplayer:2.2.4")
    implementation("dev.lavalink.youtube:v2:1.13.3")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // common dependencies
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")

    // bot dependencies
    implementation("net.dv8tion:JDA:5.0.0-beta.24")
    implementation("io.github.cdimascio:dotenv-java:2.3.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // unit-test
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")

    // integration-test
    "integrationTestImplementation"(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    "integrationTestImplementation"("org.testcontainers:junit-jupiter")
    "integrationTestImplementation"("io.rest-assured:rest-assured:5.4.0")
    "integrationTestImplementation"("org.awaitility:awaitility:4.2.1")
}

