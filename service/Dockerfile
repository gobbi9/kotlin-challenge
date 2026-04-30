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
COPY model/build.gradle.kts model/
COPY configuration/build.gradle.kts configuration/
RUN ./gradlew :service:dependencies --no-daemon || true

COPY . .
RUN ./gradlew :service:buildFatJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=build /home/gradle/src/service/build/libs/service.jar service.jar

# Download OpenTelemetry Java Agent (cached by Docker if URL doesn't change)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

EXPOSE 8082
ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "--enable-native-access=ALL-UNNAMED", "-jar", "service.jar"]
