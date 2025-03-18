package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.MenuScope
import org.jetbrains.jewel.ui.component.Text

@Composable
internal fun TextDropdown(
    selectedValue: String,
    modifier: Modifier = Modifier,
    content: TextDropdownScope.() -> Unit,
) {
    Dropdown(
        menuContent = {
            TextDropdownScopeInstance(this).content()
        },
        modifier = modifier,
    ) {
        Text(text = selectedValue)
    }
}

private class TextDropdownScopeInstance(
    private val scope: MenuScope,
) : TextDropdownScope {
    override fun item(text: String, selected: Boolean, onClick: () -> Unit) {
        scope.selectableItem(selected = selected, onClick = onClick) {
            Text(text = text)
        }
    }
}

internal interface TextDropdownScope {
    fun item(text: String, selected: Boolean, onClick: () -> Unit)
}