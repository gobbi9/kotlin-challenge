package it.schwarz.coupon.configuration

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import it.schwarz.coupon.migrations.MongoMigrations
import it.schwarz.coupon.model.serialization.couponSerializersModule
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider
import org.testcontainers.containers.MongoDBContainer

/**
 * [TestListener] that provides a MongoDB database within a test container environment.
 * Handles database connections, migrations, and property management for test specifications.
 */
class MongoDatabaseTestcontainer(
    private val databaseName: String,
    private val additionalProperties: Map<String, String> = emptyMap(),
) : TestListener {
    companion object {
        private val container = MongoDBContainer("mongo:8.2.7")
        private var client: MongoClient? = null
        private val migratedDatabases = mutableSetOf<String>()

        fun getDatabase(databaseName: String): MongoDatabase {
            if (!container.isRunning) {
                container.start()
            }
            val mongoClient = getClient(databaseName)
            val codecRegistry = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(
                    InstantCodec(),
                    LocalDateCodec(),
                    LocalDateTimeCodec(),
                ),
                CodecRegistries.fromProviders(
                    KotlinSerializerCodecProvider(serializersModule = couponSerializersModule),
                ),
                MongoClientSettings.getDefaultCodecRegistry(),
            )
            return mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry)
        }

        private fun getClient(databaseName: String? = null): MongoClient {
            if (client == null) {
                client = MongoClient.create(container.replicaSetUrl)
            }

            if (databaseName != null && !migratedDatabases.contains(databaseName)) {
                MongoMigrations.run(mongodbUri = container.replicaSetUrl, databaseName = databaseName)
                migratedDatabases.add(databaseName)
            }

            return client!!
        }
    }

    fun getDatabase(): MongoDatabase = getDatabase(databaseName)

    override suspend fun beforeSpec(spec: Spec) {
        if (!container.isRunning) {
            container.start()
        }
        System.setProperty("MONGODB_URI", container.replicaSetUrl)
        databaseName.let { System.setProperty("DATABASE_NAME", it) }
        additionalProperties.forEach { (key, value) ->
            System.setProperty(key, value)
        }
    }

    override suspend fun afterSpec(spec: Spec) {
        System.clearProperty("MONGODB_URI")
        databaseName.let { System.clearProperty("DATABASE_NAME") }
        additionalProperties.keys.forEach { System.clearProperty(it) }

        // We don't stop the container here to allow reuse across specs
        // Kotest doesn't have a built-in "afterAllSpecs" easily accessible from an extension
        // but we can rely on JVM shutdown or manual stop if needed.
        // For now, let's keep it running for performance.
    }
}
