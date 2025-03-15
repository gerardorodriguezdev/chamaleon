package io.github.gerardorodriguezdev.chamaleon.core.safeModels

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptyKeyStoreSerializer
import kotlinx.serialization.Serializable

@Serializable(with = NonEmptyKeyStoreSerializer::class)
public class NonEmptyKeySetStore<K, V : KeyProvider<K>> private constructor(
    public val value: Map<K, V>
) : Map<K, V> by value {

    init {
        if (value.isEmpty()) throw IllegalStateException("Non empty key set store was empty")
    }

    public fun updateElementByKey(newValue: V): NonEmptyKeySetStore<K, V> =
        NonEmptyKeySetStore(
            values
                .map { value ->
                    if (value.key == newValue.key) {
                        newValue
                    } else {
                        value
                    }
                }
                .associateByKey()
        )

    public fun updateElementByIndex(index: Int, newValue: V): NonEmptyKeySetStore<K, V> =
        NonEmptyKeySetStore(
            values
                .mapIndexed { currentIndex, element ->
                    if (index == currentIndex) {
                        newValue
                    } else {
                        element
                    }
                }
                .associateByKey()
        )

    public fun updateElementByIndex(index: Int, block: (V) -> V): NonEmptyKeySetStore<K, V> =
        NonEmptyKeySetStore(
            values
                .mapIndexed { currentIndex, element ->
                    if (index == currentIndex) {
                        block(element)
                    } else {
                        element
                    }
                }
                .associateByKey()
        )

    public fun <A, R : KeyProvider<A>> mapToNonEmptyKeySetStore(block: (V) -> R): NonEmptyKeySetStore<A, R> =
        map { (_, value) ->
            block(value)
        }.toUnsafeNonEmptyKeyStore()

    public fun addAll(input: NonEmptyKeySetStore<K, V>): NonEmptyKeySetStore<K, V> {
        val newValues = value.values.toSet() + input.values.toSet()
        val newMap = newValues.associateBy { it.key }
        return NonEmptyKeySetStore(newMap)
    }

    override fun toString(): String = value.values.toSet().toString()
    override fun hashCode(): Int = value.values.toSet().hashCode()
    override fun equals(other: Any?): Boolean = this === other || value == other || value.values.toSet() == other

    public companion object {
        public fun <K, V : KeyProvider<K>> Collection<V>.toNonEmptyKeySetStore(): NonEmptyKeySetStore<K, V>? {
            return if (isEmpty()) null else NonEmptyKeySetStore<K, V>(associateByKey())
        }

        public fun <K, V : KeyProvider<K>> Collection<V>.toUnsafeNonEmptyKeyStore(): NonEmptyKeySetStore<K, V> =
            NonEmptyKeySetStore<K, V>(associateByKey())

        public fun <K, V : KeyProvider<K>> Map<K, V>.toNonEmptyKeySetStore(): NonEmptyKeySetStore<K, V>? {
            return if (isEmpty()) null else NonEmptyKeySetStore<K, V>(this)
        }

        private fun <K, V : KeyProvider<K>> Collection<V>.associateByKey(): Map<K, V> =
            associateBy { element -> element.key }
    }
}