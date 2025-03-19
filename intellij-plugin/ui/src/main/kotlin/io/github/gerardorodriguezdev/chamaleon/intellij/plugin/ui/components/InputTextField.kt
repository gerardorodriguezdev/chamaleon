package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.theme.ThemeConstants.labelWidth
import io.github.gerardorodriguezdev.chamaleon.intellij.plugin.ui.models.Field
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Tooltip

@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongParameterList")
@Composable
internal fun InputTextField(
    label: String,
    field: Field<String>,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (newText: String) -> Unit = {},
) {
    val invalidVerification = field.verification?.asInvalid()
    val tooltip = invalidVerification?.reason ?: label
    val outline = if (invalidVerification != null) Outline.Error else Outline.None

    Tooltip(
        modifier = modifier,
        tooltip = {
            Text(text = tooltip)
        },
        content = {
            InputContainer(label = label) {
                TextField(
                    value = field.value,
                    trailingIcon = trailingIcon,
                    readOnly = readOnly,
                    outline = outline,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .widthIn(min = labelWidth)
                        .weight(weight = 1f, fill = false),
                )
            }
        },
    )
}