@file:Suppress("InvalidPackageDeclaration")

package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.BooleanProperty
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue.StringProperty
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupPropertiesAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupPropertiesAction.OnPropertyNameChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupPropertiesAction.OnPropertyValueChanged
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesConstants.allBooleans
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupPropertiesState

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

            itemsIndexed(state.properties) { index, property ->
                Section(enableDivider = true) {
                    InputTextField(
                        label = string(StringsKeys.name),
                        onValueChange = { newText ->
                            onAction(OnPropertyNameChanged(index, newText))
                        },
                    )

                    when (property.value) {
                        is StringProperty ->
                            InputStringProperty(
                                index = index,
                                property = property.value,
                                onAction = onAction,
                            )

                        is BooleanProperty ->
                            InputBooleanProperty(
                                index = index,
                                property = property.value,
                                onAction = onAction,
                            )
                    }
                }
            }
        }
    )
}

@Composable
private fun InputStringProperty(
    index: Int,
    property: StringProperty,
    onAction: (action: SetupPropertiesAction) -> Unit,
) {
    InputTextField(
        label = string(StringsKeys.value),
        initialValue = property.value,
        onValueChange = { newText ->
            onAction(
                OnPropertyValueChanged(
                    index = index,
                    newValue = StringProperty(newText),
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
                        val newValue = !property.value
                        onAction(
                            OnPropertyValueChanged(
                                index = index,
                                newValue = BooleanProperty(newValue),
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
}