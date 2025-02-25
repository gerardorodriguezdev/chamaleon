package io.github.gerardorodriguezdev.chamaleon.core.generators

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import io.github.gerardorodriguezdev.chamaleon.core.dtos.PlatformDto
import io.github.gerardorodriguezdev.chamaleon.core.extractors.EnvironmentFileNameExtractor
import io.github.gerardorodriguezdev.chamaleon.core.mappers.PlatformMapperImpl
import io.github.gerardorodriguezdev.chamaleon.core.models.Environment
import io.github.gerardorodriguezdev.chamaleon.core.models.Platform
import io.github.gerardorodriguezdev.chamaleon.core.results.AddEnvironmentsResult
import io.github.gerardorodriguezdev.chamaleon.core.results.AddEnvironmentsResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.AddEnvironmentsResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File

public interface EnvironmentsGenerator {
    public suspend fun addEnvironments(
        environmentsDirectory: File,
        newEnvironments: Set<Environment>,
    ): AddEnvironmentsResult
}

internal class DefaultEnvironmentsGenerator(
    private val environmentFileNameExtractor: EnvironmentFileNameExtractor,
) : EnvironmentsGenerator {

    override suspend fun addEnvironments(
        environmentsDirectory: File,
        newEnvironments: Set<Environment>,
    ): AddEnvironmentsResult =
        coroutineScope {
            if (newEnvironments.isEmpty()) {
                return@coroutineScope Failure.EmptyEnvironments(
                    environmentsDirectoryPath = environmentsDirectory.path
                )
            }

            return@coroutineScope try {
                if (!environmentsDirectory.isDirectory) {
                    return@coroutineScope Failure.InvalidDirectory(
                        environmentsDirectoryPath = environmentsDirectory.path
                    )
                }

                val operations = newEnvironments.toOperationsOrFailure(environmentsDirectory)
                operations.fold(
                    ifLeft = { it },
                    ifRight = { operations ->
                        operations
                            .map { operation ->
                                async { operation.execute() }
                            }
                            .awaitAll()

                        Success
                    },
                )
            } catch (error: Exception) {
                Failure.Serialization(
                    environmentsDirectoryPath = environmentsDirectory.path,
                    throwable = error
                )
            }
        }

    private fun Set<Environment>.toOperationsOrFailure(environmentsDirectory: File): Either<Failure, List<Operation>> =
        either {
            map { environment ->
                val environmentFileName = environmentFileNameExtractor(environment.name)
                val environmentFile = File(environmentsDirectory, environmentFileName)

                ensure(environmentFile.exists()) {
                    Failure.FileAlreadyPresent(
                        environmentsDirectoryPath = environmentFile.path,
                        environmentName = environmentFileName,
                    )
                }

                Operation(
                    environmentFile = environmentFile,
                    environment = environment,
                )
            }
        }

    private fun Operation.execute() {
        environmentFile.createNewFile()

        val platformDtos = environment.platforms.toPlatformDtos()
        val platformDtosJson = PrettyJson.encodeToString(platformDtos)
        environmentFile.writeText(platformDtosJson)
    }

    private fun Set<Platform>.toPlatformDtos(): Set<PlatformDto> = map { PlatformMapperImpl.toDto(it) }.toSet()

    private data class Operation(
        val environmentFile: File,
        val environment: Environment,
    )
}