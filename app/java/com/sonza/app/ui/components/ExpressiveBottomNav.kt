package com.sonza.app.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.navigation.Destination
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlin.random.Random

/**
 * iOS 26-style Liquid Glass Bottom Navigation Bar.
 *
 * Features a floating pill-shaped navbar with multi-layered glass material:
 * - Frosted translucent base with adaptive tint
 * - Specular highlight gradient (top-to-bottom light reflection)
 * - Luminous gradient border rim
 * - Soft diffused shadow for depth
 * - Morphing selected indicator with spring physics
 * - Dark/light theme adaptive glass appearance
 *
 * Uses Material You dynamic theming for colors that adapt to wallpaper.
 */
@Composable
fun ExpressiveBottomNav(
    currentDestination: Destination,
    onDestinationChange: (Destination) -> Unit,
    onReClick: (Destination) -> Unit = {},
    modifier: Modifier = Modifier,
    alpha: Float = 1.0f,
    iosLiquidGlassEnabled: Boolean = false,
    backgroundColor: Color? = null,
    iosNavBarBlur: Float = 60f
) {
    val navItems = listOf(
        BottomNavItem(
            destination = Destination.Home,
            label = androidx.compose.ui.res.stringResource(com.sonza.app.R.string.nav_home),
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        BottomNavItem(
            destination = Destination.Search,
            label = androidx.compose.ui.res.stringResource(com.sonza.app.R.string.nav_search),
            unselectedIcon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search
        ),
        BottomNavItem(
            destination = Destination.Library,
            label = androidx.compose.ui.res.stringResource(com.sonza.app.R.string.nav_library),
            unselectedIcon = Icons.Outlined.LibraryMusic,
            selectedIcon = Icons.Filled.LibraryMusic
        ),
        BottomNavItem(
            destination = Destination.Settings,
            label = androidx.compose.ui.res.stringResource(com.sonza.app.R.string.nav_settings),
            unselectedIcon = Icons.Outlined.Settings,
            selectedIcon = Icons.Filled.Settings
        )
    )

    if (iosLiquidGlassEnabled) {
        LiquidGlassNavBar(
            navItems = navItems,
            currentDestination = currentDestination,
            onDestinationChange = onDestinationChange,
            onReClick = onReClick,
            modifier = modifier,
            alpha = alpha,
            blurAmount = iosNavBarBlur
        )
    } else {
        StandardNavBar(
            navItems = navItems,
            currentDestination = currentDestination,
            onDestinationChange = onDestinationChange,
            onReClick = onReClick,
            modifier = modifier,
            alpha = alpha,
            backgroundColor = backgroundColor
        )
    }
}

// ─── iOS Liquid Glass Navigation Bar ─────────────────────────────────────────

@Composable
private fun LiquidGlassNavBar(
    navItems: List<BottomNavItem>,
    currentDestination: Destination,
    onDestinationChange: (Destination) -> Unit,
    onReClick: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    alpha: Float = 1.0f,
    blurAmount: Float = 60f
) {
    val dynamicColors = com.sonza.app.ui.components.LocalSonzaDynamicColors.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surfaceColor = com.sonza.app.ui.theme.SonzaColors.Surface
    val primaryColor = dynamicColors.accent
    val glassShape = RoundedCornerShape(com.sonza.app.ui.theme.RadiusTokens.Lg)
    val glassIntensity = (1f - alpha.coerceIn(0f, 1f)).coerceAtLeast(0.05f)

    // Glass material colors – adaptive for dark/light
    val glassBaseAlpha = (if (isDarkTheme) 0.55f else 0.40f) * glassIntensity
    val glassBaseColor = if (isDarkTheme) {
        surfaceColor.copy(alpha = glassBaseAlpha)
    } else {
        Color.White.copy(alpha = glassBaseAlpha)
    }

    // Specular highlight (simulates light hitting the top of the glass)
    val specularHighlight = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = (if (isDarkTheme) 0.15f else 0.20f) * glassIntensity),
        0.3f to Color.White.copy(alpha = (if (isDarkTheme) 0.05f else 0.08f) * glassIntensity),
        0.5f to Color.Transparent,
        1.0f to Color.Black.copy(alpha = (if (isDarkTheme) 0.08f else 0.02f) * glassIntensity)
    )

    // Border rim – luminous on top, fading to transparent
    val borderBrush = Brush.verticalGradient(
        0.0f to com.sonza.app.ui.theme.SonzaColors.Outline.copy(alpha = 0.8f * glassIntensity),
        0.4f to com.sonza.app.ui.theme.SonzaColors.Outline.copy(alpha = 0.4f * glassIntensity),
        1.0f to Color.Transparent
    )

    // Inner tint – subtle color wash from dynamic accent
    val innerTintColor = primaryColor.copy(alpha = 0.06f * glassIntensity)

    val selectedItemIndex = navItems
        .indexOfFirst { it.destination == currentDestination }
        .coerceAtLeast(0)

    val indicatorIndex by animateFloatAsState(
        targetValue = selectedItemIndex.toFloat(),
        animationSpec = tween(
            durationMillis = com.sonza.app.ui.theme.MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "indicatorIndex"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = com.sonza.app.ui.theme.SpacingTokens.SpaceLg,
                vertical = com.sonza.app.ui.theme.SpacingTokens.SpaceSm
            )
            .height(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        // Layer 1: Enhanced shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = com.sonza.app.ui.theme.ElevationTokens.Elevation2,
                    shape = glassShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
        )

        // Layer 2: Glass material stack
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(glassShape)
                .then(
                    if (blurAmount > 0.5f && Build.VERSION.SDK_INT >= 31) {
                        Modifier.graphicsLayer {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                blurAmount,
                                blurAmount,
                                android.graphics.Shader.TileMode.DECAL
                            ).asComposeRenderEffect()
                        }
                    } else if (blurAmount > 0.5f) {
                        Modifier.blur((blurAmount / 2).dp)
                    } else {
                        Modifier
                    }
                )
                .background(glassBaseColor)
        )

        // Layer 3: Color tint & Specular highlight
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(glassShape)
                .background(innerTintColor)
                .background(specularHighlight)
        )

        // Layer 4: Indicator (equal-width tabs)
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val tabWidth = maxWidth / navItems.size
            Box(
                modifier = Modifier
                    .offset(x = tabWidth * indicatorIndex)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(
                        vertical = com.sonza.app.ui.theme.SpacingTokens.SpaceXs,
                        horizontal = com.sonza.app.ui.theme.SpacingTokens.SpaceXs
                    )
                    .background(
                        color = primaryColor.copy(alpha = 0.15f * glassIntensity),
                        shape = RoundedCornerShape(com.sonza.app.ui.theme.RadiusTokens.Md)
                    )
                    .border(
                        width = 0.75.dp,
                        color = primaryColor.copy(alpha = 0.30f * glassIntensity),
                        shape = RoundedCornerShape(com.sonza.app.ui.theme.RadiusTokens.Md)
                    )
            )
        }

        // Layer 5: Luminous rim
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 0.75.dp,
                    brush = borderBrush,
                    shape = glassShape
                )
        )

        // Layer 6: Items
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentDestination == item.destination
                LiquidGlassNavItem(
                    item = item,
                    isSelected = isSelected,
                    accentColor = dynamicColors.accent,
                    onClick = {
                        if (currentDestination == item.destination) {
                            onReClick(item.destination)
                        } else {
                            onDestinationChange(item.destination)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else com.sonza.app.ui.theme.SonzaColors.OnSurfaceVariant,
        animationSpec = tween(
            durationMillis = com.sonza.app.ui.theme.MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "itemColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(com.sonza.app.ui.theme.SpacingTokens.SpaceXs)
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )

            Text(
                text = item.label,
                style = com.sonza.app.ui.theme.SonzaTypography.LabelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

// ─── Standard (Design System) Navigation Bar ─────────────────────────────────

@Composable
private fun StandardNavBar(
    navItems: List<BottomNavItem>,
    currentDestination: Destination,
    onDestinationChange: (Destination) -> Unit,
    onReClick: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    alpha: Float = 1.0f,
    backgroundColor: Color? = null
) {
    val dynamicColors = com.sonza.app.ui.components.LocalSonzaDynamicColors.current
    val baseSurface = backgroundColor ?: com.sonza.app.ui.theme.SonzaColors.Surface
    val blurRadius = com.sonza.app.ui.theme.ElevationTokens.StandardBlurRadius.value

    // Blur-behind treatment per Part 5 & 6.3:
    // API 31+: RenderEffect 12dp radial blur + surface @ 78% opacity + outline rim
    // API < 31 fallback: surface @ 92% opacity + elevation shadow
    val isApi31Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val navShape = RoundedCornerShape(
        topStart = com.sonza.app.ui.theme.RadiusTokens.Md,
        topEnd = com.sonza.app.ui.theme.RadiusTokens.Md
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isApi31Plus) {
                    Modifier.shadow(
                        elevation = com.sonza.app.ui.theme.ElevationTokens.Elevation2,
                        shape = navShape,
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.25f)
                    )
                } else Modifier
            )
            .clip(navShape)
            .then(
                if (isApi31Plus && blurRadius > 0.5f) {
                    Modifier.graphicsLayer {
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            blurRadius * 2f,
                            blurRadius * 2f,
                            android.graphics.Shader.TileMode.DECAL
                        ).asComposeRenderEffect()
                    }
                } else Modifier
            )
            .background(
                color = if (isApi31Plus) baseSurface.copy(alpha = 0.78f * alpha.coerceIn(0f, 1f))
                        else baseSurface.copy(alpha = 0.92f * alpha.coerceIn(0f, 1f))
            )
            .border(
                width = 0.75.dp,
                color = com.sonza.app.ui.theme.SonzaColors.Outline.copy(alpha = 0.6f),
                shape = navShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = com.sonza.app.ui.theme.SpacingTokens.SpaceSm),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentDestination == item.destination
                StandardNavItem(
                    item = item,
                    isSelected = isSelected,
                    accentColor = dynamicColors.accent,
                    onClick = {
                        if (currentDestination == item.destination) {
                            onReClick(item.destination)
                        } else {
                            onDestinationChange(item.destination)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StandardNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val selectedColor = accentColor
    val unselectedColor = com.sonza.app.ui.theme.SonzaColors.OnSurfaceVariant

    // 150ms ease-out transition on tab change per Part 6.3 & Part 8
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(
            durationMillis = com.sonza.app.ui.theme.MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "navItemColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = com.sonza.app.ui.theme.SpacingTokens.SpaceSm,
                vertical = com.sonza.app.ui.theme.SpacingTokens.SpaceXs
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(com.sonza.app.ui.theme.SpacingTokens.SpaceXs)
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )

            Text(
                text = item.label,
                style = com.sonza.app.ui.theme.SonzaTypography.LabelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

private data class BottomNavItem(
    val destination: Destination,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)
