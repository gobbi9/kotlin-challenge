package it.schwarz.coupon.model.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * Defines the [SerializersModule] for the coupon application.
 *
 * This module registers custom serializers for types that are not natively supported
 * by Kotlin serialization, such as [BigDecimal], [ObjectId], [Instant], and [LocalDateTime].
 */
val couponSerializersModule = SerializersModule {
    contextual(BigDecimalSerializer)
    contextual(ObjectIdSerializer)
    contextual(InstantSerializer)
    contextual(LocalDateTimeSerializer)
}
