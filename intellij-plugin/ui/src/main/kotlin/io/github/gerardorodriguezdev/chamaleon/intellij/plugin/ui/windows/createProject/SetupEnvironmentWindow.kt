package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction.SetupEnvironmentAction.OnEnvironmentNameChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPath
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupEnvironmentState
import org.jetbrains.jewel.ui.icons.AllIconsKeys

// TODO: Not notification if field not valid
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
                    value = state.environmentsDirectoryPathField.value,
                    readOnly = true,
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
                        ) {
                            TooltipIconButton(
                                iconKey = AllIconsKeys.Actions.NewFolder,
                                tooltip = string(StringsKeys.selectEnvironmentsDirectoryLocation),
                                onClick = { onAction(OnSelectEnvironmentPath) }
                            )

                            state.environmentsDirectoryPathField.verification?.let {
                                VerificationIcon(verification = state.environmentsDirectoryPathField.verification)
                            }
                        }
                    }
                )
            }

            item {
                Section(enableDivider = true) {
                    InputTextField(
                        label = string(StringsKeys.environmentName),
                        value = state.environmentNameField.value,
                        onValueChange = { newText ->
                            onAction(OnEnvironmentNameChanged(newText))
                        },
                        trailingIcon = {
                            state.environmentNameField.verification?.let {
                                VerificationIcon(verification = state.environmentNameField.verification)
                            }
                        }
                    )
                }
            }
        }
    )
}