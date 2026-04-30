plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sonarqube)
    jacoco
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    fatJar {
        archiveFileName.set("service.jar")
    }
}

dependencies {
    // KTOR
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.call.logging)

    // Logging
    implementation(libs.logback.classic)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.kotlin.logging)

    // Serialization:
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)

    // Koin:
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // Mongo DB
    implementation(libs.mongodb.driver.kotlin.sync)
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.bson.kotlinx)

    // Bridge from java jul logging to slf (logback) logging:
    implementation(libs.jul.to.slf4j)

    // Tracing
    implementation(platform(libs.opentelemetry.bom))
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.instrumentation.mongo)
    implementation(libs.opentelemetry.extension.kotlin)

    implementation(libs.opentelemetry.instrumentation.annotations)
    implementation(project(":model"))
    implementation(project(":configuration"))
    testImplementation(testFixtures(project(":configuration")))

    // Json schema validation:
    implementation(libs.json.kotlin.schema)

    // Http client
    implementation(libs.ktor.client.core.jvm)
    implementation(libs.ktor.client.cio.jvm)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // Unit Testing & Mocking
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.kotest.extensions.testcontainers)
    // Koin tests:
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    // Ktor test
    testImplementation(libs.ktor.server.test.host)
    // Mock for client requests
    testImplementation(libs.ktor.client.mock)
}

tasks.register("prepareKotlinBuildScriptModel") {
    // Dummy task for IDE
}
