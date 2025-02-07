package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.strings.StringsKeys
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.string
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.EnvironmentCard
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.EnvironmentCardState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.TooltipIconButton
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.WindowContainer
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionConstants.horizontalPadding
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionConstants.verticalScrollBarWidth
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.VerticalScrollbar
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun EnvironmentSelectionWindow(
    state: EnvironmentSelectionState,
    onRefreshClicked: () -> Unit,
    onCreateEnvironmentClicked: () -> Unit,
    onSelectedEnvironmentChanged: (environmentsDirectoryPath: String, newSelectedEnvironment: String?) -> Unit,
) {
    if (state.isLoading) {
        LoadingWindow()
    } else {
        ContentWindow(
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
) {
    WindowContainer {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Toolbar(
                onRefreshClicked = onRefreshClicked,
                onCreateEnvironmentClicked = onCreateEnvironmentClicked,
            )

            EnvironmentCards(
                environmentCardStates = environmentCardStates,
                onSelectedEnvironmentChanged = onSelectedEnvironmentChanged,
            )
        }
    }
}

@Composable
private fun Toolbar(
    onRefreshClicked: () -> Unit,
    onCreateEnvironmentClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
}

//TODO: Screen for VerticalScroller + Toolbar
@Composable
private fun ColumnScope.EnvironmentCards(
    environmentCardStates: ImmutableList<EnvironmentCardState>,
    onSelectedEnvironmentChanged: (environmentsDirectoryPath: String, newSelectedEnvironment: String?) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        val lazyListState = rememberLazyListState()

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                bottom = 16.dp,
                start = horizontalPadding,
                end = horizontalPadding - verticalScrollBarWidth
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = verticalScrollBarWidth)
        ) {
            items(
                items = environmentCardStates,
                key = { environmentCardState -> environmentCardState.environmentsDirectoryPath },
            ) { environmentCardState ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Divider(orientation = Orientation.Horizontal)

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
        }

        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            scrollState = lazyListState,
        )
    }
}

internal object EnvironmentSelectionConstants {
    val horizontalPadding = 12.dp
    val verticalScrollBarWidth = 12.dp
}

data class EnvironmentSelectionState(
    val environmentCardStates: ImmutableList<EnvironmentCardState> = persistentListOf(),
    val isLoading: Boolean = false,
)