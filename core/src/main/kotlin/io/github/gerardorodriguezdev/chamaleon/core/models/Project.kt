package io.github.gerardorodriguezdev.chamaleon.core.models

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectValidationResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString

public class Project private constructor(
    public val environmentsDirectory: ExistingDirectory,
    public val schema: Schema,
    public val properties: Properties,
    public val environments: NonEmptyKeySetStore<String, Environment>? = null,
) {
    public fun selectedEnvironment(): Environment? = environments?.get(properties.selectedEnvironmentName?.value)

    public fun addEnvironments(newEnvironments: NonEmptyKeySetStore<String, Environment>): Project? {
        val newEnvironments = environments?.addValues(newEnvironments)

        val projectValidationResult = projectOf(
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
        public const val SCHEMA_FILE: String = "template.chamaleon.json"
        public const val PROPERTIES_FILE: String = "properties.chamaleon.json"
        public const val ENVIRONMENT_FILE_SUFFIX: String = ".environment.chamaleon.json"
        public const val ENVIRONMENTS_DIRECTORY_NAME: String = "environments"

        public fun environmentFileName(environmentName: NonEmptyString): NonEmptyString =
            environmentName.append(ENVIRONMENT_FILE_SUFFIX)

        public fun ExistingDirectory.propertiesExistingFile(createIfNotPresent: Boolean = false): ExistingFile? =
            existingFile(
                fileName = PROPERTIES_FILE.toUnsafeNonEmptyString(),
                createIfNotPresent = createIfNotPresent,
            )

        public fun ExistingDirectory.schemaExistingFile(createIfNotPresent: Boolean = false): ExistingFile? =
            existingFile(
                fileName = SCHEMA_FILE.toUnsafeNonEmptyString(),
                createIfNotPresent = createIfNotPresent
            )

        public fun String.isEnvironmentFileName(): Boolean =
            this != ENVIRONMENT_FILE_SUFFIX && endsWith(ENVIRONMENT_FILE_SUFFIX)

        public fun String.isEnvironmentsDirectory(): Boolean = this == ENVIRONMENTS_DIRECTORY_NAME

        public fun projectOf(
            environmentsDirectory: ExistingDirectory,
            schema: Schema,
            properties: Properties,
            environments: NonEmptyKeySetStore<String, Environment>? = null,
        ): ProjectValidationResult =
            projectOfEither(
                environmentsDirectory = environmentsDirectory,
                schema = schema,
                properties = properties,
                environments = environments,
            ).fold(
                ifLeft = { it },
                ifRight = { it },
            )

        private fun projectOfEither(
            environmentsDirectory: ExistingDirectory,
            schema: Schema,
            properties: Properties,
            environments: NonEmptyKeySetStore<String, Environment>?,
        ): Either<Failure, Success> =
            either {
                properties.isSelectedEnvironmentOnEnvironments(
                    environmentsDirectoryPath = environmentsDirectory.path.value,
                    environments = environments
                ).bind()

                schema.areEnvironmentsValid(
                    environmentsDirectoryPath = environmentsDirectory.path.value,
                    environments = environments,
                ).bind()

                Success(
                    Project(
                        environmentsDirectory = environmentsDirectory,
                        schema = schema,
                        properties = properties,
                        environments = environments,
                    )
                )
            }

        private fun Properties.isSelectedEnvironmentOnEnvironments(
            environmentsDirectoryPath: String,
            environments: NonEmptyKeySetStore<String, Environment>?,
        ): Either<Failure, Unit> =
            either {
                when {
                    selectedEnvironmentName == null -> Unit
                    environments == null ->
                        raise(
                            Failure.SelectedEnvironmentNotFound(
                                environmentsDirectoryPath = environmentsDirectoryPath,
                                selectedEnvironmentName = selectedEnvironmentName.value,
                                environmentNames = "",
                            )
                        )

                    !environments.contains(selectedEnvironmentName.value) ->
                        raise(
                            Failure.SelectedEnvironmentNotFound(
                                environmentsDirectoryPath = environmentsDirectoryPath,
                                selectedEnvironmentName = selectedEnvironmentName.value,
                                environmentNames = environments.values.joinToString { environment -> environment.name.value }
                            )
                        )

                    else -> Unit
                }
            }

        internal fun Schema.areEnvironmentsValid(
            environmentsDirectoryPath: String,
            environments: NonEmptyKeySetStore<String, Environment>?,
        ): Either<Failure, Unit> =
            either {
                environments?.values?.forEach { environment ->
                    val context = Context(
                        schema = this@areEnvironmentsValid,
                        environment = environment,
                        environmentsDirectoryPath = environmentsDirectoryPath,
                    )
                    context.isEnvironmentValid().bind()
                }
            }

        private fun Context.isEnvironmentValid(): Either<Failure, Unit> =
            either {
                environmentContainsAllPlatforms().bind()

                environment.platforms.values.forEach { platform ->
                    isPlatformValid(platform).bind()
                }
            }

        private fun Context.environmentContainsAllPlatforms(): Either<Failure, Unit> =
            either {
                val platformTypes = environment.platforms.keys
                ensure(schema.globalSupportedPlatformTypes == platformTypes) {
                    Failure.EnvironmentMissingPlatforms(
                        environmentsDirectoryPath = environmentsDirectoryPath,
                        environmentName = environment.name.value,
                        schemaPlatformTypes = schema.globalSupportedPlatformTypes,
                        environmentPlatformTypes = platformTypes,
                    )
                }
            }

        private fun Context.isPlatformValid(platform: Platform): Either<Failure, Unit> =
            either {
                val propertyDefinitionsForPlatform = propertyDefinitionsForPlatform(platform)

                platformContainsAllProperties(
                    propertyDefinitionsForPlatform = propertyDefinitionsForPlatform,
                    platform = platform,
                ).bind()

                platform.properties.values.forEach { property ->
                    isPropertyValid(
                        propertyDefinitionsForPlatform = propertyDefinitionsForPlatform,
                        property = property,
                        platformType = platform.platformType,
                    ).bind()
                }
            }

        private fun Context.isPropertyValid(
            propertyDefinitionsForPlatform: Map<String, PropertyDefinition>,
            property: Property,
            platformType: PlatformType,
        ): Either<Failure, Unit> =
            either {
                val propertyDefinition = propertyDefinitionsForPlatform.propertyDefinition(property)

                isPropertyTypeValid(
                    propertyDefinition = propertyDefinition,
                    property = property,
                    platformType = platformType,
                ).bind()
            }

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

        private fun Context.platformContainsAllProperties(
            propertyDefinitionsForPlatform: Map<String, PropertyDefinition>,
            platform: Platform,
        ): Either<Failure, Unit> =
            either {
                val platformPropertiesNames = platform.properties.keys
                val propertyDefinitionsNames = propertyDefinitionsForPlatform.keys
                val platformContainsAllProperties = platformPropertiesNames == propertyDefinitionsNames

                ensure(platformContainsAllProperties) {
                    Failure.PlatformMissingProperties(
                        environmentsDirectoryPath = environmentsDirectoryPath,
                        environmentName = environment.name.value,
                        platformType = platform.platformType,
                        schemaPropertyDefinitions = schema.propertyDefinitions.values.toSet(),
                        platformProperties = platform.properties.values.toSet(),
                    )
                }
            }

        private fun Map<String, PropertyDefinition>.propertyDefinition(property: Property): PropertyDefinition =
            getValue(property.name.value)

        private fun Context.isPropertyTypeValid(
            propertyDefinition: PropertyDefinition,
            property: Property,
            platformType: PlatformType,
        ): Either<Failure, Unit> =
            when (property.value) {
                null -> isPropertyValueNullable(
                    propertyDefinition = propertyDefinition,
                    propertyName = property.name.value,
                    platformType = platformType,
                )

                else -> isPropertyTypeValid(
                    propertyName = property.name.value,
                    propertyValue = property.value,
                    propertyDefinition = propertyDefinition,
                    platformType = platformType,
                )
            }

        private fun Context.isPropertyValueNullable(
            propertyDefinition: PropertyDefinition,
            propertyName: String,
            platformType: PlatformType,
        ): Either<Failure, Unit> =
            either {
                ensure(propertyDefinition.nullable) {
                    Failure.NullPropertyNotNullable(
                        propertyName = propertyName,
                        platformType = platformType,
                        environmentName = environment.name.value,
                        environmentsDirectoryPath = environmentsDirectoryPath,
                    )
                }
            }

        private fun Context.isPropertyTypeValid(
            propertyName: String,
            propertyValue: PropertyValue,
            propertyDefinition: PropertyDefinition,
            platformType: PlatformType,
        ): Either<Failure, Unit> =
            either {
                val propertyType = propertyValue.toPropertyType()

                ensure(propertyDefinition.propertyType == propertyType) {
                    Failure.PropertyTypeNotEqualToPropertyDefinition(
                        propertyName = propertyName,
                        platformType = platformType,
                        environmentName = environment.name.value,
                        propertyType = propertyType,
                        environmentsDirectoryPath = environmentsDirectoryPath,
                    )
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