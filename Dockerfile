# ---------- 1) BUILD ----------
FROM gradle:8.8-jdk21 AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle clean bootJar --no-daemon

# ---------- 2) RUNTIME ----------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/gamma-tunes.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
