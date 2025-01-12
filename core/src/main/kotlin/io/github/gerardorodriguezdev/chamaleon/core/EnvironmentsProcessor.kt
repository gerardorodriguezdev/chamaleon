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
import io.github.gerardorodriguezdev.chamaleon.core.models.Result.Companion.toFailure
import io.github.gerardorodriguezdev.chamaleon.core.models.Result.Companion.toSuccess
import io.github.gerardorodriguezdev.chamaleon.core.models.isFailure
import io.github.gerardorodriguezdev.chamaleon.core.parsers.*
import io.github.gerardorodriguezdev.chamaleon.core.parsers.EnvironmentsParser.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.PropertiesParser.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.parsers.SchemaParser.SchemaParserResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File

public interface EnvironmentsProcessor {
    public suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult
    public suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult>
    public fun updateSelectedEnvironment(environmentsDirectory: File, newSelectedEnvironment: String?): Boolean

    public sealed interface EnvironmentsProcessorResult {
        public data class Success(
            val environmentsDirectoryPath: String,
            val selectedEnvironmentName: String? = null,
            val environments: Set<Environment>,
        ) : EnvironmentsProcessorResult

        public sealed interface Failure : EnvironmentsProcessorResult {
            public data class SchemaFileNotFound(val environmentsDirectoryPath: String) : Failure
            public data class SchemaFileIsEmpty(val environmentsDirectoryPath: String) : Failure
            public data class SchemaSerialization(val throwable: Throwable) : Failure
            public data class EnvironmentsSerialization(val throwable: Throwable) : Failure
            public data class PropertiesSerialization(val throwable: Throwable) : Failure
            public data class PlatformsNotEqualToSchema(val environmentName: String) : Failure
            public data class PropertiesNotEqualToSchema(
                val platformType: PlatformType,
                val environmentName: String
            ) : Failure

            public data class PropertyTypeNotMatchSchema(
                val propertyName: String,
                val platformType: PlatformType,
                val environmentName: String,
                val propertyType: PropertyType,
            ) : Failure

            public data class NullPropertyNotNullableOnSchema(
                val propertyName: String,
                val platformType: PlatformType,
                val environmentName: String,
            ) : Failure

            public data class SelectedEnvironmentInvalid(
                val selectedEnvironmentName: String,
                val environmentNames: String
            ) : Failure
        }
    }

    public companion object {
        public const val SCHEMA_FILE: String = "cha.json"
        public const val PROPERTIES_FILE: String = "properties.chamaleon.json"
        public const val ENVIRONMENTS_DIRECTORY_NAME: String = "environments"

        public fun create(): EnvironmentsProcessor = DefaultEnvironmentsProcessor()
    }
}

@Suppress("TooManyFunctions")
internal class DefaultEnvironmentsProcessor(
    val schemaParser: SchemaParser = DefaultSchemaParser(),
    val environmentsParser: EnvironmentsParser = DefaultEnvironmentsParser(),
    val propertiesParser: PropertiesParser = DefaultPropertiesParser(),
) : EnvironmentsProcessor {

    override suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult =
        coroutineScope {
            val filesParserResult = parseFiles(environmentsDirectory)
            if (filesParserResult.isFailure()) return@coroutineScope filesParserResult.value

            val (schema, environments, selectedEnvironmentName) = filesParserResult.value
            val environmentsVerificationResult = verifyEnvironments(schema, environments, selectedEnvironmentName)
            if (environmentsVerificationResult is Failure) return@coroutineScope environmentsVerificationResult

            return@coroutineScope Success(
                environmentsDirectoryPath = environmentsDirectory.absolutePath,
                selectedEnvironmentName = selectedEnvironmentName,
                environments = environments,
            )
        }

    @Suppress("ReturnCount")
    private suspend fun CoroutineScope.parseFiles(environmentsDirectory: File): Result<FilesParserResult, Failure> {
        val schemaParsing = async {
            val schemaFile = File(environmentsDirectory, SCHEMA_FILE)
            val schemaParserResult = schemaParser.schemaParserResult(schemaFile)
            schemaParserResult.schemaOrFailure()
        }

        val environmentsParsing = async {
            val environmentsParserResult = environmentsParser.environmentsParserResult(environmentsDirectory)
            environmentsParserResult.environmentsOrFailure()
        }

        val propertiesParsing = async {
            val propertiesFile = File(environmentsDirectory, PROPERTIES_FILE)
            val propertiesParserResult = propertiesParser.propertiesParserResult(propertiesFile)
            propertiesParserResult.selectedEnvironmentNameOrFailure()
        }

        val schemaParserResult = schemaParsing.await()
        val environmentsParserResult = environmentsParsing.await()
        val propertiesParserResult = propertiesParsing.await()

        if (schemaParserResult.isFailure()) return schemaParserResult.value.toFailure()
        if (environmentsParserResult.isFailure()) return environmentsParserResult.value.toFailure()
        if (propertiesParserResult.isFailure()) return propertiesParserResult.value.toFailure()

        return FilesParserResult(
            schema = schemaParserResult.value,
            environments = environmentsParserResult.value,
            selectedEnvironmentName = propertiesParserResult.value,
        ).toSuccess()
    }

    private fun SchemaParserResult.schemaOrFailure(): Result<Schema, Failure> =
        when (this) {
            is SchemaParserResult.Success -> schema.toSuccess()
            is SchemaParserResult.Failure.FileNotFound -> SchemaFileNotFound(path).toFailure()
            is SchemaParserResult.Failure.FileIsEmpty -> SchemaFileIsEmpty(path).toFailure()
            is SchemaParserResult.Failure.Serialization -> SchemaSerialization(throwable).toFailure()
        }

    private fun EnvironmentsParserResult.environmentsOrFailure(): Result<Set<Environment>, Failure> =
        when (this) {
            is EnvironmentsParserResult.Success -> environments.toSuccess()
            is EnvironmentsParserResult.Failure.Serialization -> EnvironmentsSerialization(throwable).toFailure()
        }

    private fun PropertiesParserResult.selectedEnvironmentNameOrFailure(): Result<String?, Failure> =
        when (this) {
            is PropertiesParserResult.Success -> selectedEnvironmentName.toSuccess()
            is PropertiesParserResult.Failure -> PropertiesSerialization(throwable).toFailure()
        }

    @Suppress("ReturnCount")
    private suspend fun CoroutineScope.verifyEnvironments(
        schema: Schema,
        environments: Set<Environment>,
        selectedEnvironmentName: String?,
    ): Failure? {
        val schemaVerification = async { schema.verifyEnvironments(environments) }
        val selectedEnvironmentNameVerification =
            async { selectedEnvironmentName?.verifyEnvironments(environments) }

        val schemaVerificationResult = schemaVerification.await()
        val selectedEnvironmentVerificationResult = selectedEnvironmentNameVerification.await()

        if (schemaVerificationResult is Failure) return schemaVerificationResult
        if (selectedEnvironmentVerificationResult is Failure) return selectedEnvironmentVerificationResult

        return null
    }

    @Suppress("NestedBlockDepth", "ReturnCount")
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

    private fun String.verifyEnvironments(environments: Set<Environment>): Failure? =
        if (!environments.any { environment -> environment.name == this }) {
            SelectedEnvironmentInvalid(
                selectedEnvironmentName = this,
                environmentNames = environments.joinToString { environment -> environment.name }
            )
        } else {
            null
        }

    override suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult> =
        coroutineScope {
            val environmentsDirectoriesPaths = rootDirectory.environmentsDirectoriesPaths()

            environmentsDirectoriesPaths
                .map { environmentsDirectoryPath ->
                    async {
                        val environmentsDirectory = File(environmentsDirectoryPath)
                        val environmentsProcessorResult = process(environmentsDirectory)
                        environmentsProcessorResult
                    }
                }
                .awaitAll()
        }

    override fun updateSelectedEnvironment(
        environmentsDirectory: File,
        newSelectedEnvironment: String?
    ): Boolean =
        propertiesParser.updateSelectedEnvironment(
            propertiesFile = File(environmentsDirectory, PROPERTIES_FILE),
            newSelectedEnvironment = newSelectedEnvironment,
        )

    private fun Schema.verifyEnvironmentContainsAllPlatforms(environment: Environment): Failure? {
        val platformTypes = environment.platforms.map { platform -> platform.platformType }

        return if (supportedPlatforms.size != platformTypes.size || !supportedPlatforms.containsAll(platformTypes)) {
            PlatformsNotEqualToSchema(environment.name)
        } else {
            null
        }
    }

    private fun Schema.verifyPlatformContainsAllProperties(platform: Platform, environmentName: String): Failure? {
        val propertyDefinitionNames = propertyDefinitions.map { propertyDefinition -> propertyDefinition.name }
        val propertyNames = platform.properties.map { property -> property.name }

        return if (
            propertyDefinitionNames.size != propertyNames.size || !propertyDefinitionNames.containsAll(propertyNames)
        ) {
            PropertiesNotEqualToSchema(platform.platformType, environmentName)
        } else {
            null
        }
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
        } else {
            null
        }

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
        } else {
            null
        }
    }

    private fun PropertyValue.toPropertyType(): PropertyType =
        when (this) {
            is StringProperty -> PropertyType.STRING
            is BooleanProperty -> PropertyType.BOOLEAN
        }

    private fun File.environmentsDirectoriesPaths(): List<String> =
        walkTopDown()
            .filter { file -> file.isEnvironmentsDirectory }
            .map { file -> file.absolutePath }
            .toList()

    private val File.isEnvironmentsDirectory: Boolean get() = isDirectory && name == ENVIRONMENTS_DIRECTORY_NAME

    private data class FilesParserResult(
        val schema: Schema,
        val environments: Set<Environment>,
        val selectedEnvironmentName: String?,
    )
}