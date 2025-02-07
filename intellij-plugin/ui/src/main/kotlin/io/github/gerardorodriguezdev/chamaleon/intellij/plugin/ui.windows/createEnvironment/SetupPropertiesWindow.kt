package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyValue
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupPropertiesConstants.allBooleans
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupPropertiesState
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun SetupPropertiesWindow(
    state: SetupPropertiesState,
    modifier: Modifier = Modifier,
) {
    WindowContainer(
        modifier = modifier,
        toolbar = { Toolbar(title = string(StringsKeys.createEnvironment), forceLabelWidth = false) },
        content = {
            //TODO: Move to start
            item {
                Section(enableDivider = true) {
                    InputTextField(
                        label = string(StringsKeys.environmentName),
                        onValueChange = {}, //TODO: Finish
                    )
                }
            }

            item {
                Section(
                    title = string(StringsKeys.properties),
                    forceLabelWidth = true,
                    enableDivider = true,
                    titleTrailingIcon = {
                        TooltipIconButton(
                            iconKey = AllIconsKeys.General.Add,
                            tooltip = string(StringsKeys.addProperty),
                            onClick = {} //TODO: Finish
                        )
                    }
                )
            }

            items(state.properties) { property ->
                Section(enableDivider = true) {
                    InputTextField(
                        label = string(StringsKeys.name),
                        onValueChange = {}, //TODO: Finish
                    )

                    when (property.value) {
                        is PropertyValue.StringProperty ->
                            InputTextField(
                                label = string(StringsKeys.value),
                                initialValue = property.value.value,
                                onValueChange = {}, //TODO: Finish
                            )

                        is PropertyValue.BooleanProperty ->
                            InputTextDropdown(
                                label = string(StringsKeys.value),
                                selectedValue = property.value.value.toString(),
                                content = {
                                    allBooleans.forEach { boolean ->
                                        item(
                                            text = boolean.toString(),
                                            selected = boolean == property.value.value,
                                            onClick = {} //TODO: Finish
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