package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.itemsSpacing

@Composable
internal fun InputTextDropdown(
    label: String,
    selectedValue: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    content: TextDropdownScope.() -> Unit,
) {
    InputContainer(label = label, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
        ) {
            TextDropdown(
                selectedValue = selectedValue,
                content = content,
            )

            trailingIcon?.invoke()
        }
    }
}