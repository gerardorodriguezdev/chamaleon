package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction.SetupEnvironmentAction.OnEnvironmentNameChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPath
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupEnvironmentState
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun SetupEnvironmentWindow(
    state: SetupEnvironmentState,
    onAction: (action: SetupEnvironmentAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    WindowContainer(
        modifier = modifier,
        toolbar = {
            Toolbar(title = string(StringsKeys.setupEnvironment), forceLabelWidth = false)
        },
        content = {
            item {
                InputTextField(
                    label = string(StringsKeys.environmentsDirectory),
                    field = state.environmentsDirectoryPathField,
                    readOnly = true,
                    trailingIcon = {
                        TooltipIconButton(
                            iconKey = AllIconsKeys.Actions.NewFolder,
                            tooltip = string(StringsKeys.selectEnvironmentsDirectoryLocation),
                            onClick = { onAction(OnSelectEnvironmentPath) }
                        )
                    }
                )
            }

            item {
                Section(enableDivider = true) {
                    InputTextField(
                        label = string(StringsKeys.environmentName),
                        field = state.environmentNameField,
                        onValueChange = { newText ->
                            onAction(OnEnvironmentNameChanged(newText))
                        },
                    )
                }
            }
        }
    )
}