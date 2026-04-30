package it.schwarz.coupon.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        if (encoder is JsonEncoder) {
            encoder.encodeJsonElement(JsonPrimitive(value))
        } else {
            encoder.encodeString(value.toPlainString())
        }
    }

    override fun deserialize(decoder: Decoder): BigDecimal = if (decoder is JsonDecoder) {
        decoder.decodeJsonElement().jsonPrimitive.content.toBigDecimal()
    } else {
        BigDecimal(decoder.decodeString())
    }
}

object ObjectIdSerializer : KSerializer<ObjectId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ObjectId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ObjectId) = encoder.encodeString(value.toHexString())

    override fun deserialize(decoder: Decoder): ObjectId = ObjectId(decoder.decodeString())
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
