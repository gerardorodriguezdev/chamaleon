package io.github.gerardorodriguezdev.chamaleon.core.safeModels

import io.github.gerardorodriguezdev.chamaleon.core.serializers.NonEmptyStringSerializer
import kotlinx.serialization.Serializable

@Serializable(with = NonEmptyStringSerializer::class)
@JvmInline
public value class NonEmptyString private constructor(public val value: String) {

    init {
        if (value.isEmpty()) throw IllegalArgumentException("NonEmptyString was empty")
    }

    public fun append(input: String): NonEmptyString = NonEmptyString(value + input)
    public fun removeSuffix(suffix: String): NonEmptyString? = value.removeSuffix(suffix).toNonEmptyString()
    override fun toString(): String = value

    public companion object {
        public fun String.toNonEmptyString(): NonEmptyString? = if (isEmpty()) null else NonEmptyString(this)
        public fun ExistingFile.toNonEmptyString(): NonEmptyString = NonEmptyString(name.value)
        public fun String.toUnsafeNonEmptyString(): NonEmptyString = NonEmptyString(this)
    }
}