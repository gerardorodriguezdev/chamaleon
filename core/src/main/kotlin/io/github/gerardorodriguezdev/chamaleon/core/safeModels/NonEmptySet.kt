package io.github.gerardorodriguezdev.chamaleon.core.safeModels

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore.Companion.toUnsafeNonEmptyKeyStore
import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptySetSerializer
import kotlinx.serialization.Serializable

@Serializable(with = NonEmptySetSerializer::class)
public class NonEmptySet<T> private constructor(public val value: Set<T>) : Set<T> by value {

    init {
        if (value.isEmpty()) throw IllegalStateException("Non empty set was empty")
    }

    public fun add(element: T): NonEmptySet<T> {
        val newSet = value + element
        return NonEmptySet(newSet)
    }

    public fun remove(element: T): NonEmptySet<T>? {
        val newSet = value - element
        return newSet.toNonEmptySet()
    }

    public infix fun intersect(other: NonEmptySet<T>): NonEmptySet<T> = NonEmptySet(value intersect other.value)

    public operator fun minus(other: NonEmptySet<T>): NonEmptySet<T> = NonEmptySet(value - other.value)

    public fun <A, R : KeyProvider<A>> mapToNonEmptyKeySetStore(block: (T) -> R): NonEmptyKeySetStore<A, R> =
        map { value ->
            block(value)
        }.toUnsafeNonEmptyKeyStore()

    override fun toString(): String = value.toString()
    override fun hashCode(): Int = value.hashCode()
    override fun equals(other: Any?): Boolean = this === other || value == other

    public companion object {
        public fun <T> List<T>.toNonEmptySet(): NonEmptySet<T>? = if (isEmpty()) null else NonEmptySet(toSet())
        public fun <T> Set<T>.toNonEmptySet(): NonEmptySet<T>? = if (isEmpty()) null else NonEmptySet(this)
        public fun <T> Set<T>.toUnsafeNonEmptySet(): NonEmptySet<T> = NonEmptySet(this)
    }
}