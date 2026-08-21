package com.sonza.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.R
import com.sonza.app.core.model.Song
import com.sonza.app.navigation.Destination
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurface
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaTypography

object ExpressiveBottomNavTokens {
    val NavBarHeight = 58.dp
}

/**
 * Adaptive Bottom Navigation Bar:
 * - When no track is playing: Standard 4 tabs (Home, Search, Library, Settings) full-width, evenly spaced.
 * - When a track is playing: Single continuous bar arranged as:
 *   [Far Left: Circular Home Button] [Middle: Inline Mini-Player (Artwork + Marquee Title/Artist + Play/Pause)] [Far Right: Circular Search Button]
 *   All sharing the exact same 58dp height token.
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
    modifier: Modifier = Modifier,
    alpha: Float = 1.0f,
    iosLiquidGlassEnabled: Boolean = false,
    backgroundColor: Color? = null,
    iosNavBarBlur: Float = 60f
) {
    val accentColor = SonzaBrandAccent
    val navBackground = backgroundColor ?: SonzaBackground
    val hasSong = currentSong != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(navBackground)
            .navigationBarsPadding()
    ) {
        AnimatedContent(
            targetState = hasSong,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
            },
            label = "bottomNavLayout"
        ) { isPlayingTrack ->
            if (isPlayingTrack && currentSong != null) {
                InlinePlayerBottomNavBar(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    currentDestination = currentDestination,
                    accentColor = accentColor,
                    onDestinationChange = onDestinationChange,
                    onReClick = onReClick,
                    onPlayPause = onPlayPause,
                    onExpandPlayer = onExpandPlayer
                )
            } else {
                Standard4TabsNavBar(
                    currentDestination = currentDestination,
                    accentColor = accentColor,
                    onDestinationChange = onDestinationChange,
                    onReClick = onReClick
                )
            }
        }
    }
}

@Composable
private fun InlinePlayerBottomNavBar(
    currentSong: Song,
    isPlaying: Boolean,
    isLoading: Boolean,
    currentDestination: Destination,
    accentColor: Color,
    onDestinationChange: (Destination) -> Unit,
    onReClick: (Destination) -> Unit,
    onPlayPause: () -> Unit,
    onExpandPlayer: () -> Unit
) {
    val isHomeSelected = currentDestination == Destination.Home
    val isSearchSelected = currentDestination == Destination.Search
    val unselectedColor = SonzaOnSurfaceVariant.copy(alpha = 0.70f)

    val homeIconColor by animateColorAsState(
        targetValue = if (isHomeSelected) accentColor else unselectedColor,
        animationSpec = tween(durationMillis = MotionTokens.NavSelectionDuration, easing = FastOutSlowInEasing),
        label = "homeIconColor"
    )

    val searchIconColor by animateColorAsState(
        targetValue = if (isSearchSelected) accentColor else unselectedColor,
        animationSpec = tween(durationMillis = MotionTokens.NavSelectionDuration, easing = FastOutSlowInEasing),
        label = "searchIconColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ExpressiveBottomNavTokens.NavBarHeight)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Far Left: Circular Home icon button (same height as bar)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (isHomeSelected) onReClick(Destination.Home) else onDestinationChange(Destination.Home)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                contentDescription = stringResource(R.string.nav_home),
                modifier = Modifier.size(24.dp),
                tint = homeIconColor
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Middle: Mini-player section expanding to fill remaining space
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onExpandPlayer
                ),
            shape = RoundedCornerShape(23.dp),
            color = SonzaSurface.copy(alpha = 0.85f),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left edge: Album art thumbnail
                val thumbUrl = currentSong.thumbnailUrl
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!thumbUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = thumbUrl,
                            contentDescription = currentSong.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = SonzaOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Middle: Track title and artist
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentSong.title,
                        style = SonzaTypography.SongTitle.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SonzaOnBackground,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    if (!currentSong.artist.isNullOrBlank()) {
                        Text(
                            text = currentSong.artist,
                            style = SonzaTypography.ArtistSubtitle.copy(
                                fontSize = 11.5.sp
                            ),
                            color = SonzaOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Right edge: Play/Pause button
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = accentColor
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = SonzaOnSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Far Right: Circular Search icon button (same height as bar)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (isSearchSelected) onReClick(Destination.Search) else onDestinationChange(Destination.Search)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearchSelected) Icons.Filled.Search else Icons.Outlined.Search,
                contentDescription = stringResource(R.string.nav_search),
                modifier = Modifier.size(24.dp),
                tint = searchIconColor
            )
        }
    }
}

@Composable
private fun Standard4TabsNavBar(
    currentDestination: Destination,
    accentColor: Color,
    onDestinationChange: (Destination) -> Unit,
    onReClick: (Destination) -> Unit
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ExpressiveBottomNavTokens.NavBarHeight),
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
    val unselectedColor = SonzaOnSurfaceVariant.copy(alpha = 0.70f)

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
