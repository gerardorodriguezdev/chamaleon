package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.windows.createEnvironment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.PreviewContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SelectEnvironmentPathWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State

@Preview
@Composable
internal fun SelectEnvironmentPathWindowPreview() {
    PreviewContainer {
        SelectEnvironmentPathWindow(
            state = State.SelectEnvironmentPath(
                "/location/location/location/location/location/location/location/location/",
            ),
            onSelectEnvironmentPathClicked = {}
        )
    }
}