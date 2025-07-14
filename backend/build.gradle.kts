/* ───────────── 1. PLUGINS ─────────────
   • Give each plugin its version here so the Spring-Boot BOM is applied.
   • The IDEA plugin lets IntelliJ mark the custom source-set.                */
plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.7"
    idea
}

/* ───────────── 2. BOOT-JAR CONFIGURATION ─────────────
   Produces backend-app.jar with the right entry-point class.                  */
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("backend-app.jar")
    mainClass.set("com.gammatunes.backend.BackendApplication")
}

/* ───────────── 3. DEPENDENCIES ─────────────                                 */
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.1"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("dev.arbjerg:lavaplayer:2.2.4")
    implementation("net.dv8tion:JDA:6.0.0-preview")

    implementation("dev.lavalink.youtube:v2:1.13.3")

    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
