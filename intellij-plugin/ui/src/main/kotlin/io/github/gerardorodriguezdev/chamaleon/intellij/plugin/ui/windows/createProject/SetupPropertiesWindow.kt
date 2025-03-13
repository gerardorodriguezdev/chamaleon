package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction.SetupPlatformsAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowAction.SetupPlatformsAction.OnPropertyValueChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupPlatformsState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupPlatformsState.Platform.PlatformType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.CreateProjectWindowState.SetupPlatformsState.Platform.Property.PropertyValue.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.SetupPlatformsConstants.allBooleans
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createProject.SetupPlatformsConstants.allNullableBooleans

@Composable
fun SetupPlatformsWindow(
    state: SetupPlatformsState,
    onAction: (action: SetupPlatformsAction) -> Unit,
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
                        title = platform.platformType.platformName,
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
    onAction: (action: SetupPlatformsAction) -> Unit
) {
    when (propertyValue) {
        is StringProperty ->
            InputStringProperty(
                platformType = platformType,
                index = index,
                property = propertyValue,
                onAction = onAction,
            )

        is NullableStringProperty ->
            InputNullableStringProperty(
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
    onAction: (action: SetupPlatformsAction) -> Unit,
) {
    InputTextField(
        label = string(StringsKeys.value),
        value = property.valueField.value,
        onValueChange = { newText ->
            onAction(
                OnPropertyValueChanged(
                    platformType = platformType,
                    index = index,
                    newValue = StringProperty(
                        Field(value = newText, verification = null),
                    ),
                )
            )
        },
        trailingIcon = {
            property.valueField.verification?.let {
                VerificationIcon(verification = property.valueField.verification)
            }
        }
    )
}

@Composable
private fun InputNullableStringProperty(
    platformType: PlatformType,
    index: Int,
    property: NullableStringProperty,
    onAction: (action: SetupPlatformsAction) -> Unit,
) {
    InputTextField(
        label = string(StringsKeys.value),
        value = property.value,
        onValueChange = { newText ->
            onAction(
                OnPropertyValueChanged(
                    platformType = platformType,
                    index = index,
                    newValue = NullableStringProperty(newText),
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
    onAction: (action: SetupPlatformsAction) -> Unit
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
    onAction: (action: SetupPlatformsAction) -> Unit
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

private object SetupPlatformsConstants {
    val allBooleans = booleanArrayOf(true, false)
    val allNullableBooleans: Array<Boolean?> = arrayOf(true, false, null)
}