package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.LocalStrings
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.TooltipIcon
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.TooltipIconButton
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState.Verification
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun SelectEnvironmentsDirectoryLocationWindow(
    state: SelectEnvironmentsDirectoryLocationState,
    onIconClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = LocalStrings.current.environmentsDirectoryLocation, modifier = Modifier.widthIn(max = 140.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val textFieldState = rememberTextFieldState(initialText = state.path)
                TextField(
                    state = textFieldState,
                    readOnly = true,
                    trailingIcon = {
                        TooltipIconButton(
                            iconKey = AllIconsKeys.Actions.NewFolder,
                            tooltip = state.path,
                            onClick = { onIconClicked() }
                        )
                    },
                )

                state.verification?.let {
                    VerificationIcon(verification = state.verification)
                }
            }
        }
    }
}

@Composable
private fun VerificationIcon(verification: Verification) {
    val modifier = Modifier.size(24.dp)
    when (verification) {
        is Verification.Valid -> ValidIcon(modifier = modifier)
        is Verification.Invalid -> InvalidIcon(
            invalidVerification = verification,
            modifier = modifier
        )

        is Verification.InProgress -> InProgressIcon(modifier = modifier)
    }
}

@Composable
private fun ValidIcon(modifier: Modifier) {
    TooltipIcon(
        iconKey = AllIconsKeys.Actions.Checked,
        tooltip = LocalStrings.current.validEnvironments,
        modifier = modifier,
    )
}

@Composable
private fun InvalidIcon(invalidVerification: Verification.Invalid, modifier: Modifier) {
    TooltipIcon(
        iconKey = AllIconsKeys.RunConfigurations.InvalidConfigurationLayer,
        tooltip = invalidVerification.reason,
        modifier = modifier,
    )
}

@Composable
private fun InProgressIcon(modifier: Modifier) {
    CircularProgressIndicator(modifier = modifier)
}