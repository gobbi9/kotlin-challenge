# Stage 1: Build
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /home/gradle/src

# Cache Gradle wrapper and distribution
COPY gradlew .
COPY gradle gradle
RUN ./gradlew --version --no-daemon

# Cache dependencies
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
# Copy subprojects' build files to cache their dependencies
COPY cleanup/build.gradle.kts cleanup/
COPY service/build.gradle.kts service/
COPY model/build.gradle.kts model/
COPY configuration/build.gradle.kts configuration/
COPY db-migrations/build.gradle.kts db-migrations/
COPY frontend/build.gradle.kts frontend/
COPY documentation/build.gradle.kts documentation/
RUN ./gradlew :service:dependencies --no-daemon

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
