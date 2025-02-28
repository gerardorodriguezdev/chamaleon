package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptySetSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.VisibleForTesting

@Serializable(with = NonEmptySetSerializer::class)
public class NonEmptySet<T> private constructor(public val value: Set<T>) : Set<T> by value {

    public companion object {
        @VisibleForTesting
        internal fun <T> unsafe(input: Set<T>): NonEmptySet<T> = NonEmptySet(input)

        public fun <T> of(input: Set<T>): NonEmptySet<T>? = if (input.isEmpty()) null else NonEmptySet(input)
    }
}