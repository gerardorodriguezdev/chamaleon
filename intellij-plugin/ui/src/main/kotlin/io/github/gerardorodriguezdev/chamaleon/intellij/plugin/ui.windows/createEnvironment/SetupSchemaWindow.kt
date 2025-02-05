package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.SetupSchemaConstants.platformTypes
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.jewel.ui.component.Text

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupSchema(
    state: SetupSchemaState,
    onCheckedChanged: (platformType: PlatformType) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        stickyHeader {
            Title(stateProvider = { state })
        }

        platforms(
            supportedPlatformsProvider = { state.supportedPlatforms },
            onCheckedChanged = onCheckedChanged,
        )
    }
}

@Composable
private fun Title(stateProvider: () -> SetupSchemaState) {
    val schemaExists by remember {
        derivedStateOf {
            val state = stateProvider()
            state.supportedPlatforms.isNotEmpty() && state.propertyDefinitions.isNotEmpty()
        }
    }
    val strings = LocalStrings.current
    val text = if (schemaExists) strings.updateSchema else strings.createSchema
    Text(text = text)
}

private fun LazyListScope.platforms(
    supportedPlatformsProvider: () -> ImmutableSet<PlatformType>,
    onCheckedChanged: (platformType: PlatformType) -> Unit,
) {
    item {
        Text(text = LocalStrings.current.supportedPlatforms)
    }

    items(platformTypes) { platformType ->
        val isChecked by remember {
            derivedStateOf {
                val supportedPlatforms = supportedPlatformsProvider()
                supportedPlatforms.contains(platformType)
            }
        }

        InputCheckBox(
            label = platformType.serialName,
            isChecked = isChecked,
            onCheckedChanged = { onCheckedChanged(platformType) }
        )
    }
}

private object SetupSchemaConstants {
    val platformTypes = PlatformType.entries.toPersistentList()
}