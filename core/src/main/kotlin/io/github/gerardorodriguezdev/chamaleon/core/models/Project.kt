package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeCollections.*

public class Project private constructor(
    public val environmentsDirectory: ExistingDirectory,
    public val schema: Schema,
    public val properties: Properties,
    public val environments: NonEmptyKeyStore<String, Environment>? = null,
) {

    public fun propertiesValidFile(): ValidFile =
        requireNotNull(EnvironmentsProcessor.propertiesValidFile(environmentsDirectory))

    public fun schemaExistingFile(): ExistingFile =
        requireNotNull(EnvironmentsProcessor.schemaExistingFile(environmentsDirectory))

    public fun addEnvironment(newEnvironments: NonEmptyKeyStore<String, Environment>): Project? {
        val newEnvironments = environments?.addValues(newEnvironments)

        val projectValidationResult = of(
            environmentsDirectory = environmentsDirectory,
            schema = schema,
            properties = properties,
            environments = newEnvironments,
        )

        return when (projectValidationResult) {
            is Success -> projectValidationResult.project
            is Failure -> null
        }
    }

    public fun updateProperties(newSelectedEnvironmentName: NonEmptyString): Project? {
        if (environments?.contains(newSelectedEnvironmentName.value) == false) return null

        return Project(
            environmentsDirectory = environmentsDirectory,
            schema = schema,
            properties = Properties(
                selectedEnvironmentName = newSelectedEnvironmentName,
            ),
            environments = environments,
        )
    }

    public companion object {
        public fun of(
            environmentsDirectory: ExistingDirectory,
            schema: Schema,
            properties: Properties,
            environments: NonEmptyKeyStore<String, Environment>? = null,
        ): ProjectValidationResult {
            val isSelectedEnvironmentOnEnvironmentsResult = properties.isSelectedEnvironmentOnEnvironmentsOrFailure(
                environmentsDirectoryPath = environmentsDirectory.directory.path,
                environments = environments
            )
            if (isSelectedEnvironmentOnEnvironmentsResult != null) return isSelectedEnvironmentOnEnvironmentsResult

            val areEnvironmentsValid = schema.areEnvironmentsValidOrFailure(
                environmentsDirectoryPath = environmentsDirectory.directory.path,
                environments = environments,
            )
            if (areEnvironmentsValid != null) return areEnvironmentsValid

            return Success(
                Project(
                    environmentsDirectory = environmentsDirectory,
                    schema = schema,
                    properties = properties,
                    environments = environments,
                )
            )
        }

        private fun Properties.isSelectedEnvironmentOnEnvironmentsOrFailure(
            environmentsDirectoryPath: String,
            environments: NonEmptyKeyStore<String, Environment>?,
        ): Failure? =
            when {
                selectedEnvironmentName == null -> null
                environments == null -> Failure.SelectedEnvironmentNotFound(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    selectedEnvironmentName = selectedEnvironmentName.value,
                    environmentNames = "",
                )

                !environments.contains(selectedEnvironmentName.value) -> {
                    Failure.SelectedEnvironmentNotFound(
                        environmentsDirectoryPath = environmentsDirectoryPath,
                        selectedEnvironmentName = selectedEnvironmentName.value,
                        environmentNames = environments.values.joinToString { environment -> environment.name.value }
                    )
                }

                else -> null
            }

        internal fun Schema.areEnvironmentsValidOrFailure(
            environmentsDirectoryPath: String,
            environments: NonEmptyKeyStore<String, Environment>?,
        ): Failure? =
            environments?.values?.firstNotNullOfOrNull { environment ->
                val context = Context(
                    schema = this,
                    environment = environment,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
                context.isEnvironmentValidOrFailure()
            }

        private fun Context.isEnvironmentValidOrFailure(): Failure? {
            val environmentContainsAllPlatformsResult = environmentContainsAllPlatformsOrFailure()
            if (environmentContainsAllPlatformsResult is Failure) return environmentContainsAllPlatformsResult

            return environment.platforms.values.firstNotNullOfOrNull { platform ->
                isPlatformValidOrFailure(platform)
            }
        }

        private fun Context.environmentContainsAllPlatformsOrFailure(): Failure? {
            val platformTypes = environment.platformTypes()
            return if (schema.globalSupportedPlatformTypes != platformTypes) {
                Failure.EnvironmentMissingPlatforms(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    environmentName = environment.name.value,
                    schemaPlatformTypes = schema.globalSupportedPlatformTypes,
                    environmentPlatformTypes = platformTypes,
                )
            } else {
                null
            }
        }

        private fun Context.isPlatformValidOrFailure(platform: Platform): Failure? {
            val propertyDefinitionsForPlatform = propertyDefinitionsForPlatform(platform)

            val platformContainsAllPropertiesResult =
                platformContainsAllPropertiesOrFailure(
                    propertyDefinitionsForPlatform = propertyDefinitionsForPlatform,
                    platform = platform,
                )
            if (platformContainsAllPropertiesResult is Failure) return platformContainsAllPropertiesResult

            return platform.properties.values.firstNotNullOfOrNull { property ->
                isPropertyValidOrFailure(
                    propertyDefinitionsForPlatform = propertyDefinitionsForPlatform,
                    property = property,
                    platformType = platform.platformType,
                )
            }
        }

        private fun Context.isPropertyValidOrFailure(
            propertyDefinitionsForPlatform: Map<String, PropertyDefinition>,
            property: Property,
            platformType: PlatformType,
        ): Failure? {
            val propertyDefinition = propertyDefinitionsForPlatform.propertyDefinition(property)

            return isPropertyTypeValidOrFailure(
                propertyDefinition = propertyDefinition,
                property = property,
                platformType = platformType,
            )
        }

        private fun Environment.platformTypes(): Set<PlatformType> = platforms.keys

        private fun Context.propertyDefinitionsForPlatform(platform: Platform): Map<String, PropertyDefinition> =
            schema.propertyDefinitions.values
                .filter { propertyDefinition ->
                    val platformType = platform.platformType
                    if (propertyDefinition.supportedPlatformTypes == null) {
                        schema.globalSupportedPlatformTypes.contains(platformType)
                    } else {
                        propertyDefinition.supportedPlatformTypes.contains(platformType)
                    }
                }
                .associateBy { propertyDefinition -> propertyDefinition.name.value }

        private fun Context.platformContainsAllPropertiesOrFailure(
            propertyDefinitionsForPlatform: Map<String, PropertyDefinition>,
            platform: Platform,
        ): Failure? {
            val platformPropertiesNames = platform.properties.keys
            val propertyDefinitionsNames = propertyDefinitionsForPlatform.keys
            val platformContainsAllProperties = platformPropertiesNames == propertyDefinitionsNames

            return if (!platformContainsAllProperties) {
                Failure.PlatformMissingProperties(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    environmentName = environment.name.value,
                    platformType = platform.platformType,
                    schemaPropertyDefinitions = schema.propertyDefinitions.values.toSet(),
                    platformProperties = platform.properties.values.toSet(),
                )
            } else {
                null
            }
        }

        private fun Map<String, PropertyDefinition>.propertyDefinition(property: Property): PropertyDefinition =
            getValue(property.name.value)

        private fun Context.isPropertyTypeValidOrFailure(
            propertyDefinition: PropertyDefinition,
            property: Property,
            platformType: PlatformType,
        ): Failure? {
            return when (property.value) {
                null -> isPropertyValueNullableOrFailure(
                    propertyDefinition = propertyDefinition,
                    propertyName = property.name.value,
                    platformType = platformType,
                )

                else -> isPropertyTypeValidOrFailure(
                    propertyName = property.name.value,
                    propertyValue = property.value,
                    propertyDefinition = propertyDefinition,
                    platformType = platformType,
                )
            }
        }

        private fun Context.isPropertyValueNullableOrFailure(
            propertyDefinition: PropertyDefinition,
            propertyName: String,
            platformType: PlatformType,
        ): Failure? =
            if (!propertyDefinition.nullable) {
                Failure.NullPropertyNotNullable(
                    propertyName = propertyName,
                    platformType = platformType,
                    environmentName = environment.name.value,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            } else {
                null
            }

        private fun Context.isPropertyTypeValidOrFailure(
            propertyName: String,
            propertyValue: PropertyValue,
            propertyDefinition: PropertyDefinition,
            platformType: PlatformType,
        ): Failure? {
            val propertyType = propertyValue.toPropertyType()

            return if (propertyDefinition.propertyType != propertyType) {
                Failure.PropertyTypeNotEqualToPropertyDefinition(
                    propertyName = propertyName,
                    platformType = platformType,
                    environmentName = environment.name.value,
                    propertyType = propertyType,
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            } else {
                null
            }
        }

        private fun PropertyValue.toPropertyType(): PropertyType =
            when (this) {
                is StringProperty -> PropertyType.STRING
                is BooleanProperty -> PropertyType.BOOLEAN
            }

        private class Context(
            val schema: Schema,
            val environment: Environment,
            val environmentsDirectoryPath: String,
        )
    }
}