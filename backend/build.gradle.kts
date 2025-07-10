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

/* ───────────── 3. SOURCE-SETS ─────────────
   Adds an “integrationTest” source-set that re-uses the main classpath.      */
sourceSets {
    val main by getting
    val integrationTest by creating {
        compileClasspath += main.output
        runtimeClasspath += main.output
    }
}

/* ───────────── 4. CONFIGURATION INHERITANCE ─────────────   */
configurations {
    named("integrationTestImplementation") { extendsFrom(getByName("testImplementation")) }
    named("integrationTestRuntimeOnly") { extendsFrom(getByName("testRuntimeOnly")) }
}

/* ───────────── 5. TEST TASKS ───────────── */

val integrationTest by tasks.registering(Test::class) {
    description = "Runs component integration tests using Testcontainers"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.named("test"))
}

/* ───────────── 6. LIFECYCLE HOOK ─────────────
   ‘check’ now = unit + component integrations.                               */
tasks.named("check") { dependsOn(integrationTest) }

/* ───────────── 7. DEPENDENCIES ─────────────                                 */
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.1"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("dev.arbjerg:lavaplayer:2.2.4")
    implementation("net.dv8tion:JDA:5.0.0-beta.24")

    implementation("dev.lavalink.youtube:v2:1.13.3")

    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.google.code.gson:gson:2.13.1")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    val tcVersion = "1.19.8"
    "integrationTestImplementation"(platform("org.testcontainers:testcontainers-bom:$tcVersion"))
    "integrationTestImplementation"("org.testcontainers:junit-jupiter")
    "integrationTestImplementation"("io.rest-assured:rest-assured:5.4.0")
    "integrationTestImplementation"("org.awaitility:awaitility:4.2.1")
}
