# ---------- Builder ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace/app

ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m"

COPY gradlew .
COPY gradle/ gradle
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY backend/src ./backend/src

RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon -x test

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/app/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
