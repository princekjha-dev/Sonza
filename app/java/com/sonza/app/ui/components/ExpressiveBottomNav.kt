package com.sonza.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography

/**
 * Clean & Minimal Sonza Bottom Navigation Bar (Idle / No-Music Playing State).
 *
 * Fixed at the bottom and visually integrated with Sonza's dark background.
 * No floating container, no surrounding background panel, no border, no blur/glass effect,
 * and no empty player space.
 *
 * 4 primary navigation destinations:
 * - Home (Icons.Filled.Home / Icons.Outlined.Home)
 * - Search (Icons.Filled.Search / Icons.Outlined.Search)
 * - Library (Icons.Filled.LibraryMusic / Icons.Outlined.LibraryMusic)
 * - Settings (Icons.Filled.Settings / Icons.Outlined.Settings)
 *
 * Each item has an icon on top, label underneath, equal spacing, proper alignment,
 * active tab highlighted with dynamic brand accent, and inactive tabs in neutral gray.
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
    val navItems = remember {
        listOf(
            BottomNavItem(
                destination = Destination.Home,
                labelRes = R.string.nav_home,
                unselectedIcon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home
            ),
            BottomNavItem(
                destination = Destination.Search,
                labelRes = R.string.nav_search,
                unselectedIcon = Icons.Outlined.Search,
                selectedIcon = Icons.Filled.Search
            ),
            BottomNavItem(
                destination = Destination.Library,
                labelRes = R.string.nav_library,
                unselectedIcon = Icons.Outlined.LibraryMusic,
                selectedIcon = Icons.Filled.LibraryMusic
            ),
            BottomNavItem(
                destination = Destination.Settings,
                labelRes = R.string.nav_settings,
                unselectedIcon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings
            )
        )
    }

    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent
    val navBackground = backgroundColor ?: SonzaBackground

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(navBackground)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentDestination == item.destination
                val label = stringResource(item.labelRes)

                BottomNavItemView(
                    item = item,
                    label = label,
                    isSelected = isSelected,
                    accentColor = accentColor,
                    onClick = {
                        if (isSelected) {
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
private fun BottomNavItemView(
    item: BottomNavItem,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val unselectedColor = SonzaOnSurfaceVariant.copy(alpha = 0.70f) // Neutral gray for inactive tabs

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else unselectedColor,
        animationSpec = tween(
            durationMillis = MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "navItemColor"
    )

    val itemSemanticsDescription = if (isSelected) "$label, selected" else label

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
            .padding(vertical = 4.dp),
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

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = label,
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
    @get:androidx.annotation.StringRes val labelRes: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)
