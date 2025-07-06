/* ───────────── 1. PLUGINS ───────────── */
plugins {
    java
    id("org.springframework.boot") version "3.3.1" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

/* ───────────── 2. SHARED CONFIGURATION FOR ALL SUB-PROJECTS ───────────── */
subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    group = "com.gammatunes"
    version = project.version

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://m2.dv8tion.net/releases") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://nexus.sedmelluq.net/content/groups/public/") }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

/* ───────────── 3. BACKEND-SPECIFIC CONFIGURATION ───────────── */
project(":backend") {
    apply(plugin = "org.springframework.boot")

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
    }

    // Add integrationTest to the 'check' lifecycle task
    tasks.check {
        dependsOn(integrationTest)
    }

    // Dependencies for the backend
    dependencies {
        val testcontainersVersion = "1.19.8"

        // application
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("com.sedmelluq:lavaplayer:1.3.77")
        add("developmentOnly", "org.springframework.boot:spring-boot-devtools")

        // unit-test
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.projectreactor:reactor-test")

        // integration-test
        "integrationTestImplementation"(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
        "integrationTestImplementation"("org.testcontainers:junit-jupiter")
        "integrationTestImplementation"("io.rest-assured:rest-assured:5.4.0")
        "integrationTestImplementation"("org.awaitility:awaitility:4.2.1")
    }
}

/* ───────────── 4. BOT-JDA-SPECIFIC CONFIGURATION ───────────── */
project(":bot-jda") {
    // This task configures the project to build a "fat jar" that includes all dependencies.
    tasks.jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest.attributes["Main-Class"] = "com.gammatunes.bot.JdaBotApplication"
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    // Define and configure the integrationTest source set for this subproject
    sourceSets {
        create("integrationTest") {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }

    configurations {
        val integrationTestImplementation by getting { extendsFrom(configurations.testImplementation.get()) }
        val integrationTestRuntimeOnly by getting { extendsFrom(configurations.testRuntimeOnly.get()) }
    }

    dependencies {
        implementation("net.dv8tion:JDA:5.0.0-beta.24")
        implementation("com.squareup.okhttp3:okhttp:4.12.0") // For making HTTP calls to the backend
        implementation("com.google.code.gson:gson:2.10.1") // For handling JSON
        implementation("ch.qos.logback:logback-classic:1.5.6") // Logging
        "integrationTestImplementation"("org.junit.jupiter:junit-jupiter-api:5.10.2")
        "integrationTestImplementation"("org.awaitility:awaitility:4.2.1")
    }
}

/* ───────────── 5. ROOT-LEVEL HELPER TASKS ───────────── */
tasks.register("composeUp", Exec::class) {
    group = "verification"
    description = "Build (if needed) & start the full stack via Docker Compose"
    dependsOn(":backend:bootJar", ":bot-jda:jar")
    commandLine("docker", "compose", "up", "--build", "-d")
}

tasks.register("composeDown", Exec::class) {
    group = "verification"
    description = "Stop stack and remove volumes"
    commandLine("docker", "compose", "down", "-v")
}

// Link compose tasks to the backend's smokeTest task
project(":backend").tasks.named<Test>("smokeTest") {
    dependsOn(rootProject.tasks.named("composeUp"))
    finalizedBy(rootProject.tasks.named("composeDown"))
}

// Define the root verifyAll task that runs the main backend tests
tasks.register("verifyAll") {
    group = "verification"
    description = "clean → test → integrationTest"
    dependsOn(tasks.named(":backend:clean"), tasks.named(":backend:test"), tasks.named(":backend:integrationTest"))
}
