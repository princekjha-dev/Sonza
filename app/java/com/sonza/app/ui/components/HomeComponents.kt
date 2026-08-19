package com.sonza.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.util.ImageUtils
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.HomeItem
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.ui.utils.SharedTransitionKeys
import com.sonza.app.ui.theme.SquircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert

@Composable
fun HomeItemCard(
    item: HomeItem,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    sectionItems: List<HomeItem>,
    onSongMoreClick: (Song) -> Unit = {}
) {
    when (item) {
        is HomeItem.SongItem -> {
            SquareSongCard(
                song = item.song,
                onClick = { 
                    // Extract all songs from the section for the queue
                    val songs = sectionItems.filterIsInstance<HomeItem.SongItem>().map { it.song }
                    val index = songs.indexOf(item.song)
                    if (index != -1) onSongClick(songs, index)
                },
                onMoreClick = { onSongMoreClick(item.song) },
                size = 170.dp
            )
        }
        is HomeItem.PlaylistItem -> {
            PlaylistDisplayCard(
                playlist = item.playlist,
                onClick = { onPlaylistClick(item.playlist) }
            )
        }
        is HomeItem.AlbumItem -> {
            // Albums get a thin "CD spine" sliver on the leading edge so they
            // read as physical media (album, not playlist) at a glance.
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, bottom = 60.dp)
                        .width(5.dp)
                        .height(170.dp)
                        .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2A2A2A),
                                    Color(0xFF1A1A1A),
                                    Color(0xFF2A2A2A)
                                )
                            )
                        )
                )
                PlaylistDisplayCard(
                    playlist = PlaylistDisplayItem(
                        id = item.album.id,
                        name = item.album.title,
                        url = "",
                        uploaderName = item.album.artist,
                        thumbnailUrl = item.album.thumbnailUrl
                    ),
                    onClick = {
                        onAlbumClick(item.album)
                    }
                )
            }
        }
        is HomeItem.ArtistItem -> {
            // Placeholder for Artist
        }
        is HomeItem.ExploreItem -> {
            // Explore items are handled by ExploreGridSection specifically
        }
    }
}

@Composable
fun PlaylistDisplayCard(
    playlist: PlaylistDisplayItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Get high-res thumbnail (replace w120 or similar with w544)
    val highResThumbnail = ImageUtils.getHighResThumbnailUrl(playlist.thumbnailUrl) ?: playlist.thumbnailUrl

    // YouTube-Music-style card: square artwork with the title/subtitle stacked
    // *below* the image rather than overlaid on it. The Expressive squircle clip
    // is the Sonza signature kept on top of the YTM layout.
    Column(
        modifier = Modifier
            .width(170.dp)
            .bounceClick(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(170.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = SquircleShape,
            tonalElevation = 2.dp
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(highResThumbnail)
                    .crossfade(true)
                    .size(544)  // Request high-res
                    .build(),
                contentDescription = playlist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = playlist.uploaderName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        } else if (onSeeAllClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "SEE ALL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .clickable(onClick = onSeeAllClick)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}


/**
 * Featured / Hero Card per DESIGN_SYSTEM.md Part 6.1:
 * - 2x width span, top of Home only, radius-lg (16dp).
 * - Displays subtle accent-muted wash derived from its own artwork.
 * - Bold title in TitleLarge and subtitle in BodyMedium over scrim gradient.
 */
@Composable
fun FeaturedHeroCard(
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    tag: String = "FEATURED",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dynamicColors = rememberDynamicAccentColors(thumbnailUrl)
    val cardShape = RoundedCornerShape(com.sonza.app.ui.theme.RadiusTokens.Lg)
    val highResThumbnail = remember(thumbnailUrl) {
        ImageUtils.getHighResThumbnailUrl(thumbnailUrl, size = 800) ?: thumbnailUrl
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(cardShape)
            .bounceClick(onClick = onClick)
    ) {
        // High-res artwork background
        if (!highResThumbnail.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(highResThumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(com.sonza.app.ui.theme.SonzaSurfaceVariant)
            )
        }

        // Accent-muted wash + Scrim gradient overlay per Part 1 & 6.1
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to dynamicColors.accentMuted.copy(alpha = 0.35f),
                        0.4f to Color.Transparent,
                        0.7f to Color.Black.copy(alpha = 0.55f),
                        1.0f to Color.Black.copy(alpha = 0.90f)
                    )
                )
        )

        // Metadata & Text content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(com.sonza.app.ui.theme.SpacingTokens.SpaceLg)
        ) {
            // Tag badge
            Surface(
                color = dynamicColors.accent,
                shape = RoundedCornerShape(com.sonza.app.ui.theme.RadiusTokens.Sm)
            ) {
                Text(
                    text = tag.uppercase(),
                    style = com.sonza.app.ui.theme.SonzaTypography.LabelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    color = dynamicColors.onAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(com.sonza.app.ui.theme.SpacingTokens.SpaceXs))

            Text(
                text = title,
                style = com.sonza.app.ui.theme.SonzaTypography.TitleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = com.sonza.app.ui.theme.SonzaTypography.BodyMedium,
                color = com.sonza.app.ui.theme.SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "bounce"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
