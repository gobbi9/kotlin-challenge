# Stage 1: Build
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /home/gradle/src

# Cache Gradle wrapper and dependencies
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
# Copy subprojects' build files to cache their dependencies
COPY cleanup/build.gradle.kts cleanup/
COPY service/build.gradle.kts service/
RUN ./gradlew :cleanup:dependencies --no-daemon || true

COPY . .
RUN ./gradlew :cleanup:buildFatJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=build /home/gradle/src/cleanup/build/libs/cleanup.jar cleanup.jar

# Download OpenTelemetry Java Agent (cached by Docker if URL doesn't change)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

EXPOSE 8083
ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "--enable-native-access=ALL-UNNAMED", "-jar", "cleanup.jar"]
