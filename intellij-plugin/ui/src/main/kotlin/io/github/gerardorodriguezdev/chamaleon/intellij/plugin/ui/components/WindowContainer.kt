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
import org.jetbrains.jewel.ui.component.VerticalScrollbar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WindowContainer(
    toolbar: @Composable (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val lazyListState = rememberLazyListState()

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                bottom = 16.dp,
                start = 12.dp,
                end = 12.dp - 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 12.dp)
        ) {
            toolbar?.let {
                stickyHeader {
                    toolbar()
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