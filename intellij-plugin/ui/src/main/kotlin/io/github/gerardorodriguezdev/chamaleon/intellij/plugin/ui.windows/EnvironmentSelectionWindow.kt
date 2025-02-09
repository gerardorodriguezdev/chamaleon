package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun EnvironmentSelectionWindow(
    state: EnvironmentSelectionState,
    onRefreshClicked: () -> Unit,
    onCreateEnvironmentClicked: () -> Unit,
    onSelectedEnvironmentChanged: (environmentsDirectoryPath: String, newSelectedEnvironment: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        LoadingWindow(modifier = modifier)
    } else {
        ContentWindow(
            modifier = modifier,
            environmentCardStates = state.environmentCardStates,
            onRefreshClicked = onRefreshClicked,
            onCreateEnvironmentClicked = onCreateEnvironmentClicked,
            onSelectedEnvironmentChanged = onSelectedEnvironmentChanged,
        )
    }
}

@Composable
private fun ContentWindow(
    environmentCardStates: ImmutableList<EnvironmentCardState>,
    onRefreshClicked: () -> Unit,
    onCreateEnvironmentClicked: () -> Unit,
    onSelectedEnvironmentChanged: (environmentsDirectoryPath: String, newSelectedEnvironment: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    WindowContainer(
        modifier = modifier,
        toolbar = {
            Toolbar(
                forceLabelWidth = false,
                trailingIcons = {
                    TooltipIconButton(
                        iconKey = AllIconsKeys.Actions.Refresh,
                        tooltip = string(StringsKeys.refreshEnvironments),
                        onClick = onRefreshClicked,
                    )

                    TooltipIconButton(
                        iconKey = AllIconsKeys.Actions.AddFile,
                        tooltip = string(StringsKeys.createEnvironment),
                        onClick = onCreateEnvironmentClicked,
                    )
                }
            )
        },
        content = {
            environmentCards(
                environmentCardStates = environmentCardStates,
                onSelectedEnvironmentChanged = onSelectedEnvironmentChanged,
            )
        }
    )
}

private fun LazyListScope.environmentCards(
    environmentCardStates: ImmutableList<EnvironmentCardState>,
    onSelectedEnvironmentChanged: (environmentsDirectoryPath: String, newSelectedEnvironment: String?) -> Unit,
) {
    items(
        items = environmentCardStates,
        key = { environmentCardState -> environmentCardState.environmentsDirectoryPath },
    ) { environmentCardState ->
        EnvironmentCard(
            state = environmentCardState,
            onSelectedEnvironmentChanged = { newSelectedEnvironment ->
                onSelectedEnvironmentChanged(
                    environmentCardState.environmentsDirectoryPath,
                    newSelectedEnvironment,
                )
            }
        )
    }
}

data class EnvironmentSelectionState(
    val environmentCardStates: ImmutableList<EnvironmentCardState> = persistentListOf(),
    val isLoading: Boolean = false,
)