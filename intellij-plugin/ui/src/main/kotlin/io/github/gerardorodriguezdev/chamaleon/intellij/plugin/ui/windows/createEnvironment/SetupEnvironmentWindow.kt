package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction.OnEnvironmentNameChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupEnvironmentAction.OnSelectEnvironmentPathClicked
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupEnvironmentState
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
                    value = state.path,
                    readOnly = true,
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
                        ) {
                            TooltipIconButton(
                                iconKey = AllIconsKeys.Actions.NewFolder,
                                tooltip = state.path,
                                onClick = { onAction(OnSelectEnvironmentPathClicked) }
                            )

                            state.environmentsDirectoryVerification?.let {
                                VerificationIcon(verification = state.environmentsDirectoryVerification)
                            }
                        }
                    }
                )
            }

            item {
                Section(enableDivider = true) {
                    InputTextField(
                        label = string(StringsKeys.environmentName),
                        value = state.environmentName,
                        onValueChange = { newText ->
                            onAction(OnEnvironmentNameChanged(newText))
                        },
                        trailingIcon = {
                            val verification = state.environmentNameVerification.toVerification()
                            verification?.let {
                                VerificationIcon(verification = verification)
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun SetupEnvironmentState.EnvironmentNameVerification.toVerification(): Verification? =
    when (this) {
        SetupEnvironmentState.EnvironmentNameVerification.VALID -> null
        SetupEnvironmentState.EnvironmentNameVerification.IS_EMPTY -> Verification.Invalid(
            reason = string(StringsKeys.environmentNameEmpty),
        )

        SetupEnvironmentState.EnvironmentNameVerification.IS_DUPLICATED -> Verification.Invalid(
            reason = string(StringsKeys.environmentNameIsDuplicated),
        )
    }