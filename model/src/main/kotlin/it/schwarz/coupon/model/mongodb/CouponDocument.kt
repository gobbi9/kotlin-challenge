package it.schwarz.coupon.model.mongodb

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

/**
 * Data class representing a coupon document in the database.
 *
 * This class stores all relevant information for a coupon, including its unique
 * code, discount, and application statistics. It also includes fields for tracking
 * creation and update times, and a version for optimistic locking.
 */
@Serializable
data class CouponDocument(
    /**
     * The unique identifier of the coupon document.
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
    var applicationCount: Int = 0,
    /**
     * The version of the document, used for optimistic locking.
     */
    var version: Int = 1,
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
