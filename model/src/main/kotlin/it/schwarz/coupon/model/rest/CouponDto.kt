package it.schwarz.coupon.model.rest

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

/**
Insert with
```json
{ "code": "abc123", "discount": 12.54, "description": "best coupon ev4", "applicationCount": 0 }
```
 */
@Serializable
data class CouponDto(
    @Contextual
    val id: ObjectId = ObjectId(),
    val code: String,
    @Contextual
    val discount: BigDecimal,
    val description: String,
    val applicationCount: Int = -1,
    val version: Int = -1,
    @Contextual
    val creationDateTime: Instant = Instant.now(),
    @Contextual
    val updateDateTime: Instant = Instant.now(),
)
