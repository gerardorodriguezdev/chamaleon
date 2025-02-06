package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.core.entities.PlatformType
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.LocalStrings
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.InputCheckBox
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupSchemaConstants.allPlatforms
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState.PropertyDefinition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupSchemaWindow(
    state: SetupSchemaState,
    onSupportedPlatformsChanged: (platformType: PlatformType) -> Unit,
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
    section(title = { LocalStrings.current.supportedPlatforms }) {
        item {
            SupportedPlatforms(
                supportedPlatforms = supportedPlatforms,
                onCheckedChanged = onCheckedChanged,
            )
        }
    }
}

private fun LazyListScope.propertyDefinitionsSection(propertyDefinitions: ImmutableList<PropertyDefinition>) {
    section(title = { LocalStrings.current.propertyDefinitions }) {
        items(propertyDefinitions) { propertyDefinition ->

        }
    }
}

private fun LazyListScope.section(title: @Composable () -> String, content: LazyListScope.() -> Unit) {
    item {
        Divider(orientation = Orientation.Horizontal)
    }

    item {
        Text(text = title())
    }

    content()
}

@Composable
private fun SupportedPlatforms(
    supportedPlatforms: ImmutableList<PlatformType>,
    onCheckedChanged: (platformType: PlatformType) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(allPlatforms) { platformType ->
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
}