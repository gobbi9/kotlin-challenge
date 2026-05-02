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
RUN ./gradlew :db-migrations:dependencies --no-daemon

COPY . .
RUN ./gradlew :db-migrations:jar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=build /home/gradle/src/db-migrations/build/libs/db-migrations.jar db-migrations.jar

ENTRYPOINT ["java", "-jar", "db-migrations.jar"]
