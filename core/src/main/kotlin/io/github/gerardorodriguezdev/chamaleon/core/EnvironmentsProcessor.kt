package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.PROPERTIES_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult.Failure.*
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.EnvironmentsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.entities.*
import io.github.gerardorodriguezdev.chamaleon.core.entities.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.core.models.Result
import io.github.gerardorodriguezdev.chamaleon.core.models.Result.Companion.failure
import io.github.gerardorodriguezdev.chamaleon.core.models.Result.Companion.success
import io.github.gerardorodriguezdev.chamaleon.core.models.isFailure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.*
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import kotlinx.coroutines.*
import java.io.File

public interface EnvironmentsProcessor {
    public suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult
    public suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult>
    public fun updateSelectedEnvironment(environmentsDirectory: File, newSelectedEnvironment: String?): Boolean

    public sealed interface EnvironmentsProcessorResult {
        public data class Success(
            val selectedEnvironmentName: String? = null,
            val environments: Set<Environment>,
        ) : EnvironmentsProcessorResult

        public sealed class Failure(message: String) : EnvironmentsProcessorResult {
            public data class SchemaFileNotFound(val environmentsDirectoryPath: String) :
                Failure("'$SCHEMA_FILE' not found on '$environmentsDirectoryPath'")

            public data class SchemaFileIsEmpty(val environmentsDirectoryPath: String) :
                Failure("'$SCHEMA_FILE' on '$environmentsDirectoryPath' is empty")

            public data class SchemaSerialization(val throwable: Throwable) : Failure(
                "Schema parsing failed with error '${throwable.message}'"
            )

            public data class EnvironmentsSerialization(val throwable: Throwable) : Failure(
                "Environments parsing failed with error '${throwable.message}'"
            )

            public data class PropertiesSerialization(val throwable: Throwable) : Failure(
                "Properties parsing failed with error '${throwable.message}'"
            )

            public data class PlatformsNotEqualToSchema(val environmentName: String) :
                Failure("Platforms of environment '$environmentName' are not equal to schema")

            public data class PropertiesNotEqualToSchema(val platformType: PlatformType, val environmentName: String) :
                Failure(
                    "Properties on platform '$platformType' for environment '$environmentName' are not equal to schema"
                )

            @Suppress("Indentation")
            public data class PropertyTypeNotMatchSchema(
                val propertyName: String,
                val platformType: PlatformType,
                val environmentName: String,
                val propertyType: PropertyType,
            ) : Failure(
                "Value of property '$propertyName' for platform '$platformType' " +
                        "on environment '$environmentName' doesn't match propertyType '$propertyType' on schema"
            )

            @Suppress("Indentation")
            public data class NullPropertyNotNullableOnSchema(
                val propertyName: String,
                val platformType: PlatformType,
                val environmentName: String,
            ) : Failure(
                "Value on property '$propertyName' for platform '$platformType' on environment " +
                        "'$environmentName' was null and is not marked as nullable on schema"
            )

            @Suppress("Indentation")
            public data class SelectedEnvironmentInvalid(
                val selectedEnvironmentName: String,
                val environmentNames: String
            ) : Failure(
                "Selected environment '$selectedEnvironmentName' on '$PROPERTIES_FILE' not present in any environment" +
                        "[$environmentNames]"
            )
        }
    }

    public companion object {
        public const val SCHEMA_FILE: String = "cha.json"
        public const val PROPERTIES_FILE: String = "cha.properties.json"
        public const val ENVIRONMENTS_DIRECTORY_NAME: String = "environments"

        public fun build(): EnvironmentsProcessor = DefaultEnvironmentsProcessor()
    }
}

internal class DefaultEnvironmentsProcessor(
    val schemaParser: SchemaParser = DefaultSchemaParser(),
    val environmentsParser: EnvironmentsParser = DefaultEnvironmentsParser(),
    val propertiesParser: PropertiesParser = DefaultPropertiesParser(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : EnvironmentsProcessor {
    private val scope = CoroutineScope(ioDispatcher)

    //TODO: Refactor
    override suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult =
        scope
            .async {
                val schemaDeferred = async {
                    val schemaFile = File(environmentsDirectory, SCHEMA_FILE)
                    val schemaParsingResult = schemaParser.schemaParserResult(schemaFile)
                    schemaParsingResult.schema()
                }

                val environmentsDeferred = async {
                    val environmentsParserResult = environmentsParser.environmentsParserResult(environmentsDirectory)
                    environmentsParserResult.environments()
                }

                val selectedEnvironmentNameDeferred = async {
                    val propertiesFile = File(environmentsDirectory, PROPERTIES_FILE)
                    val propertiesParserResult = propertiesParser.propertiesParserResult(propertiesFile)
                    propertiesParserResult.selectedEnvironmentName()
                }

                val schema = schemaDeferred.await()
                val environments = environmentsDeferred.await()
                val selectedEnvironmentName = selectedEnvironmentNameDeferred.await()

                if (schema.isFailure()) return@async schema.value
                if (environments.isFailure()) return@async environments.value
                if (selectedEnvironmentName.isFailure()) return@async selectedEnvironmentName.value

                val schemaVerificationDeferred = async { schema.value.verifyEnvironments(environments.value) }
                val selectedEnvironmentVerificationDeferred =
                    async { selectedEnvironmentName.value.verifyEnvironments(environments.value) }

                val schemaVerificationResult = schemaVerificationDeferred.await()
                if (schemaVerificationResult is Failure) return@async schemaVerificationResult

                val selectedEnvironmentVerificationResult = selectedEnvironmentVerificationDeferred.await()
                if (selectedEnvironmentVerificationResult is Failure) return@async selectedEnvironmentVerificationResult

                return@async Success(
                    selectedEnvironmentName = selectedEnvironmentName.value,
                    environments = environments.value,
                )
            }.await()

    //Todo: Test
    override suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult> =
        scope
            .async {
                val environmentsDirectoriesPaths = rootDirectory.environmentsDirectoriesPaths()

                environmentsDirectoriesPaths
                    .map { environmentsDirectoryPath ->
                        async {
                            val environmentsDirectory = File(environmentsDirectoryPath)
                            val environmentsProcessorResult = process(environmentsDirectory)
                            when (environmentsProcessorResult) {
                                is Success -> environmentsProcessorResult
                                is Failure -> null
                            }
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }.await()

    override fun updateSelectedEnvironment(environmentsDirectory: File, newSelectedEnvironment: String?): Boolean =
        propertiesParser.updateSelectedEnvironment(File(environmentsDirectory, PROPERTIES_FILE), newSelectedEnvironment)

    private fun SchemaParserResult.schema(): Result<Schema, Failure> =
        when (this) {
            is SchemaParserResult.Success -> success(schema)
            is SchemaParserResult.Failure.FileNotFound -> failure(SchemaFileNotFound(path))
            is SchemaParserResult.Failure.FileIsEmpty -> failure(SchemaFileIsEmpty(path))
            is SchemaParserResult.Failure.Serialization -> failure(SchemaSerialization(throwable))
        }

    private fun EnvironmentsParserResult.environments(): Result<Set<Environment>, Failure> =
        when (this) {
            is EnvironmentsParserResult.Success -> success(environments)
            is EnvironmentsParserResult.Failure.Serialization -> failure(EnvironmentsSerialization(throwable))
        }

    private fun PropertiesParserResult.selectedEnvironmentName(): Result<String?, Failure> =
        when (this) {
            is PropertiesParserResult.Success -> success(selectedEnvironmentName)
            is PropertiesParserResult.Failure -> failure(PropertiesSerialization(throwable))
        }

    private fun Schema.verifyEnvironments(environments: Set<Environment>): Failure? {
        environments.forEach { environment ->
            val verificationResult = verifyEnvironmentContainsAllPlatforms(environment)
            if (verificationResult is Failure) return verificationResult

            environment.platforms.forEach { platform ->
                val verificationResult = verifyPlatformContainsAllProperties(platform, environment.name)
                if (verificationResult is Failure) return verificationResult

                platform.properties.forEach { property ->
                    val verificationResult =
                        verifyPropertyTypeIsCorrect(property, platform.platformType, environment.name)
                    if (verificationResult is Failure) return verificationResult
                }
            }
        }

        return null
    }

    private fun Schema.verifyEnvironmentContainsAllPlatforms(environment: Environment): Failure? {
        val platformTypes = environment.platforms.map { platform -> platform.platformType }

        return if (supportedPlatforms.size != platformTypes.size || !supportedPlatforms.containsAll(platformTypes)) {
            PlatformsNotEqualToSchema(environment.name)
        } else null
    }

    private fun Schema.verifyPlatformContainsAllProperties(platform: Platform, environmentName: String): Failure? {
        val propertyDefinitionNames = propertyDefinitions.map { propertyDefinition -> propertyDefinition.name }
        val propertyNames = platform.properties.map { property -> property.name }

        return if (
            propertyDefinitionNames.size != propertyNames.size || !propertyDefinitionNames.containsAll(propertyNames)
        ) {
            PropertiesNotEqualToSchema(platform.platformType, environmentName)
        } else null
    }

    private fun Schema.verifyPropertyTypeIsCorrect(
        property: Property,
        platformType: PlatformType,
        environmentName: String,
    ): Failure? {
        val propertyDefinitions =
            propertyDefinitions.first { propertyDefinition -> propertyDefinition.name == property.name }

        return when (property.value) {
            null -> verifyNullPropertyValue(
                propertyDefinition = propertyDefinitions,
                propertyName = property.name,
                platformType = platformType,
                environmentName = environmentName,
            )

            else -> verifyPropertyType(
                propertyName = property.name,
                propertyValue = property.value,
                propertyDefinition = propertyDefinitions,
                platformType = platformType,
                environmentName = environmentName,
            )
        }
    }

    private fun verifyNullPropertyValue(
        propertyDefinition: PropertyDefinition,
        propertyName: String,
        platformType: PlatformType,
        environmentName: String,
    ): Failure? =
        if (!propertyDefinition.nullable) {
            NullPropertyNotNullableOnSchema(
                propertyName = propertyName,
                platformType = platformType,
                environmentName = environmentName,
            )
        } else null

    private fun verifyPropertyType(
        propertyName: String,
        propertyValue: PropertyValue,
        propertyDefinition: PropertyDefinition,
        platformType: PlatformType,
        environmentName: String
    ): Failure? {
        val propertyType = propertyValue.toPropertyType()

        return if (propertyDefinition.propertyType != propertyType) {
            PropertyTypeNotMatchSchema(
                propertyName = propertyName,
                platformType = platformType,
                environmentName = environmentName,
                propertyType = propertyType,
            )
        } else null
    }

    private fun String?.verifyEnvironments(environments: Set<Environment>): Failure? =
        if (this != null) {
            if (!environments.any { environment -> environment.name == this }) {
                SelectedEnvironmentInvalid(
                    selectedEnvironmentName = this,
                    environmentNames = environments.joinToString { environment -> environment.name }
                )
            } else null
        } else null

    private fun PropertyValue.toPropertyType(): PropertyType =
        when (this) {
            is StringProperty -> PropertyType.STRING
            is BooleanProperty -> PropertyType.BOOLEAN
        }

    private fun File.environmentsDirectoriesPaths(): List<String> =
        this
            .walkTopDown()
            .filter { file -> file.isDirectory && file.name == ENVIRONMENTS_DIRECTORY_NAME }
            .map { file -> file.path }
            .toList()
}