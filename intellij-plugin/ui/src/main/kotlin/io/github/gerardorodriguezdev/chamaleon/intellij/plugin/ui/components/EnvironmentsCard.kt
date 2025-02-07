package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun EnvironmentCard(
    state: EnvironmentCardState,
    onSelectedEnvironmentChanged: (newSelectedEnvironment: String?) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            //TODO: To label?
            Text(text = string(StringsKeys.environmentsDirectoryPath), modifier = Modifier.width(150.dp))
            Text(text = state.environmentsDirectoryPath, modifier = Modifier.weight(1f))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            //TODO: To label?
            Text(text = string(StringsKeys.selectedEnvironment), modifier = Modifier.width(150.dp))

            //TODO: TextDropdown
            Dropdown(
                menuContent = {
                    state.environments.forEach { environment ->
                        selectableItem(
                            selected = environment == state.selectedEnvironment,
                            onClick = { onSelectedEnvironmentChanged(environment) }
                        ) {
                            Text(environment)
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(state.selectedEnvironment.orEmpty())
            }

            TooltipIconButton(
                iconKey = AllIconsKeys.Actions.Close,
                tooltip = string(StringsKeys.removeSelectedEnvironment),
                onClick = { onSelectedEnvironmentChanged(null) },
            )
        }
    }
}

data class EnvironmentCardState(
    val environmentsDirectoryPath: String,
    val selectedEnvironment: String?,
    val environments: ImmutableList<String>,
)