package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupPropertiesAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupPropertiesAction.OnPropertyValueChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupPropertiesState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupPropertiesState.Platform.Property.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupPropertiesState.Platform.Property.PropertyValue.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesConstants.allBooleans
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesConstants.allNullableBooleans

@Composable
fun SetupPropertiesWindow(
    state: SetupPropertiesState,
    onAction: (action: SetupPropertiesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    WindowContainer(
        modifier = modifier,
        toolbar = { Toolbar(title = string(StringsKeys.addProperties), forceLabelWidth = false) },
        content = {
            item {
                Section(
                    title = string(StringsKeys.properties),
                    enableDivider = true,
                )
            }

            state.platforms.forEach { platform ->
                item {
                    Section(
                        title = platform.platformType.serialName,
                        enableDivider = true,
                    )
                }

                itemsIndexed(platform.properties) { index, property ->
                    Section(enableDivider = true) {
                        InputTextField(
                            label = string(StringsKeys.name),
                            value = property.name,
                            readOnly = true,
                        )

                        InputPropertyValue(
                            platformType = platform.platformType,
                            index = index,
                            propertyValue = property.value,
                            onAction = onAction,
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun InputPropertyValue(
    platformType: PlatformType,
    index: Int,
    propertyValue: PropertyValue,
    onAction: (action: SetupPropertiesAction) -> Unit
) {
    when (propertyValue) {
        is StringProperty ->
            InputStringProperty(
                platformType = platformType,
                index = index,
                property = propertyValue,
                onAction = onAction,
            )

        is BooleanProperty ->
            InputBooleanProperty(
                platformType = platformType,
                index = index,
                property = propertyValue,
                onAction = onAction,
            )

        is NullableBooleanProperty ->
            InputNullableBooleanProperty(
                platformType = platformType,
                index = index,
                property = propertyValue,
                onAction = onAction,
            )
    }
}

@Composable
private fun InputStringProperty(
    platformType: PlatformType,
    index: Int,
    property: StringProperty,
    onAction: (action: SetupPropertiesAction) -> Unit,
) {
    InputTextField(
        label = string(StringsKeys.value),
        value = property.value,
        onValueChange = { newText ->
            onAction(
                OnPropertyValueChanged(
                    platformType = platformType,
                    index = index,
                    newValue = StringProperty(newText),
                )
            )
        },
    )
}

@Composable
private fun InputBooleanProperty(
    platformType: PlatformType,
    index: Int,
    property: BooleanProperty,
    onAction: (action: SetupPropertiesAction) -> Unit
) {
    InputTextDropdown(
        label = string(StringsKeys.value),
        selectedValue = property.value.toString(),
        content = {
            allBooleans.forEach { boolean ->
                item(
                    text = boolean.toString(),
                    selected = boolean == property.value,
                    onClick = {
                        onAction(
                            OnPropertyValueChanged(
                                platformType = platformType,
                                index = index,
                                newValue = BooleanProperty(boolean),
                            )
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun InputNullableBooleanProperty(
    platformType: PlatformType,
    index: Int,
    property: NullableBooleanProperty,
    onAction: (action: SetupPropertiesAction) -> Unit
) {
    InputTextDropdown(
        label = string(StringsKeys.value),
        selectedValue = property.value.toString(),
        content = {
            allNullableBooleans.forEach { nullableBoolean ->
                item(
                    text = nullableBoolean.toString(),
                    selected = nullableBoolean == property.value,
                    onClick = {
                        onAction(
                            OnPropertyValueChanged(
                                platformType = platformType,
                                index = index,
                                newValue = NullableBooleanProperty(nullableBoolean),
                            )
                        )
                    }
                )
            }
        }
    )
}

private object SetupPropertiesConstants {
    val allBooleans = booleanArrayOf(true, false)
    val allNullableBooleans: Array<Boolean?> = arrayOf(true, false, null)
}