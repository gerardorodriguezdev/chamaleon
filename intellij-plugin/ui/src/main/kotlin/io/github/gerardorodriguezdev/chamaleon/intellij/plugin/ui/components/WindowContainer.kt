package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.horizontalPadding
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.scrollbarWidth
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.verticalPadding
import org.jetbrains.jewel.ui.component.VerticalScrollbar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WindowContainer(
    toolbar: @Composable (BoxScope.() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val lazyListState = rememberLazyListState()

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                top = verticalPadding,
                bottom = verticalPadding,
                start = horizontalPadding,
                end = scrollbarWidth
            ),
            verticalArrangement = Arrangement.spacedBy(itemsSpacing),
            modifier = Modifier.fillMaxSize()
        ) {
            toolbar?.let {
                stickyHeader {
                    Box(modifier = Modifier.padding(vertical = verticalPadding)) {
                        toolbar()
                    }
                }
            }

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