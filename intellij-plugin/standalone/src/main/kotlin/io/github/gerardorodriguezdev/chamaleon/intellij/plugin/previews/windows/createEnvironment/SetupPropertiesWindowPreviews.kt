package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.windows.createEnvironment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.PreviewContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupPropertiesState
import kotlinx.collections.immutable.persistentListOf

@Preview
@Composable
fun SetupPropertiesWindowPreview() {
    PreviewContainer {
        SetupPropertiesWindow(
            state = SetupPropertiesState(
                environmentName = "Environment name",
                properties = persistentListOf(
                    SetupPropertiesState.Property(
                        name = "name",
                        value = PropertyValue.BooleanProperty(true),
                    ),
                    SetupPropertiesState.Property(
                        name = "name",
                        value = PropertyValue.BooleanProperty(false),
                    ),
                    SetupPropertiesState.Property(
                        name = "name",
                        value = PropertyValue.StringProperty("value"),
                    )
                ),
            )
        )
    }
}