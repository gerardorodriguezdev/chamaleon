package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupSchemaConstants.allPropertyTypes
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState.SupportedPlatform
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupSchemaWindow(
    state: SetupSchemaState,
    onSupportedPlatformsChanged: (platformType: PlatformType) -> Unit,
    onAddPropertyDefinitionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WindowContainer(
        modifier = modifier,
        toolbar = { Toolbar(title = state.title, forceLabelWidth = false) },
        content = {
            supportedPlatformSection(
                supportedPlatforms = state.supportedPlatforms,
                onSupportedPlatformChanged = onSupportedPlatformsChanged,
            )

            propertyDefinitionsSection(
                propertyDefinitions = state.propertyDefinitions,
                onAddPropertyDefinitionClicked = onAddPropertyDefinitionClicked,
            )
        }
    )
}

private fun LazyListScope.supportedPlatformSection(
    supportedPlatforms: ImmutableList<SupportedPlatform>,
    onSupportedPlatformChanged: (platformType: PlatformType) -> Unit,
) {
    item {
        Section(
            title = string(StringsKeys.supportedPlatforms),
            enableDivider = true,
            forceLabelWidth = false,
        ) {
            SupportedPlatforms(
                supportedPlatforms = supportedPlatforms,
                onCheckedChanged = onSupportedPlatformChanged,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.propertyDefinitionsSection(
    propertyDefinitions: ImmutableList<PropertyDefinition>,
    onAddPropertyDefinitionClicked: () -> Unit,
) {
    stickyHeader {
        PropertyDefinitionSectionTitle(onAddPropertyDefinitionClicked = onAddPropertyDefinitionClicked)
    }

    items(propertyDefinitions) { propertyDefinition ->
        PropertyDefinitionSectionCard(propertyDefinition)
    }
}

@Composable
private fun PropertyDefinitionSectionTitle(onAddPropertyDefinitionClicked: () -> Unit) {
    Section(
        title = string(StringsKeys.propertyDefinitions),
        titleTrailingIcon = {
            TooltipIconButton(
                iconKey = AllIconsKeys.General.Add,
                tooltip = string(StringsKeys.addPropertyDefinitions),
                onClick = onAddPropertyDefinitionClicked,
            )
        },
        forceLabelWidth = true,
        enableDivider = true
    )
}

@Composable
private fun PropertyDefinitionSectionCard(
    propertyDefinition: PropertyDefinition,
) {
    Section(enableDivider = true) {
        InputTextField(
            label = string(StringsKeys.propertyName),
            initialValue = propertyDefinition.name,
            onValueChange = { newName -> }, //TODO: Finish
        )

        InputTextDropdown(
            label = string(StringsKeys.propertyType),
            selectedValue = propertyDefinition.propertyType.name.lowercase(),
            content = {
                allPropertyTypes.forEach { propertyType ->
                    item(
                        selected = propertyDefinition.propertyType == propertyType,
                        text = propertyType.name.lowercase(),
                        onClick = { } //TODO: Finish
                    )
                }
            }
        )

        InputCheckBox(
            label = string(StringsKeys.nullable),
            forceLabelWidth = true,
            isChecked = propertyDefinition.nullable,
            onCheckedChanged = {}, //TODO: Fin
        )

        Section(
            title = string(StringsKeys.supportedPlatformsForPropertyDefinitions),
            forceLabelWidth = false,
        ) {
            SupportedPlatforms(
                supportedPlatforms = propertyDefinition.supportedPlatforms,
                onCheckedChanged = { platformType -> } //TODO: Fin
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportedPlatforms(
    supportedPlatforms: ImmutableList<SupportedPlatform>,
    onCheckedChanged: (platformType: PlatformType) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        supportedPlatforms.forEach { supportedPlatform ->
            InputCheckBox(
                label = supportedPlatform.platformType.serialName,
                isChecked = supportedPlatform.isChecked,
                forceLabelWidth = false,
                onCheckedChanged = { onCheckedChanged(supportedPlatform.platformType) },
            )
        }
    }
}

private object SetupSchemaConstants {
    val allPropertyTypes = PropertyType.entries
}