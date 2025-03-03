package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptyString.Companion.toNonEmptyString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object NonEmptyStringSerializer : KSerializer<NonEmptyString> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NonEmptyString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NonEmptyString) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): NonEmptyString {
        val string = decoder.decodeString()
        val nonEmptyString = string.toNonEmptyString()
        if (nonEmptyString == null) throw SerializationException("NonEmpty string cannot be empty")
        return nonEmptyString
    }
}