package com.sonza.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.HomeItem
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.ui.theme.*
import com.sonza.app.util.ImageUtils
import java.util.Calendar

/**
 * Top Home Header per DESIGN_SYSTEM.md:
 * - Brand wordmark "Sonza" on leading edge with AppLogo.
 * - Action buttons: Account Avatar.
 * - Dynamic time-of-day greeting with authenticated user name.
 * - Compact height, standard SpaceLg (16dp) padding, clean touch targets.
 */
@Composable
fun HomeTopHeader(
    avatarUrl: String?,
    userName: String? = null,
    currentSong: com.sonza.app.core.model.Song? = null,
    recentlyPlayed: List<com.sonza.app.core.model.RecentlyPlayed> = emptyList(),
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val greetingText = com.sonza.app.ui.utils.HomeGreetingHelper.rememberCurrentGreeting(
        userName = userName,
        currentSong = currentSong,
        recentlyPlayed = recentlyPlayed
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = SpacingTokens.SpaceLg,
                end = SpacingTokens.SpaceLg,
                top = SpacingTokens.SpaceSm,
                bottom = SpacingTokens.SpaceXs
            ),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceSm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Wordmark + Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceSm)
            ) {
                AppLogo(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(RadiusTokens.Sm))
                )
                Text(
                    text = "Sonza",
                    style = SonzaTypography.TitleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = SonzaOnBackground,
                    letterSpacing = (-0.5).sp
                )
            }

            // Account Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SonzaSurfaceVariant)
                    .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Account",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Account",
                        tint = SonzaOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Personalized greeting header
        Text(
            text = greetingText,
            style = SonzaTypography.PageTitle.copy(
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold
            ),
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Standardized Section Header per DESIGN_SYSTEM.md Part 6.1 & P6:
 * - TitleLarge (20sp SemiBold 600, SonzaOnBackground)
 * - Standard Spacing: SpaceLg (16dp) horizontal, SpaceXl (24dp) top, SpaceMd (12dp) bottom
 * - Branded dynamic accent "See all" or custom trailing action
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
            style = SonzaTypography.SectionTitle,
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
            trailingContent()
        } else if (onSeeAllClick != null) {
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
            Surface(
                shape = RoundedCornerShape(RadiusTokens.Pill),
                color = dynamicColors.accent.copy(alpha = 0.20f),
                border = BorderStroke(0.75.dp, dynamicColors.accent.copy(alpha = 0.45f)),
                modifier = Modifier.bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onSeeAllClick)
            ) {
                Text(
                    text = "See all",
                    style = SonzaTypography.LabelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = dynamicColors.accent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Featured / Spotlight Hero Card per P4 & P5:
 * - Real data powered (current track / recommendation / featured playlist).
 * - Compact responsive height (~175dp-185dp) avoiding excessive viewport consumption.
 * - Dynamic accent-muted wash + scrim gradient overlay.
 * - Tag badge, TitleLarge title, BodyMedium subtitle, and primary play action.
 */
@Composable
fun FeaturedHeroCard(
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    tag: String = "FEATURED",
    onClick: () -> Unit,
    onPlayClick: (() -> Unit)? = null,
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
            .height(180.dp)
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

        // Clean Scrim Gradient ensuring crisp artwork and high-contrast text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.35f to Color.Black.copy(alpha = 0.20f),
                        0.65f to Color.Black.copy(alpha = 0.65f),
                        1.0f to Color.Black.copy(alpha = 0.94f)
                    )
                )
        )

        // Metadata & Actions
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(SpacingTokens.SpaceLg),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Tag Badge
                Surface(
                    color = dynamicColors.accent,
                    shape = RoundedCornerShape(RadiusTokens.Sm)
                ) {
                    Text(
                        text = tag.uppercase(),
                        style = SonzaTypography.Kicker,
                        color = dynamicColors.onAccent,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = title,
                    style = SonzaTypography.SectionTitle,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = SonzaTypography.ArtistSubtitle,
                    color = SonzaOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Play FAB Affordance
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .bounceClick(
                        scaleDown = MotionTokens.CardTapScale,
                        onClick = onPlayClick ?: onClick
                    ),
                shape = CircleShape,
                color = dynamicColors.accent,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = dynamicColors.onAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Pattern A — Square Music / Playlist / Album Card per P9, P11, P12 & P14:
 * - Compact 1:1 square artwork (~140-155dp responsive width).
 * - Radius: RadiusTokens.Lg (16dp).
 * - Consistent 1-2 line title and 1-line artist/metadata.
 * - Tap feedback: scale 0.97, 100ms.
 */
@Composable
fun SquareMusicCard(
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: Dp = 145.dp,
    badgeText: String? = null
) {
    val context = LocalContext.current
    val highResThumbnail = remember(thumbnailUrl) {
        ImageUtils.getHighResThumbnailUrl(thumbnailUrl, size = 544) ?: thumbnailUrl
    }

    Column(
        modifier = modifier
            .width(size)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        // 1:1 Square Artwork Container
        Surface(
            modifier = Modifier.size(size),
            color = SonzaSurfaceVariant,
            shape = RoundedCornerShape(RadiusTokens.Lg),
            tonalElevation = 1.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!highResThumbnail.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(highResThumbnail)
                            .crossfade(true)
                            .size(400)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SonzaSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = SonzaOnSurfaceVariant,
                            modifier = Modifier.size(size / 3)
                        )
                    }
                }

                // Optional corner badge (e.g. format / type)
                if (!badgeText.isNullOrBlank()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.70f),
                        shape = RoundedCornerShape(RadiusTokens.Sm),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(SpacingTokens.SpaceXs)
                    ) {
                        Text(
                            text = badgeText.uppercase(),
                            style = SonzaTypography.Kicker,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))

        // Title (SemiBold, max 1 line)
        Text(
            text = title,
            style = SonzaTypography.CardTitle,
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Subtitle / Artist (Regular, 1 line)
        Text(
            text = subtitle,
            style = SonzaTypography.CardSubtitle,
            color = SonzaOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Pattern B — Compact List Row per P16 & P17:
 * - High-density song row (54dp height).
 * - 46dp square artwork with RadiusTokens.Sm (8dp).
 * - Title + artist metadata.
 * - Optional rank number or more options action.
 */
@Composable
fun CompactSongRow(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    rank: Int? = null,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dynamicColors = LocalSonzaDynamicColors.current
    val highResThumbnail = remember(song.thumbnailUrl) {
        ImageUtils.getHighResThumbnailUrl(song.thumbnailUrl, size = 200) ?: song.thumbnailUrl
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(RadiusTokens.Md))
            .background(if (isPlaying) dynamicColors.accent.copy(alpha = 0.12f) else Color.Transparent)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
            .padding(horizontal = SpacingTokens.SpaceXs, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Optional Rank Number
        if (rank != null) {
            Text(
                text = "$rank",
                style = SonzaTypography.SongTitle.copy(fontWeight = FontWeight.Bold),
                color = if (rank <= 3) dynamicColors.accent else SonzaOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        // Small 46dp Artwork
        Surface(
            modifier = Modifier.size(46.dp),
            shape = RoundedCornerShape(RadiusTokens.Sm),
            color = SonzaSurfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!highResThumbnail.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(highResThumbnail)
                            .crossfade(true)
                            .size(140)
                            .build(),
                        contentDescription = song.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = SonzaOnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        NowPlayingAnimation(
                            color = Color.White,
                            isPlaying = true,
                            barCount = 3,
                            barWidth = 2.5.dp,
                            maxBarHeight = 16.dp,
                            minBarHeight = 6.dp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                style = SonzaTypography.SongTitle,
                color = if (isPlaying) dynamicColors.accent else SonzaOnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = song.artist,
                style = SonzaTypography.ArtistSubtitle,
                color = SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // More options
        if (onMoreClick != null) {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = SonzaOnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Pattern C — Chart Card per P18 & P19:
 * - Dedicated chart card with position / ranking badge, 1:1 artwork, title, and country/city or genre.
 */
@Composable
fun ChartCard(
    rank: Int,
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 145.dp
) {
    val context = LocalContext.current
    val dynamicColors = LocalSonzaDynamicColors.current
    val highResThumbnail = remember(thumbnailUrl) {
        ImageUtils.getHighResThumbnailUrl(thumbnailUrl, size = 544) ?: thumbnailUrl
    }

    val rankColor = when (rank) {
        1 -> Color(0xFFFFC83D) // Gold
        2 -> Color(0xFFB8C6D1) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> dynamicColors.accent
    }

    Column(
        modifier = modifier
            .width(size)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(size),
            color = SonzaSurfaceVariant,
            shape = RoundedCornerShape(RadiusTokens.Lg),
            tonalElevation = 1.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!highResThumbnail.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(highResThumbnail)
                            .crossfade(true)
                            .size(400)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Scrim Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.70f)
                                )
                            )
                        )
                )

                // Top-Start Rank Badge
                Surface(
                    color = rankColor,
                    shape = RoundedCornerShape(bottomEnd = RadiusTokens.Md),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (rank <= 3) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "#$rank",
                            style = SonzaTypography.LabelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            ),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))

        Text(
            text = title,
            style = SonzaTypography.CardTitle,
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            style = SonzaTypography.CardSubtitle,
            color = SonzaOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Pattern D — Video Content Card per P20:
 * - 16:9 wider preview card for Watch / Video / Live performance content.
 * - Thumbnail with duration badge, video title, and creator/artist metadata.
 */
@Composable
fun VideoContentCard(
    title: String,
    creator: String,
    thumbnailUrl: String?,
    durationText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 220.dp
) {
    val context = LocalContext.current
    val highResThumbnail = remember(thumbnailUrl) {
        ImageUtils.getHighResThumbnailUrl(thumbnailUrl, size = 640) ?: thumbnailUrl
    }

    Column(
        modifier = modifier
            .width(width)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            color = SonzaSurfaceVariant,
            shape = RoundedCornerShape(RadiusTokens.Lg),
            tonalElevation = 1.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!highResThumbnail.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(highResThumbnail)
                            .crossfade(true)
                            .size(500)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Optional Duration badge
                if (!durationText.isNullOrBlank()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(RadiusTokens.Sm),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(SpacingTokens.SpaceSm)
                    ) {
                        Text(
                            text = durationText,
                            style = SonzaTypography.Metadata,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))

        Text(
            text = title,
            style = SonzaTypography.CardTitle,
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = creator,
            style = SonzaTypography.CardSubtitle,
            color = SonzaOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Playlist Display Card adapter
 */
@Composable
fun PlaylistDisplayCard(
    playlist: PlaylistDisplayItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 145.dp
) {
    SquareMusicCard(
        title = playlist.name,
        subtitle = playlist.uploaderName,
        thumbnailUrl = playlist.thumbnailUrl,
        onClick = onClick,
        size = size,
        modifier = modifier
    )
}

/**
 * Adapt HomeItem to appropriate Card composable
 */
@Composable
fun HomeItemCard(
    item: HomeItem,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    sectionItems: List<HomeItem>,
    onSongMoreClick: (Song) -> Unit = {},
    size: Dp = 145.dp
) {
    when (item) {
        is HomeItem.SongItem -> {
            SquareMusicCard(
                title = item.song.title,
                subtitle = item.song.artist,
                thumbnailUrl = item.song.thumbnailUrl,
                onClick = {
                    val songs = sectionItems.filterIsInstance<HomeItem.SongItem>().map { it.song }
                    val index = songs.indexOf(item.song)
                    if (index != -1) onSongClick(songs, index)
                },
                onMoreClick = { onSongMoreClick(item.song) },
                size = size
            )
        }
        is HomeItem.PlaylistItem -> {
            PlaylistDisplayCard(
                playlist = item.playlist,
                onClick = { onPlaylistClick(item.playlist) },
                size = size
            )
        }
        is HomeItem.AlbumItem -> {
            SquareMusicCard(
                title = item.album.title,
                subtitle = item.album.artist,
                thumbnailUrl = item.album.thumbnailUrl,
                badgeText = item.album.year ?: "ALBUM",
                onClick = { onAlbumClick(item.album) },
                size = size
            )
        }
        is HomeItem.ArtistItem -> {
            // Handled elsewhere
        }
        is HomeItem.ExploreItem -> {
            // Handled by ExploreGridSection
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

/**
 * Backward compatibility card wrapper
 */
@Composable
fun NewReleaseCard(
    title: String,
    subtitle: String,
    imageUrl: String?,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    SquareMusicCard(
        title = title,
        subtitle = subtitle,
        thumbnailUrl = imageUrl,
        onClick = onClick,
        onMoreClick = onMoreClick,
        modifier = modifier
    )
}

