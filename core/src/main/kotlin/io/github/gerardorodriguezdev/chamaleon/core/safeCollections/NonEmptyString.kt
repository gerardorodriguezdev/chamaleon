package io.github.gerardorodriguezdev.chamaleon.core.safeCollections

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptyStringSerializer
import kotlinx.serialization.Serializable

@Serializable(with = NonEmptyStringSerializer::class)
@JvmInline
public value class NonEmptyString private constructor(public val value: String) {
    public fun append(input: String): NonEmptyString = NonEmptyString(value + input)
    public fun removeSuffix(suffix: String): NonEmptyString = NonEmptyString(value.removeSuffix(suffix))

    override fun toString(): String {
        return value
    }

    public companion object {
        public fun String.toUnsafeNonEmptyString(): NonEmptyString =
            if (isEmpty()) {
                throw IllegalStateException("NonEmptyString was empty")
            } else NonEmptyString(this)

        public fun String.toNonEmptyString(): NonEmptyString? = if (isEmpty()) null else NonEmptyString(this)

        public fun ExistingFile.toNonEmptyString(): NonEmptyString = NonEmptyString(name.value)
    }
}