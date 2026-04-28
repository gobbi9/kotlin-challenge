# Stage 1: Build
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /home/gradle/src
COPY . .
RUN ./gradlew :cleanup:buildFatJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=build /home/gradle/src/cleanup/build/libs/cleanup.jar cleanup.jar
EXPOSE 8083
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "cleanup.jar"]
