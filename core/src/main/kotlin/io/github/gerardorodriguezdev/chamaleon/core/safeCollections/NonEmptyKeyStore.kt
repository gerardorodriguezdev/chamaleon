package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptyKeyStoreSerializer
import kotlinx.serialization.Serializable

@Serializable(with = NonEmptyKeyStoreSerializer::class)
public class NonEmptyKeyStore<K, V : KeyProvider<K>> private constructor(
    public val value: Map<K, V>
) : Map<K, V> by value {

    public fun addValues(input: NonEmptyKeyStore<K, V>): NonEmptyKeyStore<K, V> {
        val newValues = value.values.toSet() + input.values.toSet()
        val newMap = newValues.associateBy { it.key }
        return NonEmptyKeyStore(newMap)
    }

    public companion object {
        public fun <K, V : KeyProvider<K>> of(input: Set<V>): NonEmptyKeyStore<K, V>? {
            val map = input.associateBy { item -> item.key }
            return if (input.isEmpty()) null else NonEmptyKeyStore<K, V>(map)
        }
    }
}