package com.sonza.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.components.glass.LiquidGlassSurface
import com.sonza.app.ui.components.glass.LiquidGlassTokens
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant

/**
 * Independent circular floating navigation button designed for Sonza's
 * 3-part floating bottom navigation system ([ Home ○ ] [ Mini Player ] [ Search ○ ]).
 * Features physical Liquid Glass rendering, dynamic ambient lighting, and spring press micro-interaction.
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = LiquidGlassTokens.Springs.Interactive,
        label = "floatingCirclePressScale"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) {
            accentColor
        } else {
            SonzaOnSurfaceVariant.copy(alpha = 0.85f)
        },
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "circleNavIcon"
    )

    val ambientTint = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainer

    Box(
        modifier = modifier
            .size(size)
            .scale(pressScale)
            .semantics {
                this.contentDescription = if (isSelected) "$contentDescription, selected" else contentDescription
                this.selected = isSelected
            }
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            ambientTint = ambientTint,
            elevation = if (isSelected) 10.dp else 6.dp,
            blurRadius = 24.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Tab,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle ambient glow behind the active icon
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.35f),
                                        accentColor.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Icon(
                    imageVector = if (isSelected) selectedIcon else icon,
                    contentDescription = null,
                    tint = animatedIconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


