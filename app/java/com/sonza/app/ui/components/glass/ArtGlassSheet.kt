package com.sonza.app.ui.components.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A bottom sheet that frosts itself against the now-playing artwork.
 *
 * Sheets opened from the player used to paint themselves with an almost-opaque dominant
 * colour, which read as a flat slab in front of the player's blurred-art backdrop. Sharing
 * [ArtworkBlurBackdrop] here makes the sheet look like the same pane of glass, continuous
 * with the screen it slid up over.
 *
 * Away from the player [LocalGlassArtwork] is null and this degrades to an ordinary sheet,
 * so the same component is safe to use anywhere.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtGlassSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    fallbackContainerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    showDragHandle: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val artwork = LocalGlassArtwork.current

    if (artwork?.artworkUrl.isNullOrBlank()) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            sheetState = sheetState,
            shape = shape,
            containerColor = fallbackContainerColor,
            contentColor = contentColor,
            contentWindowInsets = { WindowInsets(0) },
            dragHandle = if (showDragHandle) ({ BottomSheetDefaults.DragHandle() }) else null,
            content = content
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = Color.Transparent,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = null
    ) {
        // The Column sizes the Box; the backdrop fills it via matchParentSize so the blur
        // covers exactly the sheet and nothing more.
        Box(modifier = Modifier.fillMaxWidth()) {
            ArtworkBlurBackdrop(
                artworkUrl = artwork!!.artworkUrl,
                isDarkTheme = artwork.isDarkTheme,
                dominantColors = artwork.colors,
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape),
                // A sheet sits closer to the eye than the screen behind it; a touch more
                // scrim keeps its own content readable against the same artwork.
                scrimAlpha = if (artwork.isDarkTheme) 0.72f else 0.60f
            )
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (showDragHandle) {
                        BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    content()
                }
            }
        }
    }
}
