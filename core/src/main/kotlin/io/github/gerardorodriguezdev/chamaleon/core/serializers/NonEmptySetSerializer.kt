package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptySet
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.NonEmptySet.Companion.toNonEmptySet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public class NonEmptySetSerializer<T>(elementSerializer: KSerializer<T>) : KSerializer<NonEmptySet<T>> {
    private val delegate = SetSerializer(elementSerializer)

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: NonEmptySet<T>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): NonEmptySet<T> {
        val set = delegate.deserialize(decoder)
        val nonEmptySet = set.toNonEmptySet()
        if (nonEmptySet == null) throw SerializationException("NonEmpty set cannot be empty")
        return nonEmptySet
    }
}