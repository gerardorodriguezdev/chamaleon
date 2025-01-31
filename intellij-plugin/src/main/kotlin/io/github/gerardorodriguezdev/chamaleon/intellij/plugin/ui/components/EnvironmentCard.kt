package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellij.util.ui.JBUI
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.messages.Bundle
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.jewel.bridge.toComposeColor
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
internal fun EnvironmentCard(
    state: EnvironmentCardState,
    onSelectedEnvironmentChanged: (newSelectedEnvironment: String?) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR.toComposeColor(),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = Bundle.environmentsDirectoryPath, modifier = Modifier.width(150.dp))
            Text(text = state.environmentsDirectoryPath, modifier = Modifier.weight(1f))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = Bundle.selectedEnvironment, modifier = Modifier.width(150.dp))

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
                tooltip = Bundle.removeSelectedEnvironment,
                onClick = { onSelectedEnvironmentChanged(null) },
            )
        }
    }
}

internal data class EnvironmentCardState(
    val environmentsDirectoryPath: String,
    val selectedEnvironment: String?,
    val environments: ImmutableList<String>,
)