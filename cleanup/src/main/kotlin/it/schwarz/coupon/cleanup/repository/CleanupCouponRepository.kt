package it.schwarz.coupon.cleanup.repository

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.bson.Document
import org.bson.conversions.Bson

// I personally only create interfaces if multiple implementations are needed.

/**
 * Repository for cleaning up coupons from the database.
 *
 * This class provides methods to delete documents from a specified collection
 * based on a given filter, used primarily for data retention and cleanup tasks.
 */
class CleanupCouponRepository(
    private val mongoDatabase: MongoDatabase,
) {
    /**
     * Deletes documents from the specified collection that match the given filter.
     */
    @WithSpan("deleteByFilter")
    suspend fun deleteByFilter(
        collectionName: String,
        filter: Bson,
    ): Long {
        val mongoCollection = mongoDatabase.getCollection<Document>(collectionName)
        return mongoCollection.deleteMany(filter).deletedCount
    }
}
