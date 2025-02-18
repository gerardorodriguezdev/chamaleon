package io.github.gerardorodriguezdev.chamaleon.core

import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENT_FILE_SUFFIX
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.PROPERTIES_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.entities.Environment
import io.github.gerardorodriguezdev.chamaleon.core.entities.Schema
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.*
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult.Failure.*
import io.github.gerardorodriguezdev.chamaleon.core.entities.results.EnvironmentsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.models.Result
import io.github.gerardorodriguezdev.chamaleon.core.models.Result.Companion.toFailure
import io.github.gerardorodriguezdev.chamaleon.core.models.Result.Companion.toSuccess
import io.github.gerardorodriguezdev.chamaleon.core.parsers.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File

public interface EnvironmentsProcessor {
    public suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult
    public suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult>

    public fun addOrUpdateSelectedEnvironment(
        environmentsDirectory: File,
        newSelectedEnvironment: String?,
    ): AddOrUpdateSelectedEnvironmentResult

    public fun addEnvironments(environmentsDirectory: File, environments: Set<Environment>): AddEnvironmentsResult

    public fun addSchema(schemaFile: File, newSchema: Schema): AddSchemaResult

    public companion object {
        public const val SCHEMA_FILE: String = "template.chamaleon.json"
        public const val PROPERTIES_FILE: String = "properties.chamaleon.json"
        internal const val ENVIRONMENT_FILE_SUFFIX: String = ".environment.chamaleon.json"
        public const val ENVIRONMENTS_DIRECTORY_NAME: String = "environments"

        public fun environmentFileName(environmentName: String): String =
            "$environmentName$ENVIRONMENT_FILE_SUFFIX"

        public fun create(): EnvironmentsProcessor = DefaultEnvironmentsProcessor()
    }
}

@Suppress("TooManyFunctions")
internal class DefaultEnvironmentsProcessor(
    val schemaParser: SchemaParser = DefaultSchemaParser(),
    val environmentsParser: EnvironmentsParser = DefaultEnvironmentsParser(
        environmentFileMatcher = environmentFileMatcher,
        environmentNameExtractor = environmentNameExtractor,
        environmentFileNameExtractor = environmentFileNameExtractor,
    ),
    val propertiesParser: PropertiesParser = DefaultPropertiesParser(),
) : EnvironmentsProcessor {

    override suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult =
        coroutineScope {
            if (!environmentsDirectory.exists()) {
                return@coroutineScope EnvironmentsDirectoryNotFound(environmentsDirectory.path)
            }

            val filesParserResult = parseFiles(environmentsDirectory)
            if (filesParserResult.isFailure()) {
                return@coroutineScope filesParserResult.failureValue()
            }

            val (schema, environments, selectedEnvironmentName) = filesParserResult.successValue()
            val environmentsVerificationResult = verifyEnvironments(schema, environments, selectedEnvironmentName)
            if (environmentsVerificationResult is Failure) return@coroutineScope environmentsVerificationResult

            return@coroutineScope Success(
                environmentsDirectoryPath = environmentsDirectory.path,
                selectedEnvironmentName = selectedEnvironmentName,
                environments = environments,
                schema = schema,
            )
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

    override fun addOrUpdateSelectedEnvironment(
        environmentsDirectory: File,
        newSelectedEnvironment: String?
    ): AddOrUpdateSelectedEnvironmentResult =
        propertiesParser.addOrUpdateSelectedEnvironment(
            propertiesFile = File(environmentsDirectory, PROPERTIES_FILE),
            newSelectedEnvironment = newSelectedEnvironment,
        )

    override fun addEnvironments(environmentsDirectory: File, environments: Set<Environment>): AddEnvironmentsResult =
        environmentsParser.addEnvironments(environmentsDirectory, environments)

    override fun addSchema(schemaFile: File, newSchema: Schema): AddSchemaResult =
        schemaParser.addSchema(schemaFile, newSchema)

    @Suppress("ReturnCount")
    private suspend fun parseFiles(environmentsDirectory: File): Result<FilesParserResult, Failure> =
        coroutineScope {
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

            if (schemaParserResult.isFailure()) {
                return@coroutineScope schemaParserResult.failureValue().toFailure()
            }
            if (environmentsParserResult.isFailure()) {
                return@coroutineScope environmentsParserResult.failureValue().toFailure()
            }
            if (propertiesParserResult.isFailure()) {
                return@coroutineScope propertiesParserResult.failureValue().toFailure()
            }

            return@coroutineScope FilesParserResult(
                schema = schemaParserResult.successValue(),
                environments = environmentsParserResult.successValue(),
                selectedEnvironmentName = propertiesParserResult.successValue(),
            ).toSuccess()
        }

    private fun SchemaParserResult.schemaOrFailure(): Result<Schema, Failure> =
        when (this) {
            is SchemaParserResult.Success -> schema.toSuccess()
            is SchemaParserResult.Failure -> SchemaParsingError(this).toFailure()
        }

    private fun EnvironmentsParserResult.environmentsOrFailure(): Result<Set<Environment>, Failure> =
        when (this) {
            is EnvironmentsParserResult.Success -> environments.toSuccess()
            is EnvironmentsParserResult.Failure -> EnvironmentsParsingError(this).toFailure()
        }

    private fun PropertiesParserResult.selectedEnvironmentNameOrFailure(): Result<String?, Failure> =
        when (this) {
            is PropertiesParserResult.Success -> selectedEnvironmentName.toSuccess()
            is PropertiesParserResult.Failure -> PropertiesParsingError(this).toFailure()
        }

    @Suppress("ReturnCount")
    private suspend fun verifyEnvironments(
        schema: Schema,
        environments: Set<Environment>,
        selectedEnvironmentName: String?,
    ): Failure? =
        coroutineScope {
            val schemaVerification = async { schema.verifyEnvironments(environments) }
            val selectedEnvironmentNameVerification =
                async { selectedEnvironmentName?.verifyEnvironments(environments) }

            val schemaVerificationResult = schemaVerification.await()
            val selectedEnvironmentVerificationResult = selectedEnvironmentNameVerification.await()

            if (schemaVerificationResult is Failure) return@coroutineScope schemaVerificationResult
            if (selectedEnvironmentVerificationResult is Failure) {
                return@coroutineScope selectedEnvironmentVerificationResult
            }

            return@coroutineScope null
        }

    private fun Schema.verifyEnvironments(environments: Set<Environment>): Failure? {
        val environmentsValidationResult = environmentsValidationResults(environments)
        val failureEnvironmentValidationResult =
            environmentsValidationResult.filterIsInstance<Schema.EnvironmentsValidationResult.Failure>()
        return failureEnvironmentValidationResult.firstOrNull().toFailure()
    }

    private fun Schema.EnvironmentsValidationResult.Failure?.toFailure(): Failure? =
        when (this) {
            null -> null
            is Schema.EnvironmentsValidationResult.Failure.NullPropertyNotNullableOnSchema ->
                NullPropertyNotNullableOnSchema(
                    propertyName = propertyName,
                    platformType = platformType,
                    environmentName = environmentName,
                )

            is Schema.EnvironmentsValidationResult.Failure.PlatformsNotEqualToSchema ->
                PlatformsNotEqualToSchema(environmentName = environmentName)

            is Schema.EnvironmentsValidationResult.Failure.PropertiesNotEqualToSchema ->
                PropertiesNotEqualToSchema(
                    environmentName = environmentName,
                    platformType = platformType,
                )

            is Schema.EnvironmentsValidationResult.Failure.PropertyTypeNotMatchSchema ->
                PropertyTypeNotMatchSchema(
                    environmentName = environmentName,
                    platformType = platformType,
                    propertyName = propertyName,
                    propertyType = propertyType,
                )
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

    private fun File.environmentsDirectoriesPaths(): List<String> =
        walkTopDown()
            .filter { file -> file.isEnvironmentsDirectory }
            .map { environmentFile -> environmentFile.path }
            .toList()

    private val File.isEnvironmentsDirectory: Boolean get() = isDirectory && name == ENVIRONMENTS_DIRECTORY_NAME

    private data class FilesParserResult(
        val schema: Schema,
        val environments: Set<Environment>,
        val selectedEnvironmentName: String?,
    )

    internal companion object {
        val environmentFileMatcher: (environmentFile: File) -> Boolean =
            { environmentFile: File ->
                environmentFile.name != ENVIRONMENT_FILE_SUFFIX && environmentFile.name.endsWith(
                    ENVIRONMENT_FILE_SUFFIX
                )
            }
        val environmentNameExtractor: (environmentFile: File) -> String =
            { environmentFile: File -> environmentFile.name.removeSuffix(ENVIRONMENT_FILE_SUFFIX) }
        val environmentFileNameExtractor: (String) -> String =
            { environmentName -> EnvironmentsProcessor.environmentFileName(environmentName) }
    }
}