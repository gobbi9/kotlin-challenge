package it.schwarz.coupon.migrations

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import it.schwarz.coupon.configuration.Database
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import org.bson.Document
import java.time.Instant
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

data class Migration(
    val id: String,
    val description: String,
    val action: suspend (MongoDatabase) -> Unit,
)

object MongoMigrations {
    private const val MIGRATIONS_COLLECTION = "schema_migrations"

    private val migrations = listOf(
        Migration(
            id = "001-create-coupons-code-index",
            description = "Create unique index on coupons.code",
            action = { db ->
                val collection = db.getCollection<Document>("coupons")
                collection.createIndex(
                    Indexes.ascending("code"),
                    IndexOptions().unique(true).name("idx_coupons_code_unique"),
                )
            },
        ),
        Migration(
            id = "002-create-coupons-ttl-index",
            description = "Create TTL index on coupons.creationDateTime",
            action = { db ->
                val collection = db.getCollection<Document>("coupons")
                // 3 minutes as per local.env, but we can make it configurable or fixed for the index
                // MongoDB TTL index checks every minute.
                collection.createIndex(
                    Indexes.ascending("creationDateTime"),
                    IndexOptions().expireAfter(3L, TimeUnit.MINUTES).name("idx_coupons_creation_ttl"),
                )
            },
        ),
    )

    fun run(mongodbUri: String, databaseName: String, databaseProvider: () -> Database = { Database() }) {
        log.info { "Starting MongoDB migrations for database: $databaseName" }
        val client = databaseProvider().configureDatabase(mongodbUri)
        val db = client.getDatabase(databaseName)

        runBlocking {
            val collection = db.getCollection<Document>(MIGRATIONS_COLLECTION)

            val appliedMigrations = collection.find()
                .map { it.getString("id") }
                .toSet()

            migrations.sortedBy { it.id }.forEach { migration ->
                if (migration.id in appliedMigrations) {
                    log.info { "Migration ${migration.id} already applied, skipping." }
                } else {
                    log.info { "Applying migration ${migration.id}: ${migration.description}" }
                    try {
                        migration.action(db)
                        collection.insertOne(
                            Document()
                                .append("id", migration.id)
                                .append("description", migration.description)
                                .append("appliedAt", Instant.now()),
                        )
                        log.info { "Migration ${migration.id} applied successfully." }
                    } catch (e: Exception) {
                        log.error(e) { "Migration ${migration.id} failed!" }
                        throw e
                    }
                }
            }
        }
        log.info { "All migrations completed." }
    }
}
