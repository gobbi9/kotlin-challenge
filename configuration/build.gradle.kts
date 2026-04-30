plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-test-fixtures`
}

dependencies {
    api(project(":model"))
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.bson.kotlinx)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)

    testFixturesImplementation(libs.testcontainers.mongodb)
    testFixturesImplementation(libs.testcontainers.core)
    testFixturesImplementation(libs.kotest.extensions.testcontainers)
    testFixturesImplementation(libs.kotest.runner.junit5)
    testFixturesImplementation(libs.mongodb.driver.kotlin.coroutine)
    testFixturesImplementation(libs.mongodb.bson)
    testFixturesImplementation(project(":db-migrations"))
}
