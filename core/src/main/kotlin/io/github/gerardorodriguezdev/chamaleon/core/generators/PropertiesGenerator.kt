package io.github.gerardorodriguezdev.chamaleon.core.generators

import io.github.gerardorodriguezdev.chamaleon.core.dtos.PropertiesDto
import io.github.gerardorodriguezdev.chamaleon.core.results.AddOrUpdateSelectedEnvironmentResult
import io.github.gerardorodriguezdev.chamaleon.core.results.AddOrUpdateSelectedEnvironmentResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.AddOrUpdateSelectedEnvironmentResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import java.io.File

public interface PropertiesGenerator {
    public fun addOrUpdateSelectedEnvironment(
        propertiesFile: File,
        newSelectedEnvironment: String?
    ): AddOrUpdateSelectedEnvironmentResult
}

internal object DefaultPropertiesGenerator : PropertiesGenerator {

    override fun addOrUpdateSelectedEnvironment(
        propertiesFile: File,
        newSelectedEnvironment: String?,
    ): AddOrUpdateSelectedEnvironmentResult {
        return try {
            if (!propertiesFile.isFile) {
                return Failure.InvalidFile(
                    propertiesFilePath = propertiesFile.path,
                )
            }
            if (newSelectedEnvironment != null && newSelectedEnvironment.isEmpty()) {
                return Failure.EnvironmentNameIsEmpty(
                    propertiesFilePath = propertiesFile.path,
                )
            }

            if (!propertiesFile.exists()) propertiesFile.createNewFile()

            val propertiesDto = PropertiesDto(newSelectedEnvironment)
            val propertiesFileContent = PrettyJson.encodeToString(propertiesDto)
            propertiesFile.writeText(propertiesFileContent)

            Success
        } catch (error: Exception) {
            return Failure.Serialization(
                propertiesFilePath = propertiesFile.path,
                throwable = error
            )
        }
    }
}