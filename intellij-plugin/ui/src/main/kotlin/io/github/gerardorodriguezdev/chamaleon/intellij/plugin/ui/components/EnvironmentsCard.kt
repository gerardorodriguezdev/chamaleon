package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun EnvironmentCard(
    state: EnvironmentCardState,
    onSelectedEnvironmentChanged: (newSelectedEnvironment: String?) -> Unit,
) {
    Section {
        InputText(
            label = string(StringsKeys.environmentsDirectoryPath),
            text = state.environmentsDirectoryPath,
            modifier = Modifier.fillMaxWidth(),
        )

        InputTextDropdown(
            label = string(StringsKeys.selectedEnvironment),
            selectedValue = state.selectedEnvironment.orEmpty(),
            content = {
                state.environments.forEach { environment ->
                    item(
                        text = environment,
                        selected = environment == state.selectedEnvironment,
                        onClick = { onSelectedEnvironmentChanged(environment) },
                    )
                }
            },
            trailingIcon = {
                TooltipIconButton(
                    iconKey = AllIconsKeys.Actions.Close,
                    tooltip = string(StringsKeys.removeSelectedEnvironment),
                    onClick = { onSelectedEnvironmentChanged(null) },
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

data class EnvironmentCardState(
    val environmentsDirectoryPath: String,
    val selectedEnvironment: String?,
    val environments: ImmutableList<String>,
)