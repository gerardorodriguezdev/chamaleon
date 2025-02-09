package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun VerificationIcon(verification: Verification) {
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

sealed interface Verification {
    data object Valid : Verification
    data class Invalid(val reason: String) : Verification
    data object InProgress : Verification
}