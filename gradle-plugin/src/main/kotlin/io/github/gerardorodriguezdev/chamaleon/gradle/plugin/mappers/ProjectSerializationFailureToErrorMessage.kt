package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers

import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult

internal fun ProjectSerializationResult.Failure.toErrorMessage(): String =
    when (this) {
        is ProjectSerializationResult.Failure.InvalidPropertiesFile ->
            "Invalid properties file at '$environmentsDirectoryPath'"

        is ProjectSerializationResult.Failure.InvalidEnvironmentFile ->
            "Invalid environment file named '$environmentFileName' at '$environmentsDirectoryPath'"

        is ProjectSerializationResult.Failure.InvalidSchemaFile ->
            "Invalid properties file at '$environmentsDirectoryPath'"

        is ProjectSerializationResult.Failure.Serialization ->
            "Project serialization failed with error: '$error' at '$environmentsDirectoryPath'"
    }