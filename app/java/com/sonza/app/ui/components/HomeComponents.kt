package com.sonza.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.sonza.app.ui.theme.*

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
            // Handled elsewhere
        }
        is HomeItem.ExploreItem -> {
            // Handled by ExploreGridSection
        }
    }
}

@Composable
fun PlaylistDisplayCard(
    playlist: PlaylistDisplayItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val highResThumbnail = ImageUtils.getHighResThumbnailUrl(playlist.thumbnailUrl) ?: playlist.thumbnailUrl

    Column(
        modifier = Modifier
            .width(170.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(170.dp),
            color = SonzaSurfaceVariant,
            shape = RoundedCornerShape(RadiusTokens.Lg),
            tonalElevation = 2.dp
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(highResThumbnail)
                    .crossfade(true)
                    .size(544)
                    .build(),
                contentDescription = playlist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))

        Text(
            text = playlist.name,
            style = SonzaTypography.TitleSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            color = SonzaOnBackground,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = playlist.uploaderName,
            style = SonzaTypography.BodyMedium.copy(fontSize = 13.sp),
            color = SonzaOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Section Header per DESIGN_SYSTEM.md Part 6.1:
 * - TitleLarge (20sp SemiBold 600)
 * - Spacing: SpaceLg (16dp) horizontal, SpaceXl (24dp) top, SpaceMd (12dp) bottom
 * - Branded dynamic accent "See all" pill
 */
@Composable
fun HomeSectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dynamicColors = LocalSonzaDynamicColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = SpacingTokens.SpaceLg,
                end = SpacingTokens.SpaceLg,
                top = SpacingTokens.SpaceXl,
                bottom = SpacingTokens.SpaceMd
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = SonzaTypography.TitleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            ),
            color = SonzaOnBackground,
            letterSpacing = (-0.3).sp,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
            trailingContent()
        } else if (onSeeAllClick != null) {
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
            Surface(
                shape = RoundedCornerShape(RadiusTokens.Pill),
                color = dynamicColors.accent.copy(alpha = 0.12f),
                modifier = Modifier.bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onSeeAllClick)
            ) {
                Text(
                    text = "See all",
                    style = SonzaTypography.LabelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = dynamicColors.accent,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
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
 * - Part 8 tap feedback: scale 0.97, 100ms.
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
    val cardShape = RoundedCornerShape(RadiusTokens.Lg)
    val highResThumbnail = remember(thumbnailUrl) {
        ImageUtils.getHighResThumbnailUrl(thumbnailUrl, size = 800) ?: thumbnailUrl
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(cardShape)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
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
                    .background(SonzaSurfaceVariant)
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
                .padding(SpacingTokens.SpaceLg)
        ) {
            // Tag badge
            Surface(
                color = dynamicColors.accent,
                shape = RoundedCornerShape(RadiusTokens.Sm)
            ) {
                Text(
                    text = tag.uppercase(),
                    style = SonzaTypography.LabelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    color = dynamicColors.onAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.SpaceXs))

            Text(
                text = title,
                style = SonzaTypography.TitleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = SonzaTypography.BodyMedium,
                color = SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Tap Feedback modifier per DESIGN_SYSTEM.md Part 8:
 * - Scale down to 0.97 on press with 100ms duration.
 */
fun Modifier.bounceClick(
    scaleDown: Float = MotionTokens.CardTapScale,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = tween(
            durationMillis = MotionTokens.CardTapDuration,
            easing = FastOutSlowInEasing
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

