package it.schwarz.coupon.cleanup.cleaner

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.repository.CleanupCouponRepository
import org.bson.conversions.Bson

private val log = KotlinLogging.logger { }

const val CREATION_DATE_TIME_FIELD_NAME = "creationDateTime"

class CollectionCleaner(
    private val cleanupCouponRepository: CleanupCouponRepository,
) {
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
