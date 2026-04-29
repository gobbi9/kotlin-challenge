package it.schwarz.coupon.cleanup.repository

import org.bson.conversions.Bson
import org.bson.types.ObjectId

interface DocumentRepository {
    suspend fun findIdsByCreationDateTimeLessThan(
        collectionName: String,
        cleanupFilter: Bson,
    ): List<ObjectId>

    suspend fun deleteByIds(
        collectionName: String,
        ids: List<ObjectId>,
    ): Long
}
