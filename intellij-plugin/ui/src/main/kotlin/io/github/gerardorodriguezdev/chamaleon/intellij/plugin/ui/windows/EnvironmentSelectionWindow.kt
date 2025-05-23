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

@Suppress("LongParameterList")
@Composable
fun EnvironmentSelectionWindow(
    state: EnvironmentSelectionWindowState,
    onRefresh: () -> Unit,
    onCreateProject: () -> Unit,
    onSelectedEnvironmentChanged: (index: Int, newSelectedEnvironment: String?) -> Unit,
    onSelectEnvironment: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        LoadingWindow(modifier = modifier)
    } else {
        ContentWindow(
            modifier = modifier,
            gradlePluginVersionUsed = state.gradlePluginVersionUsed,
            notificationErrorMessage = state.notificationErrorMessage,
            environmentCardStates = state.environmentCardStates,
            onRefresh = onRefresh,
            onCreateProject = onCreateProject,
            onSelectedEnvironmentChanged = onSelectedEnvironmentChanged,
            onSelectEnvironment = onSelectEnvironment,
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
    onSelectEnvironment: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    WindowContainer(
        modifier = modifier,
        toolbar = {
            Toolbar(
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
            if (environmentCardStates.isNotEmpty()) {
                environmentCards(
                    environmentCardStates = environmentCardStates,
                    onSelectedEnvironmentChanged = onSelectedEnvironmentChanged,
                    onSelectEnvironment = onSelectEnvironment,
                )
            } else {
                emptyText()
            }
        }
    )
}

private fun LazyListScope.emptyText() {
    item {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = string(StringsKeys.noEnvironmentsFound),
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun LazyListScope.environmentCards(
    environmentCardStates: ImmutableList<EnvironmentCardState>,
    onSelectedEnvironmentChanged: (index: Int, newSelectedEnvironment: String?) -> Unit,
    onSelectEnvironment: (index: Int) -> Unit,
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
            },
            onSelectEnvironment = { onSelectEnvironment(index) },
        )
    }
}

data class EnvironmentSelectionWindowState(
    val gradlePluginVersionUsed: String,
    val isLoading: Boolean = true,
    val notificationErrorMessage: String? = null,
    val environmentCardStates: ImmutableList<EnvironmentCardState> = persistentListOf(),
)