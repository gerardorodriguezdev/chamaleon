package io.github.gerardorodriguezdev.chamaleon.core

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.ENVIRONMENTS_DIRECTORY_NAME
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.propertiesExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.EnvironmentsProcessor.Companion.schemaExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.extractors.DefaultEnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.extractors.DefaultEnvironmentNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.generators.DefaultProjectUpdater
import io.github.gerardorodriguezdev.chamaleon.core.generators.ProjectUpdater
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.models.Project.Companion.projectOf
import io.github.gerardorodriguezdev.chamaleon.core.models.Properties
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.parsers.*
import io.github.gerardorodriguezdev.chamaleon.core.results.*
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.EnvironmentsProcessorResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingDirectory
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.ExistingFile
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyKeySetStore
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString.Companion.toUnsafeNonEmptyString
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

public interface EnvironmentsProcessor : ProjectUpdater {
    public suspend fun process(environmentsDirectory: ExistingDirectory): EnvironmentsProcessorResult
    public suspend fun processRecursively(rootDirectory: ExistingDirectory): List<EnvironmentsProcessorResult>

    public companion object {
        public const val SCHEMA_FILE: String = "template.chamaleon.json"
        public const val PROPERTIES_FILE: String = "properties.chamaleon.json"
        internal const val ENVIRONMENT_FILE_SUFFIX: String = ".environment.chamaleon.json"
        public const val ENVIRONMENTS_DIRECTORY_NAME: String = "environments"

        public fun environmentFileName(environmentName: NonEmptyString): NonEmptyString =
            environmentName.append(ENVIRONMENT_FILE_SUFFIX)

        public fun String.isEnvironmentFileName(): Boolean =
            this != ENVIRONMENT_FILE_SUFFIX && endsWith(ENVIRONMENT_FILE_SUFFIX)

        public fun ExistingDirectory.schemaExistingFile(createIfNotPresent: Boolean = false): ExistingFile? =
            existingFile(
                fileName = SCHEMA_FILE.toUnsafeNonEmptyString(),
                createIfNotPresent = createIfNotPresent
            )

        public fun ExistingDirectory.propertiesExistingFile(createIfNotPresent: Boolean = false): ExistingFile? =
            existingFile(
                fileName = PROPERTIES_FILE.toUnsafeNonEmptyString(),
                createIfNotPresent = createIfNotPresent,
            )

        public fun create(): EnvironmentsProcessor = DefaultEnvironmentsProcessor()
    }
}

internal class DefaultEnvironmentsProcessor(
    private val schemaParser: SchemaParser = DefaultSchemaParser,
    private val environmentsParser: EnvironmentsParser = DefaultEnvironmentsParser(
        environmentNameExtractor = DefaultEnvironmentNameExtractor,
    ),
    private val propertiesParser: PropertiesParser = DefaultPropertiesParser,
    private val projectUpdater: ProjectUpdater = DefaultProjectUpdater(
        environmentFileNameExtractor = DefaultEnvironmentFileNameExtractor,
    ),
) : EnvironmentsProcessor,
    ProjectUpdater by projectUpdater {

    override suspend fun process(environmentsDirectory: ExistingDirectory): EnvironmentsProcessorResult =
        parseFiles(environmentsDirectory)
            .fold(
                ifLeft = { it },
                ifRight = { it }
            )

    private suspend fun parseFiles(environmentsDirectory: ExistingDirectory): Either<Failure, Success> =
        coroutineScope {
            either {
                val schemaParsing = async {
                    val schemaFile = environmentsDirectory.schemaExistingFile()
                    ensureNotNull(schemaFile) { Failure.InvalidSchemaFile(environmentsDirectory.path.value) }
                    val schemaParserResult = schemaParser.parse(schemaFile)
                    schemaParserResult.schemaOrFailure(environmentsDirectory.path.value)
                }

                val environmentsParsing = async {
                    val environmentsParserResult = environmentsParser.parse(environmentsDirectory)
                    environmentsParserResult.environmentsOrFailure()
                }

                val propertiesParsing = async {
                    val propertiesFile = environmentsDirectory.propertiesExistingFile()
                    if (propertiesFile == null) {
                        return@async PropertiesParserResult.Success()
                            .propertiesOrFailure(environmentsDirectory.path.value)
                    }

                    val propertiesParserResult = propertiesParser.parse(propertiesFile)
                    propertiesParserResult.propertiesOrFailure(environmentsDirectory.path.value)
                }

                val project = project(
                    environmentsDirectory = environmentsDirectory,
                    schema = schemaParsing.await().bind(),
                    environments = environmentsParsing.await().bind(),
                    properties = propertiesParsing.await().bind(),
                ).bind()

                Success(project = project)
            }
        }

    private fun SchemaParserResult.schemaOrFailure(environmentsDirectoryPath: String): Either<Failure, Schema> =
        when (this) {
            is SchemaParserResult.Failure ->
                Failure.SchemaParsing(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    error = this
                ).left()

            is SchemaParserResult.Success -> schema.right()
        }

    private fun EnvironmentsParserResult.environmentsOrFailure(): Either<Failure, NonEmptyKeySetStore<String, Environment>?> =
        when (this) {
            is EnvironmentsParserResult.Failure -> Failure.EnvironmentsParsing(
                environmentsDirectoryPath = environmentsDirectoryPath,
                error = this,
            ).left()

            is EnvironmentsParserResult.Success -> environments.right()
        }

    private fun PropertiesParserResult.propertiesOrFailure(environmentsDirectoryPath: String): Either<Failure, Properties> =
        when (this) {
            is PropertiesParserResult.Failure ->
                Failure.PropertiesParsing(
                    environmentsDirectoryPath = environmentsDirectoryPath,
                    error = this,
                ).left()

            is PropertiesParserResult.Success -> properties.right()
        }

    private fun project(
        environmentsDirectory: ExistingDirectory,
        schema: Schema,
        environments: NonEmptyKeySetStore<String, Environment>?,
        properties: Properties,
    ): Either<Failure, Project> {
        val projectValidationResult = projectOf(
            environmentsDirectory = environmentsDirectory,
            schema = schema,
            environments = environments,
            properties = properties,
        )

        return when (projectValidationResult) {
            is ProjectValidationResult.Success -> projectValidationResult.project.right()
            is ProjectValidationResult.Failure -> Failure.ProjectValidation(
                environmentsDirectoryPath = environmentsDirectory.path.value,
                error = projectValidationResult
            ).left()
        }
    }

    override suspend fun processRecursively(rootDirectory: ExistingDirectory): List<EnvironmentsProcessorResult> =
        coroutineScope {
            rootDirectory
                .environmentsDirectoriesPaths()
                .map { environmentsDirectory ->
                    async {
                        process(environmentsDirectory = environmentsDirectory)
                    }
                }
                .awaitAll()
        }

    private fun ExistingDirectory.environmentsDirectoriesPaths(): List<ExistingDirectory> =
        existingDirectories { directoryName -> directoryName == ENVIRONMENTS_DIRECTORY_NAME }
}