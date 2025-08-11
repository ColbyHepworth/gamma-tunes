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
    id("com.github.node-gradle.node")     version "7.0.2"   // React build
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
    implementation("net.dv8tion:JDA:5.6.1")
    implementation("dev.arbjerg:lavalink-client:3.2.0")


    /* Convenience / JSON */
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("com.google.code.gson:gson:2.10.1")

    /* Database driver */
    runtimeOnly("org.postgresql:postgresql")

    /* Testing */
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/* ───────────── 4. NODE / VITE (REACT FRONTEND) ───────────── */
//node {
//    download.set(true)                         // use a project-local Node binary
//    version.set("20.14.0")
//    npmVersion.set("10.7.0")
//    nodeProjectDir.set(file("${project.projectDir}/frontend"))
//}

/* Build React → frontend/dist */
//val npmInstall by tasks.registering(com.github.gradle.node.npm.task.NpmInstallTask::class)
//val npmBuild   by tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
//    dependsOn(npmInstall)
//    args.set(listOf("run", "build"))
//}
//val copyFrontend by tasks.registering(Copy::class) {
//    dependsOn(npmBuild)
//    from(file("frontend/dist"))
//    into(layout.buildDirectory.dir("frontend"))   // <build>/frontend/
//}

/* ───────────── 5. SPRING BOOT PACKAGING ───────────── */
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("gamma-tunes.jar")
    mainClass.set("com.gammatunes.GammaTunesApplication")  // adjust if your class moved
    /* embed React static assets under BOOT-INF/classes/static */
    //    dependsOn(copyFrontend)
    from(layout.buildDirectory.dir("frontend")) { into("static") }
}

/* ───────────── 6. TESTS ───────────── */
//tasks.withType<Test> { useJUnitPlatform() }

/* ───────────── 7. DOCKER-COMPOSE HELPERS ───────────── */
tasks.register<Exec>("composeUp") {
    group = "verification"
    description = "Build & start the full stack via Docker Compose"
    dependsOn(tasks.named("bootJar"))          // root bootJar
    commandLine("docker", "compose", "up", "--build", "-d")
}

tasks.register<Exec>("composeDown") {
    group = "verification"
    description = "Stop stack and remove volumes"
    commandLine("docker", "compose", "down", "-v")
}
tasks.register("verifyAll") {
    group = "verification"
    description = "Runs all checks and tests for the project."
    dependsOn("clean", "check")
}
