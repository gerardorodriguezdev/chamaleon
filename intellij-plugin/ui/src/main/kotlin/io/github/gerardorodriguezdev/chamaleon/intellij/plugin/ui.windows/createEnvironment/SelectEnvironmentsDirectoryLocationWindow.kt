package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState.Verification
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun SelectEnvironmentsDirectoryLocationWindow(
    state: SelectEnvironmentsDirectoryLocationState,
    onIconClicked: () -> Unit,
) {
    WindowContainer(
        toolbar = {
            Toolbar(title = string(StringsKeys.selectEnvironmentsDirectoryLocation), forceLabelWidth = false)
        },
        content = {
            item {
                InputTextField(
                    label = string(StringsKeys.environmentsDirectory),
                    initialValue = state.path,
                    readOnly = true,
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
                        ) {
                            TooltipIconButton(
                                iconKey = AllIconsKeys.Actions.NewFolder,
                                tooltip = state.path,
                                onClick = { onIconClicked() }
                            )

                            state.verification?.let {
                                VerificationIcon(verification = state.verification)
                            }
                        }
                    }
                )
            }
        }
    )
}

@Composable
private fun VerificationIcon(verification: Verification) {
    when (verification) {
        is Verification.Valid -> ValidIcon()
        is Verification.Invalid -> InvalidIcon(invalidVerification = verification)
        is Verification.InProgress -> InProgressIcon()
    }
}

@Composable
private fun ValidIcon() {
    TooltipIcon(
        iconKey = AllIconsKeys.Actions.Checked,
        tooltip = string(StringsKeys.validEnvironments),
    )
}

@Composable
private fun InvalidIcon(invalidVerification: Verification.Invalid) {
    TooltipIcon(
        iconKey = AllIconsKeys.RunConfigurations.InvalidConfigurationLayer,
        tooltip = invalidVerification.reason,
    )
}

@Composable
private fun InProgressIcon() {
    CircularProgressIndicator()
}