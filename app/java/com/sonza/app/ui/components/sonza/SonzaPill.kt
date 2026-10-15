package com.sonza.app.ui.components.sonza

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.theme.SonzaTypography

/**
 * Premium Sonza Pill for category chips, filter tags, and quick actions.
 */
@Composable
fun SonzaPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    leadingIcon: ImageVector? = null,
    accentColor: Color = SonzaDesignTokens.Colors.BrandAccent,
    enabled: Boolean = true
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = 0.22f)
            else -> SonzaDesignTokens.Colors.GlassBackgroundLight
        },
        animationSpec = tween(200),
        label = "pillBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = 0.55f)
            else -> SonzaDesignTokens.Colors.GlassBorderSubtle
        },
        animationSpec = tween(200),
        label = "pillBorderColor"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor
            else -> SonzaDesignTokens.Colors.TextPrimary
        },
        animationSpec = tween(200),
        label = "pillTextColor"
    )

    Box(
        modifier = modifier
            .clip(SonzaDesignTokens.Radii.PillShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, SonzaDesignTokens.Radii.PillShape)
            .bounceClick(
                scaleDown = 0.95f,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                style = SonzaTypography.BodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = textColor
            )
        }
    }
}
