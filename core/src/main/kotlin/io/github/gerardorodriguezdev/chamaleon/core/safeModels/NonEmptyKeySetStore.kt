package io.github.gerardorodriguezdev.chamaleon.core.safeModels

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptyKeyStoreSerializer
import kotlinx.serialization.Serializable

@Serializable(with = NonEmptyKeyStoreSerializer::class)
public class NonEmptyKeySetStore<K, V : KeyProvider<K>> private constructor(
    public val value: Map<K, V>
) : Map<K, V> by value {

    public fun addValues(input: NonEmptyKeySetStore<K, V>): NonEmptyKeySetStore<K, V> {
        val newValues = value.values.toSet() + input.values.toSet()
        val newMap = newValues.associateBy { it.key }
        return NonEmptyKeySetStore(newMap)
    }

    override fun toString(): String = value.values.toSet().toString()

    override fun hashCode(): Int = value.values.toSet().hashCode()

    override fun equals(other: Any?): Boolean =
        this === other || value == other || value.values.toSet() == other

    public companion object {
        //TODO: Dup this
        public fun <K, V : KeyProvider<K>> Set<V>.toUnsafeNonEmptyKeyStore(): NonEmptyKeySetStore<K, V> {
            return if (isEmpty()) {
                throw IllegalStateException("Non empty key set store was empty")
            } else {
                NonEmptyKeySetStore<K, V>(
                    associateBy { item -> item.key })
            }
        }

        public fun <K, V : KeyProvider<K>> Set<V>.toNonEmptyKeySetStore(): NonEmptyKeySetStore<K, V>? {
            return if (isEmpty()) null else NonEmptyKeySetStore<K, V>(associateBy { item -> item.key })
        }
    }
}