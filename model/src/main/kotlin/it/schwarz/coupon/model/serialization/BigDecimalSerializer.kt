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

/**
 * Serializer for [BigDecimal] objects.
 *
 * This serializer handles the conversion of [BigDecimal] to and from its string or numeric representation,
 * specifically tailored for JSON and BSON formats.
 */
object BigDecimalSerializer : KSerializer<BigDecimal> {
    /**
     * The descriptor for the [BigDecimal] type.
     */
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    /**
     * Serializes a [BigDecimal] value into the given encoder.
     */
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        if (encoder is JsonEncoder) {
            encoder.encodeJsonElement(JsonPrimitive(value))
        } else {
            encoder.encodeString(value.toPlainString())
        }
    }

    /**
     * Deserializes a [BigDecimal] value from the given decoder.
     */
    override fun deserialize(decoder: Decoder): BigDecimal = if (decoder is JsonDecoder) {
        decoder.decodeJsonElement().jsonPrimitive.content.toBigDecimal()
    } else {
        BigDecimal(decoder.decodeString())
    }
}

/**
 * Serializer for [ObjectId] objects.
 *
 * Provides the logic to serialize and deserialize MongoDB [ObjectId]s as strings.
 */
object ObjectIdSerializer : KSerializer<ObjectId> {
    /**
     * The descriptor for the [ObjectId] type.
     */
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ObjectId", PrimitiveKind.STRING)

    /**
     * Serializes an [ObjectId] value.
     */
    override fun serialize(encoder: Encoder, value: ObjectId) = encoder.encodeString(value.toHexString())

    /**
     * Deserializes an [ObjectId] value.
     */
    override fun deserialize(decoder: Decoder): ObjectId = ObjectId(decoder.decodeString())
}

/**
 * Serializer for [Instant] objects.
 *
 * Supports serialization to BSON date-time or ISO-8601 string representation.
 */
object InstantSerializer : KSerializer<Instant> {
    /**
     * The descriptor for the [Instant] type.
     */
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    /**
     * Serializes an [Instant] value.
     */
    override fun serialize(encoder: Encoder, value: Instant) {
        when (encoder) {
            is BsonEncoder -> encoder.encodeBsonValue(BsonDateTime(value.toEpochMilli()))
            else -> encoder.encodeString(value.toString())
        }
    }

    /**
     * Deserializes an [Instant] value.
     */
    override fun deserialize(decoder: Decoder): Instant =
        when (decoder) {
            is org.bson.codecs.kotlinx.BsonDecoder -> Instant.ofEpochMilli(decoder.decodeBsonValue().asDateTime().value)
            else -> Instant.parse(decoder.decodeString())
        }
}

/**
 * Serializer for [LocalDateTime] objects.
 *
 * Handles conversion between [LocalDateTime] and its representation in BSON or ISO-8601 strings.
 */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    /**
     * The descriptor for the [LocalDateTime] type.
     */
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    /**
     * Serializes a [LocalDateTime] value.
     */
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        when (encoder) {
            is BsonEncoder -> encoder.encodeBsonValue(BsonDateTime(value.toInstant(ZoneOffset.UTC).toEpochMilli()))
            else -> encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        }
    }

    /**
     * Deserializes a [LocalDateTime] value.
     */
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
