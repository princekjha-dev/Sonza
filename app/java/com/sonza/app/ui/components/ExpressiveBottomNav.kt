package com.sonza.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.R
import com.sonza.app.core.model.Song
import com.sonza.app.navigation.Destination
import com.sonza.app.ui.components.player.miniplayer.CompactFloatingMiniPlayer
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography

object ExpressiveBottomNavTokens {
    val NavBarHeight = 52.dp
    val IdleNavBarHeight = 56.dp
    val MiniPlayerHeight = 52.dp
    val Spacing = 8.dp
    val FloatingBarBottomPadding = 8.dp
    val FloatingBarHorizontalPadding = 16.dp
    val TotalBottomBarHeight = 64.dp // 56dp + 8dp

    fun getBottomSafePadding(hasMusicPlaying: Boolean = false): androidx.compose.ui.unit.Dp {
        return TotalBottomBarHeight
    }
}

/**
 * Apple Music-Style Minimal Floating Glass Bottom Navigation System:
 *
 * 1. Playing State (`currentSong != null`):
 *    Three-part floating layout:
 *    [ Home ○ ]   [ Compact Mini Player ————————— ▶ ]   [ Search ○ ]
 *    The mini-player is a small translucent floating pill between independent circular Home and Search buttons.
 *
 * 2. Idle State (`currentSong == null`):
 *    Compact floating navigation pill containing all 4 destinations:
 *    [ Home | Search | Library | Settings ]
 *    Mini-player completely disappears, floating smoothly above content.
 */
@Composable
fun ExpressiveBottomNav(
    currentDestination: Destination,
    onDestinationChange: (Destination) -> Unit,
    onReClick: (Destination) -> Unit = {},
    currentSong: Song? = null,
    isPlaying: Boolean = false,
    isLoading: Boolean = false,
    onPlayPause: () -> Unit = {},
    onExpandPlayer: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    progressProvider: () -> Float = { 0f },
    dominantColors: DominantColors? = null,
    modifier: Modifier = Modifier,
    alpha: Float = 1.0f,
    iosLiquidGlassEnabled: Boolean = false,
    backgroundColor: Color? = null,
    iosNavBarBlur: Float = 60f
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
    val resolvedDominantColors = dominantColors ?: DominantColors(
        primary = dynamicColors.surface,
        secondary = dynamicColors.surfaceVariant,
        accent = accentColor,
        onBackground = dynamicColors.onBackground,
        accentMuted = dynamicColors.accentMuted,
        onAccent = dynamicColors.onAccent,
        isIdle = currentSong == null
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = ExpressiveBottomNavTokens.FloatingBarHorizontalPadding,
                end = ExpressiveBottomNavTokens.FloatingBarHorizontalPadding,
                bottom = ExpressiveBottomNavTokens.FloatingBarBottomPadding,
                top = 0.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentSong != null,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(animationSpec = tween(220, easing = FastOutSlowInEasing), initialScale = 0.94f))
                    .togetherWith(
                        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                scaleOut(animationSpec = tween(180, easing = FastOutSlowInEasing), targetScale = 0.94f)
                    )
            },
            label = "bottomNavModeTransition"
        ) { hasPlayingSong ->
            if (hasPlayingSong && currentSong != null) {
                // --- Playing State: [ Home ○ ] — [ Compact Mini Player ] — [ ○ Search ] ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ExpressiveBottomNavTokens.NavBarHeight),
                    horizontalArrangement = Arrangement.spacedBy(ExpressiveBottomNavTokens.Spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Home Navigation Button (Circular floating glass button on left)
                    FloatingNavCircleButton(
                        icon = Icons.Outlined.Home,
                        selectedIcon = Icons.Filled.Home,
                        isSelected = currentDestination == Destination.Home,
                        contentDescription = stringResource(R.string.nav_home),
                        accentColor = accentColor,
                        size = ExpressiveBottomNavTokens.NavBarHeight,
                        onClick = {
                            if (currentDestination == Destination.Home) {
                                onReClick(Destination.Home)
                            } else {
                                onDestinationChange(Destination.Home)
                            }
                        }
                    )

                    // 2. Compact Floating Mini Player (Center floating translucent capsule)
                    CompactFloatingMiniPlayer(
                        song = currentSong,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        dominantColors = resolvedDominantColors,
                        progressProvider = progressProvider,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onTap = onExpandPlayer,
                        accentColor = accentColor,
                        userAlpha = 1f - alpha.coerceIn(0.5f, 1f),
                        modifier = Modifier.weight(1f)
                    )

                    // 3. Search Navigation Button (Circular floating glass button on right)
                    FloatingNavCircleButton(
                        icon = Icons.Outlined.Search,
                        selectedIcon = Icons.Filled.Search,
                        isSelected = currentDestination == Destination.Search,
                        contentDescription = stringResource(R.string.nav_search),
                        accentColor = accentColor,
                        size = ExpressiveBottomNavTokens.NavBarHeight,
                        onClick = {
                            if (currentDestination == Destination.Search) {
                                onReClick(Destination.Search)
                            } else {
                                onDestinationChange(Destination.Search)
                            }
                        }
                    )
                }
            } else {
                // --- Idle State (No Music Playing): Compact 4-Destination Floating Pill [ Home | Search | Library | Settings ] ---
                CompactFloating4TabsNav(
                    currentDestination = currentDestination,
                    accentColor = accentColor,
                    userAlpha = alpha,
                    onDestinationChange = onDestinationChange,
                    onReClick = onReClick
                )
            }
        }
    }
}

/**
 * Compact 4-Destination Floating Glass Navigation Pill for Idle State.
 * Features generous spacing, individual subtle rounded tabs, small labels,
 * and translucent frosted glass styling floating above the content.
 */
@Composable
private fun CompactFloating4TabsNav(
    currentDestination: Destination,
    accentColor: Color,
    userAlpha: Float,
    onDestinationChange: (Destination) -> Unit,
    onReClick: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(28.dp)
    val effectiveAlpha = (0.88f * userAlpha).coerceIn(0.70f, 0.98f)
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = effectiveAlpha)

    val specularBrush = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = 0.08f),
        0.5f to Color.Transparent,
        1.0f to Color.Black.copy(alpha = 0.12f)
    )

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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(ExpressiveBottomNavTokens.IdleNavBarHeight)
            .shadow(
                elevation = 8.dp,
                shape = pillShape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            ),
        shape = pillShape,
        color = surfaceColor,
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(specularBrush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentDestination == item.destination
                    val label = stringResource(item.labelRes)

                    CompactNavItemView(
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
}

/**
 * Individual destination tab in the compact 4-tab floating navigation pill.
 * Features subtle rounded indicator when active with dynamic accent color.
 */
@Composable
private fun CompactNavItemView(
    item: BottomNavItem,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val unselectedColor = SonzaOnSurfaceVariant.copy(alpha = 0.75f)

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else unselectedColor,
        animationSpec = tween(
            durationMillis = MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "compactNavItemColor"
    )

    val indicatorBgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(
            durationMillis = MotionTokens.NavSelectionDuration,
            easing = FastOutSlowInEasing
        ),
        label = "compactNavItemIndicatorBg"
    )

    val itemSemanticsDescription = if (isSelected) "$label, selected" else label

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(indicatorBgColor)
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
            .padding(vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = SonzaTypography.NavLabel.copy(
                    fontSize = 11.sp,
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


