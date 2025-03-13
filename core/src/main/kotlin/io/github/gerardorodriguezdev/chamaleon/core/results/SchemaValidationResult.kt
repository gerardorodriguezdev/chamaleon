package io.github.gerardorodriguezdev.chamaleon.core.results

import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptySet

public sealed interface SchemaValidationResult {
    public fun schema(): Schema? = (this as? Success)?.schema

    public data class Success(val schema: Schema) : SchemaValidationResult
    public sealed interface Failure : SchemaValidationResult {
        public data class UnsupportedPlatformTypesOnPropertyDefinition(
            val globalSupportedPlatformTypes: NonEmptySet<PlatformType>,
            val propertyDefinition: PropertyDefinition,
            val unsupportedPlatformTypes: NonEmptySet<PlatformType>,
        ) : Failure
    }
}