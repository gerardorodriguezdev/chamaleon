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
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.Action.SetupPropertiesAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesConstants.allBooleans
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupPropertiesState
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun SetupPropertiesWindow(
    state: SetupPropertiesState,
    onAction: (action: SetupPropertiesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    WindowContainer(
        modifier = modifier,
        toolbar = { Toolbar(title = string(StringsKeys.createEnvironment), forceLabelWidth = false) },
        content = {
            item {
                Section(
                    title = string(StringsKeys.properties),
                    forceLabelWidth = true,
                    enableDivider = true,
                    titleTrailingIcon = {
                        TooltipIconButton(
                            iconKey = AllIconsKeys.General.Add,
                            tooltip = string(StringsKeys.addProperty),
                            onClick = { onAction(OnAddPropertyClicked) }
                        )
                    }
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