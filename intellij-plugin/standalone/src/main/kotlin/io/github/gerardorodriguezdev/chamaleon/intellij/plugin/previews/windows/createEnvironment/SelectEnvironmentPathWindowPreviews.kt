package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.windows.createEnvironment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.PreviewContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SelectEnvironmentsDirectoryLocationAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SelectEnvironmentsDirectoryLocationWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SelectEnvironmentsDirectoryLocationState.Verification

@Preview
@Composable
internal fun SelectEnvironmentsDirectoryLocationWindowNoVerificationPreview() {
    SelectEnvironmentsDirectoryLocationWindowPreview(state = state())
}

@Preview
@Composable
internal fun SelectEnvironmentsDirectoryLocationWindowValidPreview() {
    SelectEnvironmentsDirectoryLocationWindowPreview(state = state(Verification.Valid))
}

@Preview
@Composable
internal fun SelectEnvironmentsDirectoryLocationWindowInvalidPreview() {
    SelectEnvironmentsDirectoryLocationWindowPreview(
        state = state(
            Verification.Invalid(
                reason = "Invalid schema already present",
            )
        )
    )
}

@Preview
@Composable
internal fun SelectEnvironmentsDirectoryLocationWindowInProgressPreview() {
    SelectEnvironmentsDirectoryLocationWindowPreview(state = state(Verification.InProgress))
}

@Composable
private fun SelectEnvironmentsDirectoryLocationWindowPreview(
    state: SelectEnvironmentsDirectoryLocationState,
    onAction: (action: SelectEnvironmentsDirectoryLocationAction) -> Unit = {},
) {
    PreviewContainer {
        SelectEnvironmentsDirectoryLocationWindow(
            state = state,
            onAction = onAction,
        )
    }
}

private fun state(verification: Verification? = null): SelectEnvironmentsDirectoryLocationState =
    SelectEnvironmentsDirectoryLocationState(
        path = "/location/location/location/location/location/location/location/location/",
        verification = verification,
    )