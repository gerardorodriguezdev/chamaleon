package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.VerticalScrollbar

@OptIn(ExperimentalJewelApi::class)
@Composable
fun EnvironmentSelection(
    state: EnvironmentSelectionState,
    onRefreshClicked: () -> Unit,
    onSelectedEnvironmentChanged: (environmentPath: String, newSelectedEnvironment: String?) -> Unit,
) {
    SwingBridgeTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                EnvironmentCards(
                    environmentCardStates = state.environmentCardStates,
                    onEnvironmentChanged = onSelectedEnvironmentChanged,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.EnvironmentCards(
    environmentCardStates: ImmutableList<EnvironmentCardState>,
    onEnvironmentChanged: (environmentPath: String, newSelectedEnvironment: String?) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 12.dp)
    ) {
        items(
            items = environmentCardStates,
            key = { environmentCardState -> environmentCardState.environmentPath },
        ) { environmentCardState ->
            EnvironmentCard(
                state = environmentCardState,
                onEnvironmentChanged = { newSelectedEnvironment ->
                    onEnvironmentChanged(
                        environmentCardState.environmentPath,
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

data class EnvironmentSelectionState(
    val environmentCardStates: ImmutableList<EnvironmentCardState> = persistentListOf(),
    val isLoading: Boolean = false,
)