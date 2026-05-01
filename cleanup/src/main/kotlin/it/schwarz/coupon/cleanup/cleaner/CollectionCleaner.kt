package it.schwarz.coupon.cleanup.cleaner

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.repository.CleanupCouponRepository
import org.bson.conversions.Bson

private val log = KotlinLogging.logger { }

/**
 * The name of the field representing the creation date and time in the database documents.
 */
const val CREATION_DATE_TIME_FIELD_NAME = "creationDateTime"

/**
 * Service responsible for cleaning up collections in the database.
 *
 * It provides a generic way to delete documents from any collection based on a provided filter.
 */
class CollectionCleaner(
    private val cleanupCouponRepository: CleanupCouponRepository,
) {
    /**
     * Executes the cleanup for a specific collection.
     */
    @WithSpan("clean")
    suspend fun clean(
        collectionName: String,
        filter: Bson,
    ) {
        log.info { "Starting cleanup in $collectionName. Filter: $filter" }
        val deletedCount = cleanupCouponRepository.deleteByFilter(
            collectionName = collectionName,
            filter = filter,
        )
        log.info { "Deleted $deletedCount documents in $collectionName" }
        log.info { "Cleanup finished for $collectionName" }
    }
}
