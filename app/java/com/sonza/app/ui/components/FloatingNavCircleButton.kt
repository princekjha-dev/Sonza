package com.sonza.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SonzaOnSurface
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant

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
        targetValue = if (isSelected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.90f),
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "circleNavBg"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "circleNavBorder"
    )
    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else SonzaOnSurfaceVariant,
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "circleNavIcon"
    )

    val specularBrush = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = if (isSelected) 0.15f else 0.08f),
        0.5f to Color.Transparent,
        1.0f to Color.Black.copy(alpha = 0.15f)
    )

    Surface(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.40f),
                spotColor = Color.Black.copy(alpha = 0.30f)
            )
            .semantics {
                this.contentDescription = if (isSelected) "$contentDescription, selected" else contentDescription
                this.selected = isSelected
            },
        shape = CircleShape,
        color = animatedBgColor,
        border = BorderStroke(0.75.dp, animatedBorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(specularBrush)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = null,
                tint = animatedIconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

