package io.github.gerardorodriguezdev.chamaleon.gradle.plugin.extensions

import io.github.gerardorodriguezdev.chamaleon.core.models.Project
import io.github.gerardorodriguezdev.chamaleon.core.results.ProjectSerializationResult
import io.github.gerardorodriguezdev.chamaleon.core.serializers.ProjectSerializer
import io.github.gerardorodriguezdev.chamaleon.gradle.plugin.mappers.toErrorMessage
import kotlinx.coroutines.runBlocking

internal fun Project.serialize(
    projectSerializer: ProjectSerializer,
    onSuccess: Project.() -> Unit,
    onFailure: (errorMessage: String) -> Unit,
) = runBlocking {
    when (val updateProjectResult = projectSerializer.serialize(this@serialize)) {
        is ProjectSerializationResult.Success -> onSuccess()
        is ProjectSerializationResult.Failure -> onFailure(updateProjectResult.toErrorMessage())
    }
}