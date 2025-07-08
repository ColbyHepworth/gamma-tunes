FROM gradle:8.13-jdk21-jammy AS builder
WORKDIR /workspace/app

# Copy all necessary files for the build
COPY ../../../../../../../build.gradle.kts settings.gradle.kts ./
COPY ../../../../.. ./backend/src

# Build the single backend project
RUN ./gradlew :backend:bootJar --no-daemon

# Stage 2: Create the final runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from the correct path
COPY --from=builder /workspace/app/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
