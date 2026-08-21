package com.sonza.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koin.compose.viewmodel.koinViewModel
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.HomeItem
import com.sonza.app.core.model.HomeSection
import com.sonza.app.core.model.HomeSectionType
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.*
import com.sonza.app.ui.theme.*
import com.sonza.app.ui.viewmodel.PodcastsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastsScreen(
    onBackClick: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    viewModel: PodcastsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dynamicAccent = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.title,
                        style = SonzaTypography.SectionTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories) { category ->
                    val isSelected = uiState.selectedCategory == category
                    val chipBg = if (isSelected) {
                        dynamicAccent
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    }
                    val chipContentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = chipBg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectCategory(category) }
                    ) {
                        Text(
                            text = category,
                            style = SonzaTypography.LabelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = chipContentColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = Triple(uiState.isLoading, uiState.error, uiState.sections),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "podcastsContentTransition"
            ) { (isLoading, error, sections) ->
                when {
                    isLoading -> {
                        PodcastLoadingSkeleton()
                    }
                    error != null && sections.isEmpty() -> {
                        PodcastErrorState(
                            message = error,
                            onRetry = { viewModel.retry() },
                            accentColor = dynamicAccent
                        )
                    }
                    sections.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 140.dp // Ample spacing to stay clear of floating mini player
                            ),
                            verticalArrangement = Arrangement.spacedBy(28.dp)
                        ) {
                            itemsIndexed(sections, key = { index, section -> "${section.title}_$index" }) { _, section ->
                                when {
                                    section.title.contains("Featured", ignoreCase = true) -> {
                                        FeaturedPodcastsSection(
                                            section = section,
                                            onPlaylistClick = onPlaylistClick,
                                            onSongClick = onSongClick,
                                            accentColor = dynamicAccent
                                        )
                                    }
                                    section.type == HomeSectionType.QuickPicks || section.title.contains("Episodes", ignoreCase = true) -> {
                                        QuickPicksSection(
                                            section = section,
                                            onSongClick = onSongClick,
                                            onPlaylistClick = onPlaylistClick,
                                            onAlbumClick = onAlbumClick
                                        )
                                    }
                                    section.type == HomeSectionType.LargeCardWithList -> {
                                        LargeCardWithListSection(
                                            section = section,
                                            onSongClick = onSongClick,
                                            onPlaylistClick = onPlaylistClick,
                                            onAlbumClick = onAlbumClick
                                        )
                                    }
                                    section.type == HomeSectionType.Grid -> {
                                        GridSection(
                                            section = section,
                                            onSongClick = onSongClick,
                                            onPlaylistClick = onPlaylistClick,
                                            onAlbumClick = onAlbumClick
                                        )
                                    }
                                    section.type == HomeSectionType.VerticalList -> {
                                        VerticalListSection(
                                            section = section,
                                            onSongClick = onSongClick,
                                            onPlaylistClick = onPlaylistClick,
                                            onAlbumClick = onAlbumClick
                                        )
                                    }
                                    else -> {
                                        HorizontalCarouselSection(
                                            section = section,
                                            onSongClick = onSongClick,
                                            onPlaylistClick = onPlaylistClick,
                                            onAlbumClick = onAlbumClick
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        PodcastErrorState(
                            message = "No podcast content available at this time.",
                            onRetry = { viewModel.retry() },
                            accentColor = dynamicAccent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Large spotlight featured podcast cards with prominent show artwork and creator labels.
 */
@Composable
fun FeaturedPodcastsSection(
    section: HomeSection,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (section.items.isEmpty()) return
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val cardWidth = (configuration.screenWidthDp.dp * 0.72f).coerceIn(240.dp, 300.dp)

    Column(modifier = modifier) {
        HomeSectionHeader(title = section.title)

        LazyRow(
            contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceLg)
        ) {
            items(section.items, key = { it.id }) { item ->
                when (item) {
                    is HomeItem.PlaylistItem -> {
                        val playlist = item.playlist
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isPressed) 0.96f else 1f,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
                            label = "featuredPodcastScale"
                        )

                        Column(
                            modifier = Modifier
                                .width(cardWidth)
                                .scale(scale)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onPlaylistClick(playlist) }
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(playlist.thumbnailUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = playlist.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Subtle dark bottom gradient for text contrast
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                                                startY = 180f
                                            )
                                        )
                                )

                                // Floating Play badge
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                        .size(40.dp),
                                    shape = CircleShape,
                                    color = accentColor,
                                    shadowElevation = 6.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Show",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = playlist.name,
                                style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!playlist.uploaderName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = playlist.uploaderName,
                                    style = SonzaTypography.BodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    is HomeItem.SongItem -> {
                        val song = item.song
                        Column(
                            modifier = Modifier
                                .width(cardWidth)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSongClick(listOf(song), 0) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(song.thumbnailUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = song.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = song.title,
                                style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = song.artist,
                                style = SonzaTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

/**
 * Native shimmer loading skeleton matching podcast rails and featured cards.
 */
@Composable
private fun PodcastLoadingSkeleton() {
    ShimmerContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Featured Card Rail Skeleton
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeaderSkeleton(modifier = Modifier.padding(horizontal = 16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    userScrollEnabled = false
                ) {
                    items(3) {
                        Column(modifier = Modifier.width(240.dp)) {
                            ShimmerBox(
                                width = 240.dp,
                                height = 240.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ShimmerBox(width = 180.dp, height = 18.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerBox(width = 120.dp, height = 14.dp)
                        }
                    }
                }
            }

            // Popular Rail Skeleton
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeaderSkeleton(modifier = Modifier.padding(horizontal = 16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    userScrollEnabled = false
                ) {
                    items(4) {
                        HomeCardSkeleton()
                    }
                }
            }

            // Episode List Skeleton
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeaderSkeleton()
                repeat(3) {
                    MusicCardSkeleton()
                }
            }
        }
    }
}

/**
 * Centered empty / error state with retry action.
 */
@Composable
private fun PodcastErrorState(
    message: String,
    onRetry: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Podcasts,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Podcasts",
                style = SonzaTypography.Headline.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = message,
                style = SonzaTypography.BodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
