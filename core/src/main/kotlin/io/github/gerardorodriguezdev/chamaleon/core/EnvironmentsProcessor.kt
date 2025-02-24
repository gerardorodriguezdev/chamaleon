package io.github.gerardorodriguezdev.chamaleon.core

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.PROPERTIES_FILE
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.SCHEMA_FILE
import io.github.gerardorodriguezdev.chamaleon.core.extractors.DefaultEnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.extractors.DefaultEnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.matchers.DefaultEnvironmentFileNameMatcher
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.parsers.*
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure.*
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.results.PropertiesParserResult
import io.github.gerardorodriguezdev.chamaleon.core.results.SchemaParserResult
import io.github.gerardorodriguezdev.chamaleon.core.validators.areEnvironmentsValidOrFailure
import io.github.gerardorodriguezdev.chamaleon.core.validators.isSelectedEnvironmentValidOrFailure
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File

public interface EnvironmentsProcessor {
    public suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult
    public suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult>

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

internal class DefaultEnvironmentsProcessor(
    val schemaParser: SchemaParser = DefaultSchemaParser(),
    val environmentsParser: EnvironmentsParser = DefaultEnvironmentsParser(
        environmentFileMatcher = DefaultEnvironmentFileNameMatcher(),
        environmentNameExtractor = DefaultEnvironmentNameExtractor(),
        environmentFileNameExtractor = DefaultEnvironmentFileNameExtractor(),
    ),
    val propertiesParser: PropertiesParser = DefaultPropertiesParser(),
) : EnvironmentsProcessor {

    override suspend fun process(environmentsDirectory: File): EnvironmentsProcessorResult =
        coroutineScope {
            val environmentsDirectoryPath = environmentsDirectory.path
            if (!environmentsDirectory.isDirectory) {
                return@coroutineScope InvalidEnvironmentsDirectory(environmentsDirectoryPath)
            }
            if (!environmentsDirectory.exists()) {
                return@coroutineScope EnvironmentsDirectoryNotFound(environmentsDirectoryPath)
            }

            parseFiles(environmentsDirectory)
                .fold(
                    ifLeft = { failure -> failure },
                    ifRight = { success -> success }
                )
        }

    private suspend fun parseFiles(environmentsDirectory: File): Either<Failure, Success> =
        coroutineScope {
            either {
                val environmentsDirectoryPath = environmentsDirectory.path

                val schemaParsing = async {
                    val schemaFile = File(environmentsDirectory, SCHEMA_FILE)
                    val schemaParserResult = schemaParser.schemaParserResult(schemaFile)
                    schemaParserResult.schemaOrFailure(environmentsDirectoryPath)
                }

                val environmentsParsing = async {
                    val environmentsParserResult = environmentsParser.environmentsParserResult(environmentsDirectory)
                    environmentsParserResult.environmentsOrFailure()
                }

                val propertiesParsing = async {
                    val propertiesFile = File(environmentsDirectory, PROPERTIES_FILE)
                    val propertiesParserResult = propertiesParser.propertiesParserResult(propertiesFile)
                    propertiesParserResult.selectedEnvironmentNameOrFailure(environmentsDirectoryPath)
                }

                environmentsProcessorResult(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    schema = schemaParsing.await().bind(),
                    environments = environmentsParsing.await().bind(),
                    selectedEnvironmentName = propertiesParsing.await().bind(),
                ).bind()
            }
        }


    private fun SchemaParserResult.schemaOrFailure(
        environmentsDirectoryPath: String,
    ): Either<Failure, Schema> =
        when (this) {
            is SchemaParserResult.Failure ->
                SchemaParsingError(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    schemaParsingError = this
                ).left()

            is SchemaParserResult.Success -> schema.right()
        }

    private fun EnvironmentsParserResult.environmentsOrFailure(): Either<Failure, Set<Environment>> =
        when (this) {
            is EnvironmentsParserResult.Failure -> EnvironmentsParsingError(this).left()
            is EnvironmentsParserResult.Success -> environments.right()
        }

    private fun PropertiesParserResult.selectedEnvironmentNameOrFailure(environmentsDirectoryPath: String): Either<Failure, String?> =
        when (this) {
            is PropertiesParserResult.Failure ->
                PropertiesParsingError(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    propertiesParsingError = this
                ).left()

            is PropertiesParserResult.Success -> selectedEnvironmentName.right()
        }

    private fun environmentsProcessorResult(
        environmentsDirectoryPath: String,
        schema: Schema,
        environments: Set<Environment>,
        selectedEnvironmentName: String?,
    ): Either<Failure, Success> =
        either {
            val schemaValidation = schema.areEnvironmentsValidOrFailure(
                environmentsDirectoryPath = environmentsDirectoryPath,
                environments = environments
            )
            if (schemaValidation is Failure) raise(schemaValidation)

            val selectedEnvironmentNameValidation =
                selectedEnvironmentName?.isSelectedEnvironmentValidOrFailure(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    environments = environments
                )
            if (selectedEnvironmentNameValidation is Failure) raise(selectedEnvironmentNameValidation)

            Success(
                environmentsDirectoryPath = environmentsDirectoryPath,
                schema = schema,
                environments = environments,
                selectedEnvironmentName = selectedEnvironmentName,
            )
        }

    override suspend fun processRecursively(rootDirectory: File): List<EnvironmentsProcessorResult> =
        coroutineScope {
            if (!rootDirectory.isDirectory) return@coroutineScope emptyList()
            if (!rootDirectory.exists()) return@coroutineScope emptyList()

            rootDirectory
                .environmentsDirectoriesPaths()
                .map { environmentsDirectoryPath ->
                    async {
                        process(environmentsDirectory = File(environmentsDirectoryPath))
                    }
                }
                .awaitAll()
        }

    private fun File.environmentsDirectoriesPaths(): List<String> =
        walkTopDown()
            .filter { file -> file.isEnvironmentsDirectory }
            .map { environmentFile -> environmentFile.path }
            .toList()

    private val File.isEnvironmentsDirectory: Boolean get() = isDirectory && name == ENVIRONMENTS_DIRECTORY_NAME
}