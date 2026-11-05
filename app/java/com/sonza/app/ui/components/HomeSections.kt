package com.sonza.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.sonza.app.core.model.HomeSection
import com.sonza.app.core.model.HomeSectionType
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.ui.theme.*
import com.sonza.app.ui.utils.carouselSwipeShield
import com.sonza.app.util.ImageUtils

// -----------------------------------------------------------------------------
// Pattern A — Horizontal Square Music / Playlist / Album Cards Rail
// -----------------------------------------------------------------------------

/**
 * Pattern A: Compact horizontal rail for New Releases, New This Week, Playlists, Albums, and Mixes.
 * Uses responsive card width so multiple cards (~2.4) are visible simultaneously.
 */
@Composable
fun HorizontalSquareCardsSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (section.items.isEmpty()) return

    val items = remember(section.items) { section.items.distinctBy { it.id } }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = remember(screenWidth) {
        ((screenWidth - 32.dp - 24.dp) / 2.35f).coerceIn(136.dp, 160.dp)
    }

    Column(modifier = modifier) {
        HomeSectionHeader(
            title = section.title,
            onSeeAllClick = onSeeAllClick
        )

        LazyRow(
            modifier = Modifier.carouselSwipeShield(),
            contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.id },
                contentType = { _, item -> item::class }
            ) { _, item ->
                HomeItemCard(
                    item = item,
                    onSongClick = onSongClick,
                    onPlaylistClick = onPlaylistClick,
                    onAlbumClick = onAlbumClick,
                    sectionItems = items,
                    onSongMoreClick = onSongMoreClick,
                    size = cardWidth
                )
            }
        }
    }
}

// Backward-compatible alias for HorizontalSquareCardsSection
@Composable
fun HorizontalCarouselSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Check if it is a chart section
    if (isChartSection(section.title)) {
        ChartCardsSection(
            section = section,
            onSongClick = onSongClick,
            onPlaylistClick = onPlaylistClick,
            onAlbumClick = onAlbumClick,
            modifier = modifier
        )
        return
    }

    // Check if it is a video section
    if (isVideoSection(section.title)) {
        VideoCardsSection(
            section = section,
            onSongClick = onSongClick,
            onPlaylistClick = onPlaylistClick,
            onAlbumClick = onAlbumClick,
            modifier = modifier
        )
        return
    }

    HorizontalSquareCardsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        onSongMoreClick = onSongMoreClick,
        modifier = modifier
    )
}

// -----------------------------------------------------------------------------
// Pattern B — Compact List Rows Rail (Best New Songs, Trending Songs, Quick Picks)
// -----------------------------------------------------------------------------

/**
 * Pattern B: Compact list rows chunked into 3-4 items per vertical page in a horizontal rail.
 * Maximizes information density, allowing 9-12 songs to be browsed cleanly with minimal scrolling.
 */
@Composable
fun CompactSongRowsSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    rowsPerPage: Int = 4,
    modifier: Modifier = Modifier
) {
    if (section.items.isEmpty()) return

    val songs = remember(section.items) {
        section.items.mapNotNull {
            when (it) {
                is HomeItem.SongItem -> it.song
                is HomeItem.PlaylistItem -> Song(
                    id = it.playlist.id,
                    title = it.playlist.name,
                    artist = it.playlist.uploaderName,
                    thumbnailUrl = it.playlist.thumbnailUrl,
                    album = "Playlist",
                    duration = 0L,
                    source = com.sonza.app.core.model.SongSource.YOUTUBE
                )
                is HomeItem.AlbumItem -> Song(
                    id = it.album.id,
                    title = it.album.title,
                    artist = it.album.artist,
                    thumbnailUrl = it.album.thumbnailUrl,
                    album = it.album.year ?: "Album",
                    duration = 0L,
                    source = com.sonza.app.core.model.SongSource.YOUTUBE
                )
                else -> null
            }
        }
    }

    if (songs.isEmpty()) return

    val pages = remember(songs, rowsPerPage) { songs.chunked(rowsPerPage) }
    val listState = rememberLazyListState()

    Column(modifier = modifier) {
        HomeSectionHeader(
            title = section.title,
            trailingContent = {
                PlayAllPill(onClick = { onSongClick(songs, 0) })
            }
        )

        LazyRow(
            modifier = Modifier.carouselSwipeShield(),
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            itemsIndexed(
                items = pages,
                key = { index, _ -> "${section.title}_page_$index" }
            ) { pageIndex, pageSongs ->
                Column(
                    modifier = Modifier.fillParentMaxWidth(if (pages.size > 1) 0.88f else 1f),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceXs)
                ) {
                    pageSongs.forEachIndexed { rowIndex, song ->
                        val globalIndex = pageIndex * rowsPerPage + rowIndex
                        CompactSongRow(
                            song = song,
                            onClick = {
                                val originalItem = section.items.getOrNull(globalIndex)
                                when (originalItem) {
                                    is HomeItem.PlaylistItem -> onPlaylistClick(originalItem.playlist)
                                    is HomeItem.AlbumItem -> onAlbumClick(originalItem.album)
                                    else -> onSongClick(songs, globalIndex)
                                }
                            },
                            onMoreClick = { onSongMoreClick(song) }
                        )
                    }
                }
            }
        }
    }
}

// Backward-compatible alias for VerticalListSection & QuickPicksSection
@Composable
fun VerticalListSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    CompactSongRowsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        onSongMoreClick = onSongMoreClick,
        modifier = modifier
    )
}

@Composable
fun QuickPicksSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    CompactSongRowsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        onSongMoreClick = onSongMoreClick,
        modifier = modifier
    )
}

/**
 * Compact "Play all" pill used in section headers
 */
@Composable
fun PlayAllPill(onClick: () -> Unit) {
    val dynamicColors = LocalSonzaDynamicColors.current

    Surface(
        shape = RoundedCornerShape(RadiusTokens.Pill),
        color = dynamicColors.accent.copy(alpha = 0.20f),
        border = androidx.compose.foundation.BorderStroke(0.75.dp, dynamicColors.accent.copy(alpha = 0.45f)),
        modifier = Modifier.bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = dynamicColors.accent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Play all",
                style = SonzaTypography.LabelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = dynamicColors.accent
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Pattern C — Chart Cards Section (Top Charts, City / Regional Charts)
// -----------------------------------------------------------------------------

/**
 * Pattern C: Dedicated chart section with ranked cards (#1, #2, #3, ...)
 */
@Composable
fun ChartCardsSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    if (section.items.isEmpty()) return

    val items = remember(section.items) { section.items.distinctBy { it.id } }
    val songs = remember(items) {
        items.filterIsInstance<HomeItem.SongItem>().map { it.song }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = remember(screenWidth) {
        ((screenWidth - 32.dp - 24.dp) / 2.35f).coerceIn(136.dp, 160.dp)
    }

    Column(modifier = modifier) {
        HomeSectionHeader(title = section.title)

        LazyRow(
            modifier = Modifier.carouselSwipeShield(),
            contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.id }
            ) { index, item ->
                val (title, subtitle, thumb) = when (item) {
                    is HomeItem.SongItem -> Triple(item.song.title, item.song.artist, item.song.thumbnailUrl)
                    is HomeItem.PlaylistItem -> Triple(item.playlist.name, item.playlist.uploaderName, item.playlist.thumbnailUrl)
                    is HomeItem.AlbumItem -> Triple(item.album.title, item.album.artist, item.album.thumbnailUrl)
                    else -> Triple("", "", null)
                }

                ChartCard(
                    rank = index + 1,
                    title = title,
                    subtitle = subtitle,
                    thumbnailUrl = thumb,
                    onClick = {
                        when (item) {
                            is HomeItem.SongItem -> {
                                val sIndex = songs.indexOf(item.song)
                                if (sIndex != -1) onSongClick(songs, sIndex)
                            }
                            is HomeItem.PlaylistItem -> onPlaylistClick(item.playlist)
                            is HomeItem.AlbumItem -> onAlbumClick(item.album)
                            else -> {}
                        }
                    },
                    size = cardWidth
                )
            }
        }
    }
}

// Backward-compatible alias
@Composable
fun ChartPodiumSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ChartCardsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        modifier = modifier
    )
}

// -----------------------------------------------------------------------------
// Pattern D — Video Cards Section (Watch / Music Videos / Interviews)
// -----------------------------------------------------------------------------

/**
 * Pattern D: 16:9 wider video preview cards rail
 */
@Composable
fun VideoCardsSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    if (section.items.isEmpty()) return

    val items = remember(section.items) { section.items.distinctBy { it.id } }
    val songs = remember(items) {
        items.filterIsInstance<HomeItem.SongItem>().map { it.song }
    }

    Column(modifier = modifier) {
        HomeSectionHeader(title = section.title)

        LazyRow(
            modifier = Modifier.carouselSwipeShield(),
            contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.id }
            ) { _, item ->
                val (title, creator, thumb) = when (item) {
                    is HomeItem.SongItem -> Triple(item.song.title, item.song.artist, item.song.thumbnailUrl)
                    is HomeItem.PlaylistItem -> Triple(item.playlist.name, item.playlist.uploaderName, item.playlist.thumbnailUrl)
                    is HomeItem.AlbumItem -> Triple(item.album.title, item.album.artist, item.album.thumbnailUrl)
                    else -> Triple("", "", null)
                }

                VideoContentCard(
                    title = title,
                    creator = creator,
                    thumbnailUrl = thumb,
                    onClick = {
                        when (item) {
                            is HomeItem.SongItem -> {
                                val sIndex = songs.indexOf(item.song)
                                if (sIndex != -1) onSongClick(songs, sIndex)
                            }
                            is HomeItem.PlaylistItem -> onPlaylistClick(item.playlist)
                            is HomeItem.AlbumItem -> onAlbumClick(item.album)
                            else -> {}
                        }
                    }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Community Mixes & Personalized Discovery Rails
// -----------------------------------------------------------------------------

@Composable
fun CommunityCarouselSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSavePlaylist: (PlaylistDisplayItem) -> Unit = {},
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    HorizontalSquareCardsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        onSongMoreClick = onSongMoreClick,
        modifier = modifier
    )
}

@Composable
fun PersonalizedMixCarousel(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    HorizontalSquareCardsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        onSongMoreClick = onSongMoreClick,
        modifier = modifier
    )
}

@Composable
fun GenreCarousel(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    HorizontalSquareCardsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        onSongMoreClick = onSongMoreClick,
        modifier = modifier
    )
}

// -----------------------------------------------------------------------------
// Explore Grid Section
// -----------------------------------------------------------------------------

@Composable
fun ExploreGridSection(
    section: HomeSection,
    onExploreItemClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember(section.items) {
        section.items.filterIsInstance<HomeItem.ExploreItem>()
    }
    if (items.isEmpty()) return

    Column(modifier = modifier) {
        HomeSectionHeader(title = section.title)

        LazyRow(
            modifier = Modifier.carouselSwipeShield(),
            contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            items(items, key = { it.browseId }) { item ->
                ExploreCategoryTile(
                    title = item.title,
                    iconRes = item.iconRes,
                    onClick = { onExploreItemClick(item.browseId, item.title) }
                )
            }
        }
    }
}

@Composable
private fun ExploreCategoryTile(
    title: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    val dynamicColors = LocalSonzaDynamicColors.current

    Surface(
        modifier = Modifier
            .width(150.dp)
            .height(72.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick),
        shape = RoundedCornerShape(RadiusTokens.Lg),
        color = SonzaSurfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SpacingTokens.SpaceMd, vertical = SpacingTokens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceSm)
        ) {
            if (iconRes != 0) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = dynamicColors.accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                style = SonzaTypography.TitleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = SonzaOnBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Large Card with List Section & Grid Section (Compatibility)
// -----------------------------------------------------------------------------

@Composable
fun LargeCardWithListSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    CompactSongRowsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        onSongMoreClick = onSongMoreClick,
        modifier = modifier
    )
}

@Composable
fun GridSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    HorizontalSquareCardsSection(
        section = section,
        onSongClick = onSongClick,
        onPlaylistClick = onPlaylistClick,
        onAlbumClick = onAlbumClick,
        onSongMoreClick = onSongMoreClick,
        modifier = modifier
    )
}

// -----------------------------------------------------------------------------
// Unified Master Section Dispatcher
// -----------------------------------------------------------------------------

/**
 * Dispatches a HomeSection to its optimal pattern based on its type and title cues.
 */
@Composable
fun RenderHomeSection(
    section: HomeSection,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onExploreClick: (String, String) -> Unit,
    onSongMoreClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when {
        section.type == HomeSectionType.ExploreGrid -> {
            ExploreGridSection(
                section = section,
                onExploreItemClick = onExploreClick,
                modifier = modifier
            )
        }
        isChartSection(section.title) -> {
            ChartCardsSection(
                section = section,
                onSongClick = onSongClick,
                onPlaylistClick = onPlaylistClick,
                onAlbumClick = onAlbumClick,
                modifier = modifier
            )
        }
        isVideoSection(section.title) -> {
            VideoCardsSection(
                section = section,
                onSongClick = onSongClick,
                onPlaylistClick = onPlaylistClick,
                onAlbumClick = onAlbumClick,
                modifier = modifier
            )
        }
        isCompactListSection(section) -> {
            CompactSongRowsSection(
                section = section,
                onSongClick = onSongClick,
                onPlaylistClick = onPlaylistClick,
                onAlbumClick = onAlbumClick,
                onSongMoreClick = onSongMoreClick,
                modifier = modifier
            )
        }
        else -> {
            HorizontalSquareCardsSection(
                section = section,
                onSongClick = onSongClick,
                onPlaylistClick = onPlaylistClick,
                onAlbumClick = onAlbumClick,
                onSongMoreClick = onSongMoreClick,
                modifier = modifier
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Helper Classification Functions
// -----------------------------------------------------------------------------

private fun isChartSection(title: String): Boolean {
    val t = title.lowercase()
    return t.contains("chart") || t.contains("top 100") || t.contains("top 50") ||
            t.contains("billboard") || t.contains("ranking") || t.contains("trending tracks")
}

private fun isVideoSection(title: String): Boolean {
    val t = title.lowercase()
    return t.contains("video") || t.contains("watch") || t.contains("live performance") ||
            t.contains("interview") || t.contains("episode")
}

private fun isCompactListSection(section: HomeSection): Boolean {
    val t = section.title.lowercase()
    return section.type == HomeSectionType.VerticalList ||
            section.type == HomeSectionType.QuickPicks ||
            t.contains("quick pick") ||
            t.contains("best new song") ||
            t.contains("trending song") ||
            t.contains("top song") ||
            t.contains("hit song") ||
            t.contains("fresh find")
}
