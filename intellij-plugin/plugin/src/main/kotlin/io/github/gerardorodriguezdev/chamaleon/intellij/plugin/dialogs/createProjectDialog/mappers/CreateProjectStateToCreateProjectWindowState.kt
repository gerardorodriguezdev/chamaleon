package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.dialogs.createProjectDialog.mappers

import io.github.gerardorodriguezdev.chamaleon.core.safeModels.NonEmptyString
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.presentation.createProjectPresenter.CreateProjectState.SetupEnvironment.ProjectDeserializationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsProvider
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field.Verification
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState

internal fun CreateProjectState.toCreateProjectWindowState(
    projectDirectoryPath: String,
    stringsProvider: StringsProvider
): CreateProjectWindowState? {
    val context = Context(projectDirectoryPath, stringsProvider)

    return when (this) {
        is CreateProjectState.SetupEnvironment -> context.toSetupEnvironment(this)
        is CreateProjectState.SetupSchema -> null
        is CreateProjectState.SetupPlatforms -> null
        is CreateProjectState.Finish -> null
    }

}

private fun Context.toSetupEnvironment(state: CreateProjectState.SetupEnvironment): CreateProjectWindowState.SetupEnvironmentState {
    val projectDeserializationState = state.projectDeserializationState
    return CreateProjectWindowState.SetupEnvironmentState(
        environmentsDirectoryPathField = toEnvironmentsDirectoryPathField(projectDeserializationState),
        environmentNameField = toEnvironmentNameField(state.environmentName, projectDeserializationState),
    )
}

private fun Context.toEnvironmentsDirectoryPathField(projectDeserializationState: ProjectDeserializationState?): Field<String> {
    return when (projectDeserializationState) {
        null -> Field(value = "", verification = null)

        is ProjectDeserializationState.Valid ->
            Field(value = "", verification = Verification.Valid)

        is ProjectDeserializationState.Loading ->
            Field(
                value = projectDeserializationState.environmentsDirectory.path.value,
                verification = Verification.Loading
            )

        is ProjectDeserializationState.Invalid ->
            Field(
                value = projectDeserializationState.environmentsDirectory.path.value,
                verification = Verification.Invalid(projectDeserializationState.errorMessage),
            )
    }
}

private fun Context.toEnvironmentNameField(
    environmentName: NonEmptyString?,
    projectDeserializationState: ProjectDeserializationState?,
): Field<String> {
    return if (environmentName == null) {
        Field(
            value = "",
            verification = Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameEmpty))
        )
    } else {
        if (projectDeserializationState is ProjectDeserializationState.Valid) {
            when (projectDeserializationState) {
                is ProjectDeserializationState.Valid.NewProject ->
                    Field(value = environmentName.value, verification = Verification.Valid)

                is ProjectDeserializationState.Valid.ExistingProject ->
                    Field(
                        value = environmentName.value,
                        verification = if (projectDeserializationState.currentProject.environments?.contains(key = environmentName.value) == true) {
                            Verification.Invalid(stringsProvider.string(StringsKeys.environmentNameIsDuplicated))
                        } else {
                            Verification.Valid
                        }
                    )
            }
        } else {
            Field(value = environmentName.value, verification = null)
        }
    }
}

private data class Context(
    val projectDirectoryPath: String,
    val stringsProvider: StringsProvider,
)