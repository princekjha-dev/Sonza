package com.sonza.app.ui.components.sonza

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.theme.SonzaTypography

/**
 * Standardized Section Header as specified in DESIGN_SYSTEM.md Part 2.
 * Used across Home, Library, Search, and Playlist screens for consistent hierarchy.
 */
@Composable
fun SonzaSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    accentColor: Color = SonzaDesignTokens.Colors.BrandAccent
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SonzaDesignTokens.Spacing.SpaceLg,
                vertical = SonzaDesignTokens.Spacing.SpaceSm
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!kicker.isNullOrBlank()) {
                Text(
                    text = kicker.uppercase(),
                    style = SonzaTypography.Kicker.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
            Text(
                text = title,
                style = SonzaTypography.SectionTitle.copy(
                    color = SonzaDesignTokens.Colors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Text(
                text = actionText,
                style = SonzaTypography.BodySmall.copy(
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )
        }
    }
}
