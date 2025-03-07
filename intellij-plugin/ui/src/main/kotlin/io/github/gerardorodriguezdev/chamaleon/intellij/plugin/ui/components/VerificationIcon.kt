package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field.Verification
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
internal fun VerificationIcon(verification: Verification) {
    when (verification) {
        is Verification.Valid -> ValidIcon()
        is Verification.Invalid -> InvalidIcon(invalidVerification = verification)
        is Verification.Loading -> InProgressIcon()
    }
}

@Composable
private fun ValidIcon() {
    TooltipIcon(
        iconKey = AllIconsKeys.Actions.Checked,
        tooltip = string(StringsKeys.validField),
    )
}

@Composable
private fun InvalidIcon(invalidVerification: Verification.Invalid) {
    TooltipIcon(
        iconKey = AllIconsKeys.Nodes.ErrorIntroduction,
        tooltip = invalidVerification.reason,
    )
}

@Composable
private fun InProgressIcon() {
    CircularProgressIndicator()
}