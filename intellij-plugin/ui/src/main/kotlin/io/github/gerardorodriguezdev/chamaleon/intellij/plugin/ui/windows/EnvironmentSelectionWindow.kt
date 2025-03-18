package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.shared.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun EnvironmentSelectionWindow(
    state: EnvironmentSelectionWindowState,
    onRefresh: () -> Unit,
    onCreateProject: () -> Unit,
    onSelectedEnvironmentChanged: (index: Int, newSelectedEnvironment: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> LoadingWindow(modifier = modifier)
        state.environmentCardStates.isEmpty() -> EmptyWindow(modifier = modifier)
        else -> ContentWindow(
            modifier = modifier,
            gradlePluginVersionUsed = state.gradlePluginVersionUsed,
            notificationErrorMessage = state.notificationErrorMessage,
            environmentCardStates = state.environmentCardStates,
            onRefresh = onRefresh,
            onCreateProject = onCreateProject,
            onSelectedEnvironmentChanged = onSelectedEnvironmentChanged,
        )
    }
}

@Composable
private fun EmptyWindow(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = string(StringsKeys.noEnvironmentsFound),
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun ContentWindow(
    gradlePluginVersionUsed: String,
    notificationErrorMessage: String?,
    environmentCardStates: ImmutableList<EnvironmentCardState>,
    onRefresh: () -> Unit,
    onCreateProject: () -> Unit,
    onSelectedEnvironmentChanged: (index: Int, newSelectedEnvironment: String?) -> Unit,
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
                        onClick = onCreateProject,
                    )

                    notificationErrorMessage?.let {
                        TooltipIcon(
                            iconKey = AllIconsKeys.Nodes.ErrorIntroduction,
                            tooltip = notificationErrorMessage,
                        )
                    }

                    TooltipIcon(
                        iconKey = AllIconsKeys.General.Information,
                        tooltip = string(StringsKeys.gradlePluginVersionUsed(gradlePluginVersionUsed)),
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
    onSelectedEnvironmentChanged: (index: Int, newSelectedEnvironment: String?) -> Unit,
) {
    itemsIndexed(
        items = environmentCardStates,
        key = { index, environmentCardState -> environmentCardState.environmentsDirectoryPath },
    ) { index, environmentCardState ->
        EnvironmentCard(
            state = environmentCardState,
            onSelectedEnvironmentChanged = { newSelectedEnvironment ->
                onSelectedEnvironmentChanged(
                    index,
                    newSelectedEnvironment,
                )
            }
        )
    }
}

data class EnvironmentSelectionWindowState(
    val gradlePluginVersionUsed: String,
    val isLoading: Boolean = true,
    val notificationErrorMessage: String? = null,
    val environmentCardStates: ImmutableList<EnvironmentCardState> = persistentListOf(),
)