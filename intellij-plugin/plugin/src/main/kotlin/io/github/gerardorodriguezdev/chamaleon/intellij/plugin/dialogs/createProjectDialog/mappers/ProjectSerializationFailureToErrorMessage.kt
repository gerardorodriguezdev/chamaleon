package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers

import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult.Failure
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider

internal fun Failure.toErrorMessage(stringsProvider: StringsProvider): String =
    when (this) {
        is Failure.InvalidPropertiesFile ->
            stringsProvider.string(StringsKeys.invalidPropertiesFile(environmentsDirectoryPath))

        is Failure.InvalidEnvironmentFile ->
            stringsProvider.string(
                StringsKeys.invalidEnvironmentFile(
                    environmentName = environmentFileName,
                    environmentsDirectoryPath = environmentsDirectoryPath
                )
            )

        is Failure.InvalidSchemaFile ->
            stringsProvider.string(StringsKeys.invalidSchemaFile(environmentsDirectoryPath))

        is Failure.Serialization ->
            stringsProvider.string(
                StringsKeys.serializationError(
                    errorMessage = throwable.toString(),
                    environmentsDirectoryPath = environmentsDirectoryPath,
                )
            )
    }