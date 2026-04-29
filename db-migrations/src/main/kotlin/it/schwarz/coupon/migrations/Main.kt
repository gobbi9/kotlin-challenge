package it.schwarz.coupon.migrations

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

fun main() {
    val mongodbUri = System.getenv("MONGODB_URI")
    val databaseName = System.getenv("MONGODB_DATABASE")

    if (mongodbUri == null || databaseName == null) {
        log.error { "Missing environment variables: MONGODB_URI or MONGODB_DATABASE" }
        error("Missing environment variables: MONGODB_URI or MONGODB_DATABASE")
    }

    MongoMigrations.run(mongodbUri, databaseName)
}
