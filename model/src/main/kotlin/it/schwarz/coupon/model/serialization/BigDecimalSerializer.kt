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
import org.bson.BsonDateTime
import org.bson.codecs.kotlinx.BsonEncoder
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
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

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        when (encoder) {
            is BsonEncoder -> encoder.encodeBsonValue(BsonDateTime(value.toEpochMilli()))
            else -> encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): Instant =
        when (decoder) {
            is org.bson.codecs.kotlinx.BsonDecoder -> Instant.ofEpochMilli(decoder.decodeBsonValue().asDateTime().value)
            else -> Instant.parse(decoder.decodeString())
        }
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        when (encoder) {
            is BsonEncoder -> encoder.encodeBsonValue(BsonDateTime(value.toInstant(ZoneOffset.UTC).toEpochMilli()))
            else -> encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        }
    }

    override fun deserialize(decoder: Decoder): LocalDateTime =
        when (decoder) {
            is org.bson.codecs.kotlinx.BsonDecoder ->
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(decoder.decodeBsonValue().asDateTime().value),
                    ZoneOffset.UTC,
                )
            else -> LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
}
