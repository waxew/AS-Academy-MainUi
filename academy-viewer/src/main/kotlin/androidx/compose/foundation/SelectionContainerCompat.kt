package androidx.compose.foundation

import androidx.compose.foundation.text.selection.SelectionContainer as ComposeSelectionContainer
import androidx.compose.runtime.Composable

/**
 * Compatibility bridge for the viewer's historical SelectionContainer import.
 * Compose exposes this API from foundation.text.selection; keeping the bridge isolated
 * lets the reader compile without changing the screen contract while the viewer evolves.
 */
@Composable
fun SelectionContainer(content: @Composable () -> Unit) {
    ComposeSelectionContainer(content = content)
}
