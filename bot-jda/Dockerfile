# Use a lightweight Java runtime as the base image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# The Gradle build process will have already created the fat jar.
# We just need to copy it into our image.
COPY bot-jda/build/libs/*.jar app.jar
COPY common/src ./common/src

# The bot needs its token and the backend URL to function.
# These will be passed in from docker-compose.yml.
ENV DISCORD_BOT_TOKEN=${DISCORD_BOT_TOKEN}
ENV BACKEND_API_URL="http://backend:8080"

ENTRYPOINT ["java", "-jar", "app.jar"]
