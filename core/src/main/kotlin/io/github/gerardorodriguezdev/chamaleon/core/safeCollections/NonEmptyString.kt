package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptyStringSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.VisibleForTesting
import java.io.File

@Serializable(with = NonEmptyStringSerializer::class)
@JvmInline
public value class NonEmptyString private constructor(public val value: String) {
    public fun append(input: String): NonEmptyString = NonEmptyString(value + input)
    public fun removeSuffix(suffix: String): NonEmptyString = NonEmptyString(value.removeSuffix(suffix))

    public companion object {
        @VisibleForTesting
        internal fun unsafe(input: String): NonEmptyString = NonEmptyString(input)

        public fun of(input: String): NonEmptyString? = if (input.isEmpty()) null else NonEmptyString(input)
        public fun of(file: File): NonEmptyString = NonEmptyString(file.name)
    }
}