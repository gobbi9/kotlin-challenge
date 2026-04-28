package it.schwarz.coupon.cleanup.service

import com.mongodb.client.model.Filters
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.bson.conversions.Bson
import java.time.Instant
import java.time.temporal.ChronoUnit

class CouponCleanupRunner(
    documentRepository: DocumentRepository,
    private val currentTime: Instant,
    private val retentionMinutes: Long,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : CollectionCleanupRunner(documentRepository, scope) {
    override fun getCollectionName(): String = "coupons"

    override fun getFilter(): Bson =
        Filters.lt(
            CREATION_DATE_TIME_FIELD_NAME,
            currentTime.minus(retentionMinutes, ChronoUnit.MINUTES),
        )
}
