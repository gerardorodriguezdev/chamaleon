package io.github.gerardorodriguezdev.chamaleon.core.serializers

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.KeyProvider
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toNonEmptyKeySetStore
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public class NonEmptyKeyStoreSerializer<K, V : KeyProvider<K>>(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>,
) : KSerializer<NonEmptyKeySetStore<K, V>> {
    private val delegate = SetSerializer(valueSerializer)

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: NonEmptyKeySetStore<K, V>
    ) {
        delegate.serialize(encoder, value.value.values.toSet())
    }

    override fun deserialize(decoder: Decoder): NonEmptyKeySetStore<K, V> {
        val values = delegate.deserialize(decoder)
        val nonEmptyKeySetStore = values.toNonEmptyKeySetStore()
        if (nonEmptyKeySetStore == null) throw SerializationException("NonEmptyKeyStore cannot be empty")
        return nonEmptyKeySetStore
    }
}