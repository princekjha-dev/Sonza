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
import com.sonza.app.ui.theme.ElevationTokens
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens

/**
 * Sonza Expressive Bottom Navigation Bar.
 *
 * Prominent 4-destination navigation bar (Home, Search, Your Library, Settings)
 * built with Sonza design tokens, WCAG-compliant touch targets, TalkBack accessibility,
 * and solid surface separation to prevent background content bleed.
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
            destination = Destination.Search,
            label = stringResource(R.string.nav_search),
            unselectedIcon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search
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
    val dynamicColors = LocalSonzaDynamicColors.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surfaceColor = SonzaColors.Surface
    val primaryColor = dynamicColors.accent
    val glassShape = RoundedCornerShape(RadiusTokens.Lg)
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
        label = "indicatorIndex"
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
                    elevation = ElevationTokens.Elevation2,
                    shape = glassShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(glassShape)
                .background(glassBaseColor)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(glassShape)
                .background(innerTintColor)
                .background(specularHighlight)
        )

        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val tabWidth = maxWidth / navItems.size
            Box(
                modifier = Modifier
                    .offset(x = tabWidth * indicatorIndex)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(
                        vertical = SpacingTokens.SpaceXs,
                        horizontal = SpacingTokens.SpaceXs
                    )
                    .background(
                        color = primaryColor.copy(alpha = 0.15f * glassIntensity),
                        shape = RoundedCornerShape(RadiusTokens.Md)
                    )
                    .border(
                        width = 0.75.dp,
                        color = primaryColor.copy(alpha = 0.30f * glassIntensity),
                        shape = RoundedCornerShape(RadiusTokens.Md)
                    )
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 0.75.dp,
                    brush = borderBrush,
                    shape = glassShape
                )
        )

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
        targetValue = if (isSelected) accentColor else SonzaColors.OnSurfaceVariant,
        animationSpec = tween(
            durationMillis = MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "itemColor"
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
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceXs)
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )

            Text(
                text = item.label,
                style = SonzaTypography.LabelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    backgroundColor: Color? = null
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val baseSurface = backgroundColor ?: SonzaColors.Surface

    val navShape = RoundedCornerShape(
        topStart = RadiusTokens.Md,
        topEnd = RadiusTokens.Md
    )

    // Solid surface container to prevent content from bleeding through behind the bar
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ElevationTokens.Elevation2,
                shape = navShape,
                ambientColor = Color.Black.copy(alpha = 0.40f),
                spotColor = Color.Black.copy(alpha = 0.30f)
            )
            .clip(navShape)
            .background(color = baseSurface)
            .border(
                width = 0.75.dp,
                color = SonzaColors.Outline.copy(alpha = 0.6f),
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
                .padding(horizontal = SpacingTokens.SpaceSm),
            horizontalArrangement = Arrangement.SpaceEvenly,
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
    val unselectedColor = SonzaColors.OnSurfaceVariant

    // 150ms ease-out transition on tab change per Part 6.3 & Part 8
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(
            durationMillis = MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "navItemColor"
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
                horizontal = SpacingTokens.SpaceSm,
                vertical = SpacingTokens.SpaceXs
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceXs)
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )

            Text(
                text = item.label,
                style = SonzaTypography.LabelSmall.copy(
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
