package io.github.gerardorodriguezdev.chamaleon.core.safeModels

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptySetSerializer
import kotlinx.serialization.Serializable

@Serializable(with = NonEmptySetSerializer::class)
public class NonEmptySet<T> private constructor(public val value: Set<T>) : Set<T> by value {

    override fun toString(): String = value.toString()

    override fun hashCode(): Int = value.hashCode()

    override fun equals(other: Any?): Boolean =
        this === other || value == other

    public companion object {
        //TODO: Dup this
        public fun <T> Set<T>.toUnsafeNonEmptySet(): NonEmptySet<T> =
            if (this.isEmpty()) {
                throw IllegalStateException("Non empty set was empty")
            } else {
                NonEmptySet(this)
            }

        public fun <T> Set<T>.toNonEmptySet(): NonEmptySet<T>? = if (isEmpty()) null else NonEmptySet(this)
    }
}