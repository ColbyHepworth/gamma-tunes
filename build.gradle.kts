/* ───────────── 0. ROOT PROJECT INFO ───────────── */
group = "com.gammatunes"
version = "0.0.1-SNAPSHOT"
java          { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
/* ───────────── 1. PLUGINS ───────────── */
plugins {
    java
    idea
    id("org.springframework.boot")        version "3.3.2"
    id("io.spring.dependency-management") version "1.1.5"
}

/* ───────────── 2. REPOSITORIES ───────────── */
repositories {
    mavenCentral()
    maven { url = uri("https://m2.dv8tion.net/releases") }   // JDA
    maven { url = uri("https://jitpack.io") }                // misc
    maven { url = uri("https://maven.lavalink.dev/releases") } // Lava client
}

/* ───────────── 3. DEPENDENCIES ───────────── */
dependencies {
    /* Lombok */
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    /* Spring Boot platform (pulls in matching versions) */
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.2"))

    /* Spring starters */
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    /* Discord + audio */
    implementation("net.dv8tion:JDA:6.3.2")
    implementation("moe.kyokobot.libdave:adapter-jda:0.1.2")
    implementation("moe.kyokobot.libdave:impl-jni:0.1.2")
    implementation("dev.arbjerg:lavalink-client:3.4.0")

    /* Convenience / JSON */
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("com.google.code.gson:gson:2.10.1")

    /* Database driver */
    runtimeOnly("org.postgresql:postgresql")

    /* Testing */
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/* ───────────── 4. SPRING BOOT PACKAGING ───────────── */
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("gamma-tunes.jar")
    mainClass.set("com.gammatunes.GammaTunesApplication")
}

/* ───────────── 5. LOCAL DEV (bootRun) ───────────── */
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { it.split("=", limit = 2) }
            .filter { it.size == 2 }
            .forEach { (key, value) -> environment(key.trim(), value.trim()) }
    }
}

/* ───────────── 6. TESTS ───────────── */
tasks.withType<Test> { useJUnitPlatform() }

/* ───────────── 7. DOCKER-COMPOSE HELPERS ───────────── */
tasks.register<Exec>("composeUp") {
    group = "docker"
    description = "Build & start the full stack via Docker Compose"
    dependsOn(tasks.named("bootJar"))
    commandLine("docker", "compose", "up", "--build", "-d")
}

tasks.register<Exec>("composeDown") {
    group = "docker"
    description = "Stop stack and remove volumes"
    commandLine("docker", "compose", "down", "-v")
}
