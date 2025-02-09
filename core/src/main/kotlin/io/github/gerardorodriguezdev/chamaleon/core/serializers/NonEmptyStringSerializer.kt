package io.github.gerardorodriguezdev.chamaleon.core.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object NonEmptyStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NonEmptyString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        if (value.isEmpty()) throw SerializationException("String was empty")
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        val string = decoder.decodeString()
        if (string.isEmpty()) throw SerializationException("String was empty")
        return string
    }
}