package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.entities.PropertyType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupSchemaAction
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowAction.SetupSchemaAction.*
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupSchemaState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.CreateEnvironmentWindowState.SetupSchemaState.PropertyDefinition
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupSchemaConstants.allPlatformTypes
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupSchemaConstants.allPropertyTypes
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupSchemaWindow(
    state: SetupSchemaState,
    onAction: (action: SetupSchemaAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    WindowContainer(
        modifier = modifier,
        toolbar = { Toolbar(title = state.title, forceLabelWidth = false) },
        content = {
            supportedPlatformSection(
                supportedPlatforms = state.supportedPlatforms,
                onSupportedPlatformChanged = { newSupportedPlatform ->
                    onAction(OnSupportedPlatformChanged(newSupportedPlatform))
                },
            )

            propertyDefinitionsSection(
                propertyDefinitions = state.propertyDefinitions,
                onAddPropertyDefinitionClicked = {
                    onAction(OnAddPropertyDefinitionClicked)
                },
                onPropertyNameChanged = { index, newName ->
                    onAction(OnPropertyNameChanged(index, newName))
                },
                onPropertyTypeChanged = { index, newPropertyType ->
                    onAction(OnPropertyTypeChanged(index, newPropertyType))
                },
                onNullableChanged = { index, newValue ->
                    onAction(OnNullableChanged(index, newValue))
                },
                onSupportedPlatformChanged = { index, newSupportedPlatform ->
                    onAction(OnPropertyDefinitionSupportedPlatformChanged(index, newSupportedPlatform))
                },
            )
        }
    )
}

private fun LazyListScope.supportedPlatformSection(
    supportedPlatforms: ImmutableList<PlatformType>,
    onSupportedPlatformChanged: (newPlatformType: PlatformType) -> Unit,
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
@Suppress("LongParameterList")
private fun LazyListScope.propertyDefinitionsSection(
    propertyDefinitions: ImmutableList<PropertyDefinition>,
    onAddPropertyDefinitionClicked: () -> Unit,
    onPropertyNameChanged: (index: Int, newName: String) -> Unit,
    onPropertyTypeChanged: (index: Int, newPropertyType: PropertyType) -> Unit,
    onNullableChanged: (index: Int, newValue: Boolean) -> Unit,
    onSupportedPlatformChanged: (index: Int, platformType: PlatformType) -> Unit,
) {
    stickyHeader {
        PropertyDefinitionSectionTitle(onAddPropertyDefinitionClicked = onAddPropertyDefinitionClicked)
    }

    itemsIndexed(propertyDefinitions) { index, propertyDefinition ->
        PropertyDefinitionSectionCard(
            propertyDefinition = propertyDefinition,
            onPropertyNameChanged = { newName -> onPropertyNameChanged(index, newName) },
            onPropertyTypeChanged = { newPropertyType -> onPropertyTypeChanged(index, newPropertyType) },
            onNullableChanged = { newValue -> onNullableChanged(index, newValue) },
            onSupportedPlatformChanged = { newPlatformType -> onSupportedPlatformChanged(index, newPlatformType) },
        )
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
    onPropertyNameChanged: (newName: String) -> Unit,
    onPropertyTypeChanged: (newPropertyType: PropertyType) -> Unit,
    onNullableChanged: (newValue: Boolean) -> Unit,
    onSupportedPlatformChanged: (newPlatformType: PlatformType) -> Unit,
) {
    Section(enableDivider = true) {
        InputTextField(
            label = string(StringsKeys.propertyName),
            value = propertyDefinition.name,
            onValueChange = onPropertyNameChanged,
        )

        InputTextDropdown(
            label = string(StringsKeys.propertyType),
            selectedValue = propertyDefinition.propertyType.name.lowercase(),
            content = {
                allPropertyTypes.forEach { propertyType ->
                    item(
                        selected = propertyDefinition.propertyType == propertyType,
                        text = propertyType.name.lowercase(),
                        onClick = { onPropertyTypeChanged(propertyType) }
                    )
                }
            }
        )

        InputCheckBox(
            label = string(StringsKeys.nullable),
            forceLabelWidth = true,
            isChecked = propertyDefinition.nullable,
            onCheckedChanged = onNullableChanged,
        )

        Section(
            title = string(StringsKeys.supportedPlatformsForPropertyDefinitions),
            forceLabelWidth = false,
        ) {
            SupportedPlatforms(
                supportedPlatforms = propertyDefinition.supportedPlatforms,
                onCheckedChanged = onSupportedPlatformChanged,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportedPlatforms(
    supportedPlatforms: ImmutableList<PlatformType>,
    onCheckedChanged: (newPlatformType: PlatformType) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        allPlatformTypes.forEach { platformType ->
            InputCheckBox(
                label = platformType.serialName,
                isChecked = supportedPlatforms.contains(platformType),
                forceLabelWidth = false,
                onCheckedChanged = {
                    onCheckedChanged(platformType)
                },
            )
        }
    }
}

private object SetupSchemaConstants {
    val allPlatformTypes = PlatformType.entries
    val allPropertyTypes = PropertyType.entries
}