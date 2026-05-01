package it.schwarz.coupon.migrations

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Main entry point for the database migration utility.
 *
 * This function reads the MongoDB connection details from environment variables
 * and starts the migration process.
 */
fun main() {
    val mongodbUri = System.getenv("MONGODB_URI")
    val databaseName = System.getenv("MONGODB_DATABASE")

    if (mongodbUri == null || databaseName == null) {
        log.error { "Missing environment variables: MONGODB_URI or MONGODB_DATABASE" }
        error("Missing environment variables: MONGODB_URI or MONGODB_DATABASE")
    }

    MongoMigrations.run(mongodbUri, databaseName)
}
