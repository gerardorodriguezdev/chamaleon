package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun EnvironmentSelectionWindow(
    state: EnvironmentSelectionState,
    onRefresh: () -> Unit,
    onCreateEnvironment: () -> Unit,
    onSelectedEnvironmentChanged: (environmentsDirectoryPath: String, newSelectedEnvironment: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> LoadingWindow(modifier = modifier)
        state.environmentCardStates.isEmpty() -> EmptyWindow()
        else -> ContentWindow(
            modifier = modifier,
            gradlePluginVersionUsed = state.gradlePluginVersionUsed,
            notificationErrorMessage = state.notificationErrorMessage,
            environmentCardStates = state.environmentCardStates,
            onRefresh = onRefresh,
            onCreateEnvironment = onCreateEnvironment,
            onSelectedEnvironmentChanged = onSelectedEnvironmentChanged,
        )
    }
}

@Composable
private fun EmptyWindow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = string(StringsKeys.noEnvironmentsFound),
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContentWindow(
    gradlePluginVersionUsed: String,
    notificationErrorMessage: String?,
    environmentCardStates: ImmutableList<EnvironmentCardState>,
    onRefresh: () -> Unit,
    onCreateEnvironment: () -> Unit,
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
                        onClick = onRefresh,
                    )

                    TooltipIconButton(
                        iconKey = AllIconsKeys.Actions.AddFile,
                        tooltip = string(StringsKeys.createEnvironment),
                        onClick = onCreateEnvironment,
                    )

                    notificationErrorMessage?.let {
                        TooltipIcon(
                            iconKey = AllIconsKeys.Nodes.ErrorIntroduction,
                            tooltip = notificationErrorMessage,
                        )
                    }

                    TooltipIcon(
                        iconKey = AllIconsKeys.General.Information,
                        tooltip = "${string(StringsKeys.gradlePluginVersionUsed)} $gradlePluginVersionUsed",
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
    val gradlePluginVersionUsed: String, //TODO: Update
    val isLoading: Boolean = true,
    val notificationErrorMessage: String? = null,
    val environmentCardStates: ImmutableList<EnvironmentCardState> = persistentListOf(),
)