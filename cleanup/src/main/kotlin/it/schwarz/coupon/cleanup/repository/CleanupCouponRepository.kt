package it.schwarz.coupon.cleanup.repository

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.bson.Document
import org.bson.conversions.Bson

// I personally only create interfaces if multiple implementations are needed.
class CleanupCouponRepository(
    private val mongoDatabase: MongoDatabase,
) {
    @WithSpan("deleteByFilter")
    suspend fun deleteByFilter(
        collectionName: String,
        filter: Bson,
    ): Long {
        val mongoCollection = mongoDatabase.getCollection<Document>(collectionName)
        return mongoCollection.deleteMany(filter).deletedCount
    }
}
