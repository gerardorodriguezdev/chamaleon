package io.github.gerardorodriguezdev.chamaleon.core.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public class NonEmptySetSerializer<T>(elementSerializer: KSerializer<T>) : KSerializer<Set<T>> {
    private val delegate = SetSerializer(elementSerializer)

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Set<T>) {
        if (value.isEmpty()) {
            throw SerializationException("Set cannot be empty")
        }
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Set<T> {
        val result = delegate.deserialize(decoder)
        if (result.isEmpty()) {
            throw SerializationException("Set cannot be empty")
        }
        return result
    }
}