package com.sonza.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.theme.MotionTokens

/**
 * Small circular floating navigation button designed to accompany the Mini Player
 * in Sonza's compact floating glass navigation system.
 */
@Composable
fun FloatingNavCircleButton(
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    contentDescription: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "circleNavBg"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "circleNavBorder"
    )
    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "circleNavIcon"
    )

    Surface(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.40f),
                spotColor = Color.Black.copy(alpha = 0.30f)
            ),
        shape = CircleShape,
        color = animatedBgColor,
        border = BorderStroke(0.75.dp, animatedBorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = contentDescription,
                tint = animatedIconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
