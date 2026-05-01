package it.schwarz.coupon.configuration

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import it.schwarz.coupon.model.serialization.couponSerializersModule
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider

private val log = KotlinLogging.logger {}

/**
 * Database configuration class.
 *
 * This class provides the necessary configuration to connect to a MongoDB instance.
 * It sets up the [MongoClient] with the required codec registries for handling
 * Kotlin serialization and Java 8 date/time types.
 */
class Database {
    /**
     * Configures the [MongoClient] with the provided database URI.
     */
    fun configureDatabase(dbURI: String): MongoClient {
        log.debug { "Configuring database" }

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

        val settings = MongoClientSettings.builder()
            .applyConnectionString(com.mongodb.ConnectionString(dbURI))
            .codecRegistry(codecRegistry)
            .build()

        val client = MongoClient.create(settings)
        return client
    }
}
