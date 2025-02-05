package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.LocalStrings
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.createEnvironment.State.SetupSchemaState
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SetupSchema(
    state: SetupSchemaState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Title(stateProvider = { state })
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