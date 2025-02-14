package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.windows.createEnvironment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.PreviewContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupPropertiesState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupPropertiesState.Platform.Property
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupPropertiesState.PropertyValue
import kotlinx.collections.immutable.persistentListOf

@Preview
@Composable
internal fun SetupPropertiesWindowPreview() {
    PreviewContainer {
        val properties = persistentListOf(
            Property(
                name = "name",
                value = PropertyValue.BooleanProperty(true),
            ),
            Property(
                name = "name",
                value = PropertyValue.BooleanProperty(false),
            ),
            Property(
                name = "name",
                value = PropertyValue.NullableBooleanProperty(null),
            ),
            Property(
                name = "name",
                value = PropertyValue.StringProperty("value"),
            ),
            Property(
                name = "name",
                value = PropertyValue.StringProperty(""),
            )
        )

        val platforms = persistentListOf(
            SetupPropertiesState.Platform(
                PlatformType.JVM,
                properties = properties,
            ),
            SetupPropertiesState.Platform(
                PlatformType.WASM,
                properties = properties,
            )
        )

        SetupPropertiesWindow(
            state = SetupPropertiesState(
                platforms = platforms,
            ),
            onAction = {}
        )
    }
}