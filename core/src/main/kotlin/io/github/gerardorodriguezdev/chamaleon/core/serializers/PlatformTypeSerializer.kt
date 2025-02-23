package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class PlatformTypeSerializer : KSerializer<PlatformType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PlatformType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PlatformType) {
        encoder.encodeString(value.serialName)
    }

    override fun deserialize(decoder: Decoder): PlatformType {
        val serialName = decoder.decodeString()
        return PlatformType.entries.first { platformType -> serialName == platformType.serialName }
    }
}