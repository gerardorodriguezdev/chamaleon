package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.windows.createEnvironment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.previews.utils.PreviewContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupSchemaWindow
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState.PropertyDefinition
import kotlinx.collections.immutable.persistentListOf

@Preview
@Composable
internal fun SetupSchemaWindowPreview() {
    PreviewContainer {
        SetupSchemaWindow(
            state = SetupSchemaState(
                title = "Some title",
                supportedPlatforms = persistentListOf(
                    SetupSchemaState.SupportedPlatform(
                        isChecked = true,
                        platformType = PlatformType.ANDROID,
                    ),
                    SetupSchemaState.SupportedPlatform(
                        isChecked = false,
                        platformType = PlatformType.NATIVE,
                    )
                ),
                propertyDefinitions = persistentListOf(
                    PropertyDefinition(
                        name = "one",
                        propertyType = PropertyType.STRING,
                        nullable = false,
                        supportedPlatforms = persistentListOf(),
                    ),
                    PropertyDefinition(
                        name = "two",
                        propertyType = PropertyType.BOOLEAN,
                        nullable = true,
                        supportedPlatforms = persistentListOf(
                            SetupSchemaState.SupportedPlatform(
                                isChecked = true,
                                platformType = PlatformType.ANDROID,
                            ),
                            SetupSchemaState.SupportedPlatform(
                                isChecked = false,
                                platformType = PlatformType.NATIVE,
                            )
                        ),
                    ),
                )
            ),
            onSupportedPlatformsChanged = {},
            onAddPropertyDefinitionClicked = {},
        )
    }
}