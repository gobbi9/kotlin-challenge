package it.schwarz.coupon.model.mongodb

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class CouponDocument(
    @Contextual
    val id: ObjectId = ObjectId(),
    /**
     * Unique code
     */
    val code: String,
    @Contextual
    val discount: BigDecimal,
    val description: String,
    var applicationCount: Int = 0,
    /**
     * For optimistic locking
     */
    var version: Int = 1,
    @Contextual
    val creationDateTime: LocalDateTime = LocalDateTime.now(),
    @Contextual
    val updateDateTime: LocalDateTime = LocalDateTime.now(),
)
