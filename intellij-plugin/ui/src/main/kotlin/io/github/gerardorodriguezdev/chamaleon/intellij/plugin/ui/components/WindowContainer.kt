package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.horizontalPadding
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.scrollbarWidth
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.verticalPadding
import org.jetbrains.jewel.ui.component.VerticalScrollbar

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WindowContainer(
    modifier: Modifier = Modifier,
    toolbar: @Composable (ColumnScope.() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    Column(modifier) {
        toolbar?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            ) {
                toolbar()
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            val lazyListState = rememberLazyListState()

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    top = if (toolbar != null) 0.dp else verticalPadding,
                    bottom = verticalPadding,
                    start = horizontalPadding,
                    end = scrollbarWidth
                ),
                verticalArrangement = Arrangement.spacedBy(itemsSpacing),
                modifier = Modifier.fillMaxSize()
            ) {
                content()
            }

            VerticalScrollbar(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                scrollState = lazyListState,
            )
        }
    }
}