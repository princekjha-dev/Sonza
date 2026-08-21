package com.sonza.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonza.app.R
import com.sonza.app.navigation.Destination
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens

/**
 * Sonza Floating Pill Bottom Navigation Bar.
 *
 * Prominent 4-destination navigation bar (Home, Library, Settings, Search)
 * with a floating pill-shaped container, rounded highlighted capsule on active state,
 * smooth transitions, and WCAG-compliant touch targets.
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
            label = stringResource(R.string.nav_home),
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        BottomNavItem(
            destination = Destination.Library,
            label = stringResource(R.string.nav_library),
            unselectedIcon = Icons.Outlined.LibraryMusic,
            selectedIcon = Icons.Filled.LibraryMusic
        ),
        BottomNavItem(
            destination = Destination.Settings,
            label = stringResource(R.string.nav_settings),
            unselectedIcon = Icons.Outlined.Settings,
            selectedIcon = Icons.Filled.Settings
        ),
        BottomNavItem(
            destination = Destination.Search,
            label = stringResource(R.string.nav_search),
            unselectedIcon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search
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
            backgroundColor = backgroundColor
        )
    }
}

// ─── Floating Pill Navigation Bar (Standard Mode) ───────────────────────────

@Composable
private fun StandardNavBar(
    navItems: List<BottomNavItem>,
    currentDestination: Destination,
    onDestinationChange: (Destination) -> Unit,
    onReClick: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val pillShape = RoundedCornerShape(32.dp)
    val baseSurface = backgroundColor ?: SonzaSurface

    val selectedItemIndex = navItems
        .indexOfFirst { it.destination == currentDestination }
        .coerceAtLeast(0)

    val indicatorIndex by animateFloatAsState(
        targetValue = selectedItemIndex.toFloat(),
        animationSpec = tween(
            durationMillis = MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "standardNavIndicatorIndex"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = SpacingTokens.SpaceLg,
                vertical = SpacingTokens.SpaceSm
            )
            .height(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        // Floating Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = 6.dp,
                    shape = pillShape,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
        )

        // Floating Background
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(pillShape)
                .background(baseSurface.copy(alpha = 0.92f))
                .background(
                    brush = Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.08f),
                        0.5f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.15f)
                    )
                )
                .border(
                    width = 0.75.dp,
                    color = SonzaOutline.copy(alpha = 0.35f),
                    shape = pillShape
                )
        )

        // Active highlighted capsule gliding behind selected tab
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val tabWidth = maxWidth / navItems.size
            Box(
                modifier = Modifier
                    .offset(x = tabWidth * indicatorIndex)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(
                        vertical = 6.dp,
                        horizontal = 4.dp
                    )
                    .background(
                        color = dynamicColors.accent.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .border(
                        width = 0.75.dp,
                        color = dynamicColors.accent.copy(alpha = 0.40f),
                        shape = RoundedCornerShape(22.dp)
                    )
            )
        }

        // Navigation Items Row
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentDestination == item.destination
                FloatingNavItem(
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

// ─── Floating Pill Navigation Bar (iOS Liquid Glass Mode) ─────────────────────

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
    val dynamicColors = LocalSonzaDynamicColors.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surfaceColor = SonzaColors.Surface
    val primaryColor = dynamicColors.accent
    val pillShape = RoundedCornerShape(32.dp)
    val glassIntensity = (1f - alpha.coerceIn(0f, 1f)).coerceAtLeast(0.05f)

    val glassBaseAlpha = (if (isDarkTheme) 0.55f else 0.40f) * glassIntensity
    val glassBaseColor = if (isDarkTheme) {
        surfaceColor.copy(alpha = glassBaseAlpha)
    } else {
        Color.White.copy(alpha = glassBaseAlpha)
    }

    val specularHighlight = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = (if (isDarkTheme) 0.15f else 0.20f) * glassIntensity),
        0.3f to Color.White.copy(alpha = (if (isDarkTheme) 0.05f else 0.08f) * glassIntensity),
        0.5f to Color.Transparent,
        1.0f to Color.Black.copy(alpha = (if (isDarkTheme) 0.08f else 0.02f) * glassIntensity)
    )

    val borderBrush = Brush.verticalGradient(
        0.0f to SonzaColors.Outline.copy(alpha = 0.8f * glassIntensity),
        0.4f to SonzaColors.Outline.copy(alpha = 0.4f * glassIntensity),
        1.0f to Color.Transparent
    )

    val innerTintColor = primaryColor.copy(alpha = 0.06f * glassIntensity)

    val selectedItemIndex = navItems
        .indexOfFirst { it.destination == currentDestination }
        .coerceAtLeast(0)

    val indicatorIndex by animateFloatAsState(
        targetValue = selectedItemIndex.toFloat(),
        animationSpec = tween(
            durationMillis = MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "liquidGlassNavIndicatorIndex"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = SpacingTokens.SpaceLg,
                vertical = SpacingTokens.SpaceSm
            )
            .height(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = 6.dp,
                    shape = pillShape,
                    ambientColor = Color.Black.copy(alpha = 0.40f),
                    spotColor = Color.Black.copy(alpha = 0.30f)
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(pillShape)
                .background(glassBaseColor)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(pillShape)
                .background(innerTintColor)
                .background(specularHighlight)
        )

        // Active highlighted capsule gliding behind selected tab
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val tabWidth = maxWidth / navItems.size
            Box(
                modifier = Modifier
                    .offset(x = tabWidth * indicatorIndex)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(
                        vertical = 6.dp,
                        horizontal = 4.dp
                    )
                    .background(
                        color = primaryColor.copy(alpha = 0.22f * glassIntensity),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .border(
                        width = 0.75.dp,
                        color = primaryColor.copy(alpha = 0.45f * glassIntensity),
                        shape = RoundedCornerShape(22.dp)
                    )
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 0.75.dp,
                    brush = borderBrush,
                    shape = pillShape
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentDestination == item.destination
                FloatingNavItem(
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

// ─── Floating Navigation Item ────────────────────────────────────────────────

@Composable
private fun FloatingNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else SonzaColors.OnSurfaceVariant.copy(alpha = 0.75f),
        animationSpec = tween(
            durationMillis = MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "floatingNavItemColor"
    )

    val itemSemanticsDescription = if (isSelected) "${item.label}, selected" else item.label

    Box(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            )
            .semantics {
                contentDescription = itemSemanticsDescription
            }
            .padding(
                horizontal = SpacingTokens.SpaceXs,
                vertical = SpacingTokens.SpaceXs
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.label,
                style = SonzaTypography.NavLabel.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
