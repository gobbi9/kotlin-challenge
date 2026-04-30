package it.schwarz.coupon.service.repository

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.model.mongodb.CouponDocument
import kotlinx.coroutines.flow.toList

private val log = KotlinLogging.logger {}

class CouponRepository(
    private val database: MongoDatabase,
) {
    private val collection: MongoCollection<CouponDocument> = database.getCollection(collectionName = "coupons")

    @WithSpan("findAll")
    suspend fun findAll(skip: Int = 0, limit: Int = 100): List<CouponDocument> {
        log.debug { "Finding all coupons, skip=$skip, limit=$limit" }
        return collection.find().skip(skip = skip).limit(limit = limit).toList()
    }

    @WithSpan("count")
    suspend fun count(): Long {
        log.debug { "Counting coupons" }
        return collection.countDocuments()
    }

    @WithSpan("findByCodes")
    suspend fun findByCodes(codes: List<String>): List<CouponDocument> {
        log.trace { "Finding coupons by codes: $codes" }
        return collection.find(Filters.`in`("code", codes)).toList()
    }

    @WithSpan("save")
    suspend fun save(coupon: CouponDocument) {
        log.trace { "Saving coupon document: $coupon" }
        collection.insertOne(document = coupon)
    }

    @WithSpan("saveAll")
    suspend fun saveAll(coupons: List<CouponDocument>) {
        log.trace { "Saving coupon documents: $coupons" }
        if (coupons.isNotEmpty()) {
            collection.insertMany(documents = coupons)
        }
    }
}
