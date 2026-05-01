package it.schwarz.coupon.model.rest

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

/**
 * Data Transfer Object representing a coupon.
 *
 * This class is used for communicating coupon data through the REST API.
 * It encapsulates all coupon attributes, including its unique code, discount,
 * and lifecycle timestamps.
 *
 * Example insertion JSON:
 * ```json
 * { "code": "abc123", "discount": 12.54, "description": "best coupon ev4", "applicationCount": 0 }
 * ```
 */
@Serializable
data class CouponDto(
    /**
     * The unique identifier of the coupon.
     */
    @Contextual
    val id: ObjectId = ObjectId(),
    /**
     * The unique coupon code.
     */
    val code: String,
    /**
     * The discount value associated with the coupon.
     */
    @Contextual
    val discount: BigDecimal,
    /**
     * A description of the coupon.
     */
    val description: String,
    /**
     * The number of times this coupon has been applied.
     */
    val applicationCount: Int = -1,
    /**
     * The version of the coupon, used for optimistic locking.
     */
    val version: Int = -1,
    /**
     * The timestamp when the coupon was created.
     */
    @Contextual
    val creationDateTime: Instant = Instant.now(),
    /**
     * The timestamp when the coupon was last updated.
     */
    @Contextual
    val updateDateTime: Instant = Instant.now(),
)
