package it.schwarz.coupon.model.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val couponSerializersModule = SerializersModule {
    contextual(BigDecimalSerializer)
    contextual(ObjectIdSerializer)
    contextual(LocalDateTimeSerializer)
}
