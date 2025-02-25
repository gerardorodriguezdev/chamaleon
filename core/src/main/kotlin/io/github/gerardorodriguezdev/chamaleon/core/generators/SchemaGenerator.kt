package io.github.gerardorodriguezdev.chamaleon.core.generators

import io.github.gerardorodriguezdev.chamaleon.core.mappers.SchemaMapperImpl
import io.github.gerardorodriguezdev.chamaleon.core.models.Schema
import io.github.gerardorodriguezdev.chamaleon.core.results.AddSchemaResult
import io.github.gerardorodriguezdev.chamaleon.core.results.AddSchemaResult.Failure
import io.github.gerardorodriguezdev.chamaleon.core.results.AddSchemaResult.Success
import io.github.gerardorodriguezdev.chamaleon.core.utils.PrettyJson
import java.io.File

public interface SchemaGenerator {
    public fun addSchema(schemaFile: File, newSchema: Schema): AddSchemaResult
}

internal object DefaultSchemaGenerator : SchemaGenerator {

    override fun addSchema(schemaFile: File, newSchema: Schema): AddSchemaResult {
        return try {
            if (!schemaFile.isFile) return Failure.InvalidFile(schemaFilePath = schemaFile.path)
            if (!schemaFile.exists()) return Failure.FileAlreadyPresent(schemaFilePath = schemaFile.path)

            schemaFile.createNewFile()

            val schemaDto = SchemaMapperImpl.toDto(newSchema)
            val schemaFileContent = PrettyJson.encodeToString(schemaDto)
            schemaFile.writeText(schemaFileContent)

            Success
        } catch (error: Exception) {
            Failure.Serialization(schemaFilePath = schemaFile.path, throwable = error)
        }
    }
}