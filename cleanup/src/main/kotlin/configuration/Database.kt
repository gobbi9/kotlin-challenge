package it.schwarz.coupon.cleanup.configuration

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.jsr310.LocalDateCodec

private val log = KotlinLogging.logger {}

class Database {
    fun configureDatabase(dbURI: String, dbName: String): MongoDatabase {
        log.debug { "Configuring database" }

        val codecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromCodecs(LocalDateCodec()),
        )

        val settings = MongoClientSettings.builder()
            .applyConnectionString(com.mongodb.ConnectionString(dbURI))
            .codecRegistry(codecRegistry)
            .build()

        val client = MongoClient.create(settings)
        val database = client.getDatabase(dbName)

        runBlocking {
            val doc = database.runCommand(Document("ping", 1))
            log.trace { "$doc received as Ping-result" }
        }
        return database
    }
}
