package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.LocalStrings
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.InputCheckBox
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.InputField
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.TooltipIconButton
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupSchemaConstants.allPlatforms
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupSchemaConstants.allPropertyTypes
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState.PropertyDefinition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupSchemaWindow(
    state: SetupSchemaState,
    onSupportedPlatformsChanged: (platformType: PlatformType) -> Unit,
    onAddPropertyDefinitionClicked: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        stickyHeader {
            Title(title = state.title)
        }

        supportedPlatformSection(
            supportedPlatforms = state.supportedPlatforms,
            onCheckedChanged = onSupportedPlatformsChanged,
        )

        propertyDefinitionsSection(
            propertyDefinitions = state.propertyDefinitions,
            onAddPropertyDefinitionClicked = onAddPropertyDefinitionClicked,
            supportedPlatforms = state.supportedPlatforms,
            onPropertyDefinitionChanged = { propertyDefinition -> } //TODO: Connect
        )
    }
}

@Composable
private fun Title(title: String) {
    Text(text = title)
}

private fun LazyListScope.supportedPlatformSection(
    supportedPlatforms: ImmutableList<PlatformType>,
    onCheckedChanged: (platformType: PlatformType) -> Unit,
) {
    section(
        title = { Text(text = LocalStrings.current.supportedPlatforms) },
    ) {
        item {
            SupportedPlatforms(
                supportedPlatforms = supportedPlatforms,
                onCheckedChanged = onCheckedChanged,
            )
        }
    }
}

private fun LazyListScope.propertyDefinitionsSection(
    supportedPlatforms: ImmutableList<PlatformType>,
    propertyDefinitions: ImmutableList<PropertyDefinition>,
    onAddPropertyDefinitionClicked: () -> Unit,
    onPropertyDefinitionChanged: (propertyDefinition: PropertyDefinition) -> Unit,
) {
    section(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = LocalStrings.current.propertyDefinitions, modifier = Modifier.widthIn(min = 140.dp))

                TooltipIconButton(
                    iconKey = AllIconsKeys.General.Add,
                    tooltip = LocalStrings.current.addPropertyDefinitions,
                    onClick = onAddPropertyDefinitionClicked,
                )
            }
        }
    ) {
        items(propertyDefinitions) { propertyDefinition ->
            //TODO: Make card item
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Divider(orientation = Orientation.Horizontal)

                InputField(
                    label = "Property name:", //TODO: String
                    initialValue = propertyDefinition.name,
                    onValueChange = { newName ->
                        onPropertyDefinitionChanged(
                            propertyDefinition.copy(
                                name = newName,
                            )
                        )
                    },
                )

                //TODO: Component
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Property type:", modifier = Modifier.widthIn(min = 140.dp))

                    Dropdown(
                        menuContent = {
                            allPropertyTypes.forEach { propertyType ->
                                selectableItem(
                                    selected = propertyDefinition.propertyType == propertyType,
                                    onClick = {
                                        //TODO: Finish
                                    }
                                ) {
                                    //TODO: Use string cap
                                    Text(text = propertyType.name.lowercase())
                                }
                            }
                        },
                        content = {
                            //TODO: Use string cap
                            Text(text = propertyDefinition.propertyType.name.lowercase())
                        }
                    )
                }

                InputCheckBox(
                    label = "Nullable:", //TODO: Finish
                    isChecked = propertyDefinition.nullable,
                    onCheckedChanged = {
                        val isNullable = propertyDefinition.nullable
                        onPropertyDefinitionChanged(
                            propertyDefinition.copy(nullable = !isNullable)
                        )
                    }
                )

                Text(text = "Supported platforms for property definition:")

                SupportedPlatforms(
                    supportedPlatforms = if (propertyDefinition.supportedPlatforms.isEmpty()) {
                        supportedPlatforms
                    } else {
                        propertyDefinition.supportedPlatforms
                    },
                    onCheckedChanged = { platformType ->
                        val currentSupportedPlatforms = propertyDefinition.supportedPlatforms

                        val containsPlatform = currentSupportedPlatforms.contains(platformType)

                        val newMutableSupportedPlatforms = currentSupportedPlatforms.toMutableList()
                        if (containsPlatform) newMutableSupportedPlatforms.remove(platformType) else newMutableSupportedPlatforms.add(
                            platformType
                        )
                        val newSupportedPlatforms = newMutableSupportedPlatforms.toPersistentList()

                        onPropertyDefinitionChanged(
                            propertyDefinition.copy(
                                supportedPlatforms = newSupportedPlatforms,
                            )
                        )
                    }
                )
            }
        }
    }
}

private fun LazyListScope.section(title: @Composable () -> Unit, content: LazyListScope.() -> Unit) {
    item {
        Divider(orientation = Orientation.Horizontal)
    }

    item {
        title()
    }

    content()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportedPlatforms(
    supportedPlatforms: ImmutableList<PlatformType>,
    onCheckedChanged: (platformType: PlatformType) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        allPlatforms.forEach { platformType ->
            val isChecked by remember {
                derivedStateOf { supportedPlatforms.contains(platformType) }
            }

            InputCheckBox(
                label = platformType.serialName,
                isChecked = isChecked,
                onCheckedChanged = { onCheckedChanged(platformType) }
            )
        }
    }
}

private object SetupSchemaConstants {
    val allPlatforms = PlatformType.entries.toImmutableList()
    val allPropertyTypes = PropertyType.entries
}