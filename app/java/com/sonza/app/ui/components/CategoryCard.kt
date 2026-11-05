package com.sonza.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sonza.app.core.model.BrowseCategory

/**
 * Category card component forwarding to [BrowseCategoryCard] to provide
 * unified image-driven discovery card visuals throughout the app.
 */
@Composable
fun CategoryCard(
    category: BrowseCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BrowseCategoryCard(
        category = category,
        onClick = onClick,
        modifier = modifier
    )
}
