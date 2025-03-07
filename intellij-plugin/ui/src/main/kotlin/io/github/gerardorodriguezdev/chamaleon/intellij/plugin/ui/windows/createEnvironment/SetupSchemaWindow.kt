package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.core.models.PlatformType
import io.github.gerardorodriguezdev.chamaleon.core.models.PropertyType
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
            globalSupportedPlatformSection(
                globalSupportedPlatforms = state.globalSupportedPlatforms,
                onSupportedPlatformChanged = { isChecked, newSupportedPlatform ->
                    onAction(OnSupportedPlatformChanged(isChecked, newSupportedPlatform))
                },
            )

            propertyDefinitionsSection(
                propertyDefinitions = state.propertyDefinitions,
                onAddPropertyDefinition = {
                    onAction(OnAddPropertyDefinition)
                },
                onPropertyNameChanged = { index, newName ->
                    onAction(OnPropertyNameChanged(index, newName))
                },
                onDeletePropertyDefinition = { index ->
                    onAction(OnDeletePropertyDefinition(index))
                },
                onPropertyTypeChanged = { index, newPropertyType ->
                    onAction(OnPropertyTypeChanged(index, newPropertyType))
                },
                onNullableChanged = { index, newValue ->
                    onAction(OnNullableChanged(index, newValue))
                },
                onSupportedPlatformChanged = { index, isChecked, newSupportedPlatform ->
                    onAction(OnPropertyDefinitionSupportedPlatformChanged(index, isChecked, newSupportedPlatform))
                },
            )
        }
    )
}

private fun LazyListScope.globalSupportedPlatformSection(
    globalSupportedPlatforms: ImmutableList<PlatformType>,
    onSupportedPlatformChanged: (isChecked: Boolean, newPlatformType: PlatformType) -> Unit,
) {
    item {
        Section(
            title = string(StringsKeys.supportedPlatforms),
            enableDivider = true,
            forceLabelWidth = false,
        ) {
            SupportedPlatforms(
                supportedPlatforms = globalSupportedPlatforms,
                onCheckedChanged = onSupportedPlatformChanged,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongParameterList")
private fun LazyListScope.propertyDefinitionsSection(
    propertyDefinitions: ImmutableList<PropertyDefinition>,
    onAddPropertyDefinition: () -> Unit,
    onPropertyNameChanged: (index: Int, newName: String) -> Unit,
    onDeletePropertyDefinition: (index: Int) -> Unit,
    onPropertyTypeChanged: (index: Int, newPropertyType: PropertyType) -> Unit,
    onNullableChanged: (index: Int, newValue: Boolean) -> Unit,
    onSupportedPlatformChanged: (index: Int, isChecked: Boolean, platformType: PlatformType) -> Unit,
) {
    stickyHeader {
        PropertyDefinitionSectionTitle(onAddPropertyDefinition = onAddPropertyDefinition)
    }

    itemsIndexed(propertyDefinitions) { index, propertyDefinition ->
        PropertyDefinitionSectionCard(
            propertyDefinition = propertyDefinition,
            onPropertyNameChanged = { newName -> onPropertyNameChanged(index, newName) },
            onDeletePropertyDefinition = { onDeletePropertyDefinition(index) },
            onPropertyTypeChanged = { newPropertyType -> onPropertyTypeChanged(index, newPropertyType) },
            onNullableChanged = { newValue -> onNullableChanged(index, newValue) },
            onSupportedPlatformChanged = { isChecked, newPlatformType ->
                onSupportedPlatformChanged(
                    index,
                    isChecked,
                    newPlatformType
                )
            },
        )
    }
}

@Composable
private fun PropertyDefinitionSectionTitle(onAddPropertyDefinition: () -> Unit) {
    Section(
        title = string(StringsKeys.propertyDefinitions),
        titleTrailingIcon = {
            TooltipIconButton(
                iconKey = AllIconsKeys.General.Add,
                tooltip = string(StringsKeys.addPropertyDefinitions),
                onClick = onAddPropertyDefinition,
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
    onDeletePropertyDefinition: () -> Unit,
    onPropertyTypeChanged: (newPropertyType: PropertyType) -> Unit,
    onNullableChanged: (newValue: Boolean) -> Unit,
    onSupportedPlatformChanged: (isChecked: Boolean, newPlatformType: PlatformType) -> Unit,
) {
    Section(enableDivider = true) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
            modifier = Modifier.fillMaxWidth(),
        ) {
            InputTextField(
                modifier = Modifier.weight(1f),
                label = string(StringsKeys.propertyName),
                value = propertyDefinition.nameField.value,
                onValueChange = onPropertyNameChanged,
                trailingIcon = {
                    propertyDefinition.nameField.verification?.let {
                        VerificationIcon(verification = propertyDefinition.nameField.verification)
                    }
                }
            )

            TooltipIconButton(
                iconKey = AllIconsKeys.Actions.Close,
                tooltip = string(StringsKeys.deletePropertyDefinition),
                onClick = onDeletePropertyDefinition,
            )
        }

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
    onCheckedChanged: (isChecked: Boolean, newPlatformType: PlatformType) -> Unit
) {
    FlowRow(
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        allPlatformTypes.forEach { platformType ->
            InputCheckBox(
                label = platformType.serialName,
                isChecked = supportedPlatforms.contains(platformType),
                forceLabelWidth = false,
                onCheckedChanged = { isChecked ->
                    onCheckedChanged(isChecked, platformType)
                },
            )
        }
    }
}

private object SetupSchemaConstants {
    val allPlatformTypes = PlatformType.entries
    val allPropertyTypes = PropertyType.entries
}