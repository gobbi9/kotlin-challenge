package it.schwarz.coupon.configuration

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import it.schwarz.coupon.model.serialization.couponSerializersModule
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider

private val log = KotlinLogging.logger {}

class Database {
    fun configureDatabase(dbURI: String): MongoClient {
        log.debug { "Configuring database" }

        val codecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromProviders(
                KotlinSerializerCodecProvider(serializersModule = couponSerializersModule),
            ),
            CodecRegistries.fromCodecs(
                LocalDateCodec(),
                LocalDateTimeCodec(),
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
