package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.windows.createEnvironment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.PreviewContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SelectEnvironmentsDirectoryLocationWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State

@Preview
@Composable
internal fun SelectEnvironmentsDirectoryLocationWindowPreview() {
    PreviewContainer {
        SelectEnvironmentsDirectoryLocationWindow(
            state = State.SelectEnvironmentsDirectoryLocationState(
                "/location/location/location/location/location/location/location/location/",
            ),
            onIconClicked = {}
        )
    }
}