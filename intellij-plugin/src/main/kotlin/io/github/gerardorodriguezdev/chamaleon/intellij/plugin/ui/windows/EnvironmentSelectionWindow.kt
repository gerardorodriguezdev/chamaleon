package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.messages.Bundle
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.EnvironmentCard
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components.EnvironmentCardState
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionConstants.horizontalPadding
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.windows.EnvironmentSelectionConstants.verticalScrollBarWidth
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@OptIn(ExperimentalJewelApi::class)
@Composable
fun EnvironmentSelectionWindow(
    state: EnvironmentSelectionState,
    onRefreshClicked: () -> Unit,
    onCreateEnvironmentClicked: () -> Unit,
    onSelectedEnvironmentChanged: (environmentsDirectoryPath: String, newSelectedEnvironment: String?) -> Unit,
) {
    SwingBridgeTheme {
        if (state.isLoading) {
            Loading()
        } else {
            Content(
                environmentCardStates = state.environmentCardStates,
                onRefreshClicked = onRefreshClicked,
                onCreateEnvironmentClicked = onCreateEnvironmentClicked,
                onSelectedEnvironmentChanged = onSelectedEnvironmentChanged,
            )
        }
    }
}

@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun Content(
    environmentCardStates: ImmutableList<EnvironmentCardState>,
    onRefreshClicked: () -> Unit,
    onCreateEnvironmentClicked: () -> Unit,
    onSelectedEnvironmentChanged: (environmentsDirectoryPath: String, newSelectedEnvironment: String?) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 2.dp)
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

@OptIn(ExperimentalFoundationApi::class)
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
        //TODO: Refactor
        Tooltip(
            tooltip = {
                Text(text = Bundle.refreshEnvironments)
            },
            content = {
                IconActionButton(
                    key = AllIconsKeys.Actions.Refresh,
                    onClick = onRefreshClicked,
                    contentDescription = Bundle.refreshEnvironments,
                )
            })

        //TODO: Refactor
        Tooltip(
            tooltip = {
                Text(text = Bundle.createEnvironment)
            },
            content = {
                IconActionButton(
                    key = AllIconsKeys.Actions.AddFile,
                    onClick = onCreateEnvironmentClicked,
                    contentDescription = remember { Bundle.createEnvironment },
                )
            }
        )
    }
}

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

        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            scrollState = lazyListState,
        )
    }
}

object EnvironmentSelectionConstants {
    val horizontalPadding = 12.dp
    val verticalScrollBarWidth = 12.dp
}

data class EnvironmentSelectionState(
    val environmentCardStates: ImmutableList<EnvironmentCardState> = persistentListOf(),
    val isLoading: Boolean = false,
)