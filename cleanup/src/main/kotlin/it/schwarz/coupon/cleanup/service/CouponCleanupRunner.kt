package it.schwarz.coupon.cleanup.service

import com.mongodb.client.model.Filters
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.cleaner.CREATION_DATE_TIME_FIELD_NAME
import it.schwarz.coupon.cleanup.cleaner.CollectionCleaner
import java.time.Instant
import java.time.temporal.ChronoUnit

private val log = KotlinLogging.logger { }

class CouponCleanupRunner(
    private val collectionCleaner: CollectionCleaner,
    private val currentTime: Instant,
    private val retentionMinutes: Long,
) : CleanupRunner {
    @WithSpan("doCleanup")
    override suspend fun doCleanup() {
        log.debug { "Starting coupon cleanup" }
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
