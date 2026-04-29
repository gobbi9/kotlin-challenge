plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    application
}

application {
    mainClass.set("it.schwarz.coupon.migrations.MainKt")
}

dependencies {
    implementation(project(":configuration"))
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation(libs.mongodb.bson)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.coroutines.test) // For runBlocking in Main

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter)
}

tasks.jar {
    archiveFileName.set("db-migrations.jar")
    manifest {
        attributes["Main-Class"] = "it.schwarz.coupon.migrations.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.register<JavaExec>("migrateMongo") {
    group = "database"
    description = "Runs MongoDB migrations"
    mainClass.set("it.schwarz.coupon.migrations.MainKt")
    classpath = sourceSets["main"].runtimeClasspath

    // Pass environment variables to the task if needed,
    // but the requirement says to read from env vars in the entry point.
    // Gradle's JavaExec by default inherits the system environment.
}

tasks.test {
    useJUnitPlatform()
}
