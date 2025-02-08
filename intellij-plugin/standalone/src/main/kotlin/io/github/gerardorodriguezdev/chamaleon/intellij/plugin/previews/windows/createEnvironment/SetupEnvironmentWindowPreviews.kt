package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.windows.createEnvironment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.PreviewContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.Verification
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupEnvironmentAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupEnvironmentWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupEnvironmentState

@Preview
@Composable
internal fun SetupEnvironmentWindowNoVerificationPreview() {
    SetupEnvironmentWindowPreview(state = state())
}

@Preview
@Composable
internal fun SetupEnvironmentWindowValidPreview() {
    SetupEnvironmentWindowPreview(state = state(Verification.Valid))
}

@Preview
@Composable
internal fun SetupEnvironmentWindowInvalidPreview() {
    SetupEnvironmentWindowPreview(
        state = state(
            Verification.Invalid(
                reason = "Invalid schema already present",
            )
        )
    )
}

@Preview
@Composable
internal fun SetupEnvironmentWindowInProgressPreview() {
    SetupEnvironmentWindowPreview(state = state(Verification.InProgress))
}

@Composable
private fun SetupEnvironmentWindowPreview(
    state: SetupEnvironmentState,
    onAction: (action: SetupEnvironmentAction) -> Unit = {},
) {
    PreviewContainer {
        SetupEnvironmentWindow(
            state = state,
            onAction = onAction,
        )
    }
}

private fun state(verification: Verification? = null): SetupEnvironmentState =
    SetupEnvironmentState(
        path = "/location/location/location/location/location/location/location/location/",
        verification = verification,
    )