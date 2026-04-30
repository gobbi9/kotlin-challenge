package it.schwarz.coupon.cleanup.repository

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

// I personally only create interfaces if multiple implementations are needed.
class DocumentRepository(
    private val mongoDatabase: MongoDatabase,
) {
    suspend fun findIdsByCreationDateTimeLessThan(
        collectionName: String,
        cleanupFilter: Bson,
    ): List<ObjectId> {
        val mongoCollection = mongoDatabase.getCollection<Document>(collectionName)
        return mongoCollection
            .find(cleanupFilter)
            .projection(Document("_id", 1))
            .mapNotNull { doc -> doc["_id"] as? ObjectId }
            .toList()
    }

    suspend fun deleteByIds(
        collectionName: String,
        ids: List<ObjectId>,
    ): Long {
        val mongoCollection = mongoDatabase.getCollection<Document>(collectionName)
        return mongoCollection.deleteMany(Filters.`in`("_id", ids)).deletedCount
    }
}
