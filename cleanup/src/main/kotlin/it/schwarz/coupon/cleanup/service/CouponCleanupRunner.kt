package it.schwarz.coupon.cleanup.service

import com.mongodb.client.model.Filters
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.cleaner.CREATION_DATE_TIME_FIELD_NAME
import it.schwarz.coupon.cleanup.cleaner.CollectionCleaner
import java.time.Instant
import java.time.temporal.ChronoUnit

private val log = KotlinLogging.logger { }

/**
 * Implementation of [CleanupRunner] that handles the cleanup of coupon documents.
 *
 * It uses a [CollectionCleaner] to remove coupons from the database that have
 * exceeded the specified retention period.
 */
class CouponCleanupRunner(
    private val collectionCleaner: CollectionCleaner,
    private val currentTime: Instant,
    private val retentionMinutes: Long,
) : CleanupRunner {
    /**
     * Triggers the cleanup process for the coupons collection based on retention settings.
     */
    @WithSpan("doCleanup")
    override suspend fun doCleanup() {
        log.info { "Starting coupon cleanup. Retention minutes: $retentionMinutes. Current time: $currentTime" }
        collectionCleaner.clean(
            collectionName = "coupons",
            filter = Filters.lt(
                CREATION_DATE_TIME_FIELD_NAME,
                currentTime.minus(retentionMinutes, ChronoUnit.MINUTES),
            ),
        )
        log.debug { "Finished coupon cleanup" }
    }
}
