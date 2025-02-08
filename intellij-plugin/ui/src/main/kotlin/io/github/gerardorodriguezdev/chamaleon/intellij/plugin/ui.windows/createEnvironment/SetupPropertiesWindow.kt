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
            // TODO: Do this for each platform available
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

                    // TODO: Add null case (show null on text edit placeholder or show null on first selected option but treat as not existent)
                    // TODO: Sep smaller pieces
                    when (property.value) {
                        is StringProperty ->
                            InputTextField(
                                label = string(StringsKeys.value),
                                initialValue = property.value.value,
                                onValueChange = { newText ->
                                    onAction(
                                        OnPropertyValueChanged(
                                            index = index,
                                            newValue = StringProperty(newText),
                                        )
                                    )
                                },
                            )

                        is BooleanProperty ->
                            InputTextDropdown(
                                label = string(StringsKeys.value),
                                selectedValue = property.value.value.toString(),
                                content = {
                                    // TODO: Pass bools from state
                                    allBooleans.forEach { boolean ->
                                        item(
                                            text = boolean.toString(),
                                            selected = boolean == property.value.value,
                                            onClick = {
                                                val newValue = !property.value.value
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
                }
            }
        }
    )
}

private object SetupPropertiesConstants {
    val allBooleans = booleanArrayOf(true, false)
}