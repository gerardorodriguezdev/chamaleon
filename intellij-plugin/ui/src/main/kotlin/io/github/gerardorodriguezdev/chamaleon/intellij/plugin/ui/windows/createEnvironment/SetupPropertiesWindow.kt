package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupPropertiesAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupPropertiesAction.OnPropertyValueChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupPropertiesState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupPropertiesState.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupPropertiesState.PropertyValue.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesConstants.allBooleans
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesConstants.allNullableBooleans
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue as EntityPropertyValue

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
    index: Int,
    propertyValue: PropertyValue,
    onAction: (action: SetupPropertiesAction) -> Unit
) {
    when (propertyValue) {
        is StringProperty ->
            InputStringProperty(
                index = index,
                property = propertyValue,
                onAction = onAction,
            )

        is BooleanProperty ->
            InputBooleanProperty(
                index = index,
                property = propertyValue,
                onAction = onAction,
            )

        is NullableBooleanProperty ->
            InputNullableBooleanProperty(
                index = index,
                property = propertyValue,
                onAction = onAction,
            )
    }
}

@Composable
private fun InputStringProperty(
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
                    index = index,
                    newValue = StringProperty(newText).toEntityPropertyValue(),
                )
            )
        },
    )
}

@Composable
private fun InputBooleanProperty(
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
                                index = index,
                                newValue = BooleanProperty(boolean).toEntityPropertyValue(),
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
                                index = index,
                                newValue = NullableBooleanProperty(nullableBoolean).toEntityPropertyValue(),
                            )
                        )
                    }
                )
            }
        }
    )
}

private fun PropertyValue.toEntityPropertyValue(): EntityPropertyValue? =
    when (this) {
        is StringProperty -> EntityPropertyValue.StringProperty(value)
        is BooleanProperty -> EntityPropertyValue.BooleanProperty(value)
        is NullableBooleanProperty -> value?.let { EntityPropertyValue.BooleanProperty(value) }
    }

private object SetupPropertiesConstants {
    val allBooleans = booleanArrayOf(true, false)
    val allNullableBooleans: Array<Boolean?> = arrayOf(true, false, null)
}