package com.sonza.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.theme.*

/**
 * Standard Sonza Empty State per DESIGN_SYSTEM.md Part 6.7.
 * Replaces raw missing-result text with an intentional, premium design-system component.
 */
@Composable
fun SonzaEmptyState(
    title: String,
    description: String? = null,
    icon: ImageVector = Icons.Rounded.SearchOff,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dynamicColors = LocalSonzaDynamicColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(RadiusTokens.Lg),
            color = SonzaSurfaceVariant,
            tonalElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = dynamicColors.accent,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))

        Text(
            text = title,
            style = SonzaTypography.TitleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = SonzaOnBackground,
            textAlign = TextAlign.Center
        )

        if (!description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
            Text(
                text = description,
                style = SonzaTypography.BodyMedium,
                color = SonzaOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = dynamicColors.accent,
                    contentColor = dynamicColors.onAccent
                ),
                shape = RoundedCornerShape(RadiusTokens.Md),
                contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)
            ) {
                Text(
                    text = actionText,
                    style = SonzaTypography.LabelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Standard Sonza Error State per DESIGN_SYSTEM.md Part 6.7.
 */
@Composable
fun SonzaErrorState(
    title: String = "Something went wrong",
    message: String? = null,
    icon: ImageVector = Icons.Rounded.ErrorOutline,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(RadiusTokens.Lg),
            color = SonzaError.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SonzaError,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))

        Text(
            text = title,
            style = SonzaTypography.TitleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = SonzaOnBackground,
            textAlign = TextAlign.Center
        )

        if (!message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
            Text(
                text = message,
                style = SonzaTypography.BodyMedium,
                color = SonzaOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SonzaSurfaceVariant,
                    contentColor = SonzaOnBackground
                ),
                shape = RoundedCornerShape(RadiusTokens.Md),
                border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline),
                contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
                Text(
                    text = "Retry",
                    style = SonzaTypography.LabelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

enum class SonzaBannerType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

/**
 * Standard Sonza Banner / Snackbar per DESIGN_SYSTEM.md Part 6.7 & Part 8.
 * Features:
 * - SonzaSurface background
 * - radius-md (12dp)
 * - elevation-2
 * - Manrope BodyMedium typography
 * - Part 8 200ms ease-in-out slide + fade entrance/exit
 */
@Composable
fun SonzaBanner(
    message: String,
    type: SonzaBannerType = SonzaBannerType.WARNING,
    isVisible: Boolean = true,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (type) {
        SonzaBannerType.WARNING -> Icons.Rounded.WarningAmber to SonzaWarning
        SonzaBannerType.ERROR -> Icons.Rounded.ErrorOutline to SonzaError
        SonzaBannerType.SUCCESS -> Icons.Rounded.Info to SonzaSuccess
        SonzaBannerType.INFO -> Icons.Rounded.Info to LocalSonzaDynamicColors.current.accent
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(MotionTokens.BannerEnterExitDuration, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(MotionTokens.BannerEnterExitDuration, easing = FastOutSlowInEasing)
                ),
        exit = fadeOut(animationSpec = tween(MotionTokens.BannerEnterExitDuration, easing = FastOutSlowInEasing)) +
                slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(MotionTokens.BannerEnterExitDuration, easing = FastOutSlowInEasing)
                ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)
                .shadow(ElevationTokens.Level2, RoundedCornerShape(RadiusTokens.Md)),
            shape = RoundedCornerShape(RadiusTokens.Md),
            color = SonzaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

                Text(
                    text = message,
                    style = SonzaTypography.BodyMedium,
                    color = SonzaOnBackground,
                    modifier = Modifier.weight(1f)
                )

                if (!actionText.isNullOrBlank() && onActionClick != null) {
                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
                    Text(
                        text = actionText,
                        style = SonzaTypography.LabelLarge.copy(fontWeight = FontWeight.Bold),
                        color = LocalSonzaDynamicColors.current.accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(RadiusTokens.Sm))
                            .clickable(onClick = onActionClick)
                            .padding(horizontal = SpacingTokens.SpaceSm, vertical = SpacingTokens.SpaceXs)
                    )
                }

                if (onDismiss != null) {
                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceXs))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = SonzaOnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
