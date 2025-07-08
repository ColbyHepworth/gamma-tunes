# ---------- Builder ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace/app

ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m"

COPY gradlew .
COPY gradle/ gradle
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY backend/build.gradle.kts ./backend/build.gradle.kts
COPY backend/src ./backend/src

RUN chmod +x gradlew
RUN ./gradlew :backend:clean :backend:bootJar --no-daemon -x test

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /workspace/app/backend/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
