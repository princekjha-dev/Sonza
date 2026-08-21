package com.sonza.app.ui.screens.migration

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.Song
import com.sonza.app.data.migration.model.DuplicateStrategy
import com.sonza.app.data.migration.model.MatchConfidence
import com.sonza.app.data.migration.model.MigrationRecord
import com.sonza.app.data.migration.model.MigrationSource
import com.sonza.app.data.migration.model.TrackMatchResult
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.viewmodel.MigrationStep
import com.sonza.app.ui.viewmodel.PlaylistMigrationUiState
import com.sonza.app.ui.viewmodel.PlaylistMigrationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigratePlaylistsScreen(
    viewModel: PlaylistMigrationViewModel,
    onNavigateBack: () -> Unit,
    onOpenPlaylist: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        containerColor = SonzaColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.step) {
                            MigrationStep.LANDING -> "Migrate Playlists"
                            MigrationStep.INPUT_URL -> "Import from ${uiState.selectedSource?.displayName ?: "Service"}"
                            MigrationStep.ANALYZING -> "Analyzing Tracks"
                            MigrationStep.REVIEW_MATCHES -> "Review Matches"
                            MigrationStep.DUPLICATE_PROMPT -> "Playlist Exists"
                            MigrationStep.MIGRATING -> "Migrating Playlist"
                            MigrationStep.COMPLETED -> "Migration Complete"
                            MigrationStep.HISTORY -> "Migration History"
                        },
                        style = SonzaTypography.TitleMedium,
                        color = SonzaOnBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (uiState.step) {
                            MigrationStep.LANDING -> onNavigateBack()
                            MigrationStep.INPUT_URL, MigrationStep.HISTORY -> viewModel.setStep(MigrationStep.LANDING)
                            MigrationStep.REVIEW_MATCHES -> viewModel.setStep(MigrationStep.INPUT_URL)
                            MigrationStep.DUPLICATE_PROMPT -> viewModel.setStep(MigrationStep.REVIEW_MATCHES)
                            MigrationStep.COMPLETED -> {
                                viewModel.resetState()
                                viewModel.setStep(MigrationStep.LANDING)
                            }
                            else -> onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SonzaOnBackground
                        )
                    }
                },
                actions = {
                    if (uiState.step == MigrationStep.LANDING && uiState.history.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setStep(MigrationStep.HISTORY) }) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = "Migration History",
                                tint = SonzaColors.Primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SonzaColors.Background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 4 } togetherWith fadeOut() + slideOutVertically { -it / 4 }
                },
                label = "MigrationStepTransition"
            ) { step ->
                when (step) {
                    MigrationStep.LANDING -> MigrationLandingView(
                        onSelectSource = { viewModel.onSelectSource(it) },
                        onImportFile = { viewModel.startFileImport(it) },
                        history = uiState.history,
                        onViewHistory = { viewModel.setStep(MigrationStep.HISTORY) },
                        onOpenPlaylist = onOpenPlaylist
                    )

                    MigrationStep.INPUT_URL -> InputUrlView(
                        source = uiState.selectedSource ?: MigrationSource.SPOTIFY,
                        url = uiState.urlInput,
                        onUrlChange = { viewModel.onUrlChanged(it) },
                        onStartImport = { viewModel.startUrlImport() },
                        onImportFile = { viewModel.startFileImport(it) }
                    )

                    MigrationStep.ANALYZING -> AnalyzingProgressView(
                        current = uiState.progressCurrent,
                        total = uiState.progressTotal,
                        statusText = uiState.currentAnalyzingTitle
                    )

                    MigrationStep.REVIEW_MATCHES -> ReviewMatchesView(
                        uiState = uiState,
                        onFilterSelected = { viewModel.setReviewFilter(it) },
                        onToggleSkip = { viewModel.toggleSkipTrack(it) },
                        onManualSearch = { viewModel.openManualSearch(it) },
                        onProceed = { viewModel.proceedFromReview() }
                    )

                    MigrationStep.DUPLICATE_PROMPT -> DuplicateResolutionView(
                        playlistTitle = uiState.parsedPlaylist?.title ?: "Playlist",
                        onChooseStrategy = { strategy ->
                            if (strategy == DuplicateStrategy.CANCEL) {
                                viewModel.setStep(MigrationStep.REVIEW_MATCHES)
                            } else {
                                viewModel.startMigrationExecution(strategy)
                            }
                        }
                    )

                    MigrationStep.MIGRATING -> MigratingProgressView(
                        playlistTitle = uiState.parsedPlaylist?.title ?: "Playlist",
                        current = uiState.progressCurrent,
                        total = uiState.progressTotal
                    )

                    MigrationStep.COMPLETED -> MigrationCompleteView(
                        record = uiState.migrationRecord,
                        onOpenPlaylist = {
                            val targetId = uiState.targetPlaylistId
                            if (targetId != null) {
                                onOpenPlaylist(targetId)
                            } else {
                                onNavigateBack()
                            }
                        },
                        onDone = {
                            viewModel.resetState()
                            onNavigateBack()
                        }
                    )

                    MigrationStep.HISTORY -> MigrationHistoryView(
                        history = uiState.history,
                        onOpenPlaylist = onOpenPlaylist
                    )
                }
            }

            // Manual Search Dialog
            if (uiState.manualSearchTargetIndex != null) {
                ManualMatchSearchDialog(
                    uiState = uiState,
                    onDismiss = { viewModel.closeManualSearch() },
                    onQueryChange = { viewModel.onManualSearchQueryChange(it) },
                    onSearch = { viewModel.performManualSearch() },
                    onSelectSong = { song ->
                        val targetIndex = uiState.manualSearchTargetIndex ?: return@ManualMatchSearchDialog
                        viewModel.selectCandidateForTrack(targetIndex, song)
                    }
                )
            }
        }
    }
}

/**
 * Step 1: Landing screen displaying supported music services.
 */
@Composable
private fun MigrationLandingView(
    onSelectSource: (MigrationSource) -> Unit,
    onImportFile: (android.net.Uri) -> Unit,
    history: List<MigrationRecord>,
    onViewHistory: () -> Unit,
    onOpenPlaylist: (String) -> Unit
) {
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onImportFile(uri)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.SpaceLg),
        contentPadding = PaddingValues(vertical = SpacingTokens.SpaceMd)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = SpacingTokens.SpaceLg)) {
                Text(
                    text = "Bring your music to Sonza",
                    style = SonzaTypography.BodyMedium,
                    color = SonzaOnBackground.copy(alpha = 0.70f)
                )
            }
        }

        // Supported Services Section
        item {
            Text(
                text = "Select Source Service",
                style = SonzaTypography.SectionTitle,
                color = SonzaOnBackground,
                modifier = Modifier.padding(bottom = SpacingTokens.SpaceMd)
            )
        }

        val services = listOf(
            MigrationSource.SPOTIFY,
            MigrationSource.YOUTUBE_MUSIC,
            MigrationSource.YOUTUBE,
            MigrationSource.FILE_EXPORT,
            MigrationSource.APPLE_MUSIC,
            MigrationSource.AMAZON_MUSIC,
            MigrationSource.DEEZER,
            MigrationSource.TIDAL
        )

        items(services) { source ->
            ServiceCard(
                source = source,
                onClick = {
                    if (source == MigrationSource.FILE_EXPORT) {
                        filePicker.launch("*/*")
                    } else {
                        onSelectSource(source)
                    }
                }
            )
            Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        }

        // Recent Migrations Section
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.SpaceLg, bottom = SpacingTokens.SpaceSm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Migrations",
                        style = SonzaTypography.SectionTitle,
                        color = SonzaOnBackground
                    )
                    Text(
                        text = "See All",
                        style = SonzaTypography.LabelLarge,
                        color = SonzaColors.Primary,
                        modifier = Modifier.clickable { onViewHistory() }
                    )
                }
            }

            items(history.take(3)) { record ->
                MigrationHistoryCard(record = record, onClick = {
                    record.targetPlaylistId?.let(onOpenPlaylist)
                })
                Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
            }
        }
    }
}

@Composable
private fun ServiceCard(
    source: MigrationSource,
    onClick: () -> Unit
) {
    val serviceColor = when (source) {
        MigrationSource.SPOTIFY -> Color(0xFF1DB954)
        MigrationSource.YOUTUBE_MUSIC, MigrationSource.YOUTUBE -> Color(0xFFFF0000)
        MigrationSource.FILE_EXPORT -> Color(0xFF4A90E2)
        MigrationSource.APPLE_MUSIC -> Color(0xFFFA243C)
        MigrationSource.AMAZON_MUSIC -> Color(0xFF00A8E1)
        MigrationSource.DEEZER -> Color(0xFFA238FF)
        MigrationSource.TIDAL -> Color(0xFF000000)
    }

    val serviceIcon: ImageVector = when (source) {
        MigrationSource.SPOTIFY -> Icons.Rounded.MusicNote
        MigrationSource.YOUTUBE_MUSIC -> Icons.Rounded.PlayCircleFilled
        MigrationSource.YOUTUBE -> Icons.Rounded.SmartDisplay
        MigrationSource.FILE_EXPORT -> Icons.Rounded.InsertDriveFile
        MigrationSource.APPLE_MUSIC -> Icons.Rounded.GraphicEq
        MigrationSource.AMAZON_MUSIC -> Icons.Rounded.CloudDownload
        MigrationSource.DEEZER -> Icons.Rounded.LibraryMusic
        MigrationSource.TIDAL -> Icons.Rounded.Headset
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = SonzaSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            SonzaOutline.copy(alpha = if (source.isSupported) 0.35f else 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(serviceColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = serviceIcon,
                    contentDescription = source.displayName,
                    tint = if (source.isSupported) serviceColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = source.displayName,
                        style = SonzaTypography.BodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (source.isSupported) SonzaOnBackground else SonzaOnBackground.copy(alpha = 0.50f)
                    )
                    if (!source.isSupported) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2C2C2E)
                        ) {
                            Text(
                                text = "Coming soon",
                                style = SonzaTypography.LabelSmall,
                                color = Color(0xFF8E8E93),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = source.description,
                    style = SonzaTypography.BodySmall,
                    color = SonzaOnBackground.copy(alpha = 0.60f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Select",
                tint = if (source.isSupported) SonzaOnBackground.copy(alpha = 0.60f) else Color.Transparent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Step 2: Input URL view with paste and auto-clipboard detection.
 */
@Composable
private fun InputUrlView(
    source: MigrationSource,
    url: String,
    onUrlChange: (String) -> Unit,
    onStartImport: () -> Unit,
    onImportFile: (android.net.Uri) -> Unit
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onImportFile(uri)
    }

    LaunchedEffect(Unit) {
        val clipText = clipboard.getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()
        if (!clipText.isNullOrBlank() && clipText.startsWith("http") && url.isBlank()) {
            onUrlChange(clipText)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.SpaceLg)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
    ) {
        Text(
            text = "Paste playlist link",
            style = SonzaTypography.TitleMedium,
            fontWeight = FontWeight.Bold,
            color = SonzaOnBackground
        )

        Text(
            text = "Paste a public ${source.displayName} playlist or album link below:",
            style = SonzaTypography.BodyMedium,
            color = SonzaOnBackground.copy(alpha = 0.70f)
        )

        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            placeholder = { Text("https://...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            trailingIcon = {
                if (url.isNotBlank()) {
                    IconButton(onClick = { onUrlChange("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                    }
                } else {
                    IconButton(onClick = {
                        scope.launch {
                            val clipText = clipboard.getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()
                            if (!clipText.isNullOrBlank()) {
                                onUrlChange(clipText)
                            }
                        }
                    }) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste", tint = SonzaColors.Primary)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SonzaColors.Primary,
                unfocusedBorderColor = SonzaOutline.copy(alpha = 0.40f)
            )
        )

        Button(
            onClick = onStartImport,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SonzaColors.Primary
            ),
            enabled = url.isNotBlank()
        ) {
            Text("Import Playlist", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { filePicker.launch("*/*") },
            color = SonzaSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Rounded.InsertDriveFile, contentDescription = null, tint = SonzaColors.Primary)
                Column {
                    Text("Or import from file", style = SonzaTypography.BodyMedium, fontWeight = FontWeight.SemiBold, color = SonzaOnBackground)
                    Text("Supports M3U, JSON, CSV, and TXT files", style = SonzaTypography.BodySmall, color = SonzaOnBackground.copy(alpha = 0.60f))
                }
            }
        }
    }
}

/**
 * Step 3: Analyzing & Progress screen.
 */
@Composable
private fun AnalyzingProgressView(
    current: Int,
    total: Int,
    statusText: String
) {
    val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.SpaceLg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = { if (total > 0) progress else 0.5f },
            modifier = Modifier.size(72.dp),
            color = SonzaColors.Primary,
            trackColor = SonzaSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Analyzing Tracks",
            style = SonzaTypography.TitleLarge,
            fontWeight = FontWeight.Bold,
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (total > 0) {
            Text(
                text = "$current / $total tracks",
                style = SonzaTypography.BodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = SonzaColors.Primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = statusText,
            style = SonzaTypography.BodySmall,
            color = SonzaOnBackground.copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Step 4: Review Matches Screen.
 */
@Composable
private fun ReviewMatchesView(
    uiState: PlaylistMigrationUiState,
    onFilterSelected: (MatchConfidence?) -> Unit,
    onToggleSkip: (Int) -> Unit,
    onManualSearch: (Int) -> Unit,
    onProceed: () -> Unit
) {
    val playlist = uiState.parsedPlaylist
    val matches = uiState.matchResults

    val perfectCount = matches.count { it.confidence == MatchConfidence.PERFECT_MATCH && !it.isSkipped }
    val possibleCount = matches.count { it.confidence == MatchConfidence.POSSIBLE_MATCH && !it.isSkipped }
    val unavailableCount = matches.count { (it.confidence == MatchConfidence.UNAVAILABLE || it.isSkipped) }

    val filteredMatches = matches.mapIndexed { index, item -> index to item }.filter { (_, item) ->
        when (uiState.activeReviewFilter) {
            null -> true
            MatchConfidence.PERFECT_MATCH -> item.confidence == MatchConfidence.PERFECT_MATCH && !item.isSkipped
            MatchConfidence.POSSIBLE_MATCH -> item.confidence == MatchConfidence.POSSIBLE_MATCH && !item.isSkipped
            MatchConfidence.UNAVAILABLE -> item.confidence == MatchConfidence.UNAVAILABLE || item.isSkipped
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.SpaceLg)
    ) {
        // Summary Header Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpacingTokens.SpaceSm),
            color = SonzaSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = playlist?.title ?: "Imported Playlist",
                    style = SonzaTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${matches.size} tracks found",
                    style = SonzaTypography.BodySmall,
                    color = SonzaOnBackground.copy(alpha = 0.60f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MatchStatBadge(label = "Matched", count = perfectCount, color = Color(0xFF34C759))
                    MatchStatBadge(label = "Review", count = possibleCount, color = Color(0xFFFF9500))
                    MatchStatBadge(label = "Unavailable", count = unavailableCount, color = Color(0xFFFF3B30))
                }
            }
        }

        // Filter Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpacingTokens.SpaceSm),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.activeReviewFilter == null,
                    onClick = { onFilterSelected(null) },
                    label = { Text("All (${matches.size})") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.activeReviewFilter == MatchConfidence.PERFECT_MATCH,
                    onClick = { onFilterSelected(MatchConfidence.PERFECT_MATCH) },
                    label = { Text("✓ Matched ($perfectCount)") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.activeReviewFilter == MatchConfidence.POSSIBLE_MATCH,
                    onClick = { onFilterSelected(MatchConfidence.POSSIBLE_MATCH) },
                    label = { Text("⚠ Review ($possibleCount)") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.activeReviewFilter == MatchConfidence.UNAVAILABLE,
                    onClick = { onFilterSelected(MatchConfidence.UNAVAILABLE) },
                    label = { Text("✕ Unavailable ($unavailableCount)") }
                )
            }
        }

        // Match Results List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredMatches, key = { (idx, item) -> "${idx}_${item.sourceTrack.title}" }) { (originalIndex, result) ->
                MatchResultRow(
                    result = result,
                    onToggleSkip = { onToggleSkip(originalIndex) },
                    onManualSearch = { onManualSearch(originalIndex) }
                )
            }
        }

        // Action Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpacingTokens.SpaceMd),
            color = Color.Transparent
        ) {
            Button(
                onClick = onProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SonzaColors.Primary
                )
            ) {
                Text(
                    text = "Migrate ${perfectCount + possibleCount} Tracks",
                    style = SonzaTypography.BodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MatchStatBadge(label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$count $label",
            style = SonzaTypography.BodySmall,
            fontWeight = FontWeight.SemiBold,
            color = SonzaOnBackground.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun MatchResultRow(
    result: TrackMatchResult,
    onToggleSkip: () -> Unit,
    onManualSearch: () -> Unit
) {
    val isSkipped = result.isSkipped
    val isMatched = result.matchedSong != null && !isSkipped

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = SonzaSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.75.dp,
            if (isSkipped) Color.Gray.copy(alpha = 0.2f)
            else when (result.confidence) {
                MatchConfidence.PERFECT_MATCH -> Color(0xFF34C759).copy(alpha = 0.35f)
                MatchConfidence.POSSIBLE_MATCH -> Color(0xFFFF9500).copy(alpha = 0.35f)
                MatchConfidence.UNAVAILABLE -> Color(0xFFFF3B30).copy(alpha = 0.35f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork or Icon
            if (result.matchedSong?.thumbnailUrl != null && !isSkipped) {
                AsyncImage(
                    model = result.matchedSong.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSkipped) Icons.Rounded.Block else Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.sourceTrack.title,
                    style = SonzaTypography.BodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSkipped) SonzaOnBackground.copy(alpha = 0.40f) else SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = result.sourceTrack.artist,
                    style = SonzaTypography.BodySmall,
                    color = SonzaOnBackground.copy(alpha = 0.60f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Match Status Line
                if (!isSkipped && result.matchedSong != null) {
                    val statusText = if (result.confidence == MatchConfidence.PERFECT_MATCH) "✓ Perfect Match" else "⚠ Review Match"
                    val statusColor = if (result.confidence == MatchConfidence.PERFECT_MATCH) Color(0xFF34C759) else Color(0xFFFF9500)
                    Text(
                        text = "$statusText • ${result.matchedSong.title}",
                        style = SonzaTypography.LabelSmall,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (isSkipped) {
                    Text(text = "✕ Skipped", style = SonzaTypography.LabelSmall, color = Color.Gray)
                } else {
                    Text(text = "✕ Not Available", style = SonzaTypography.LabelSmall, color = Color(0xFFFF3B30))
                }
            }

            // Action Buttons (Search & Skip)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onManualSearch) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search Sonza",
                        tint = SonzaColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onToggleSkip) {
                    Icon(
                        imageVector = if (isSkipped) Icons.Rounded.Undo else Icons.Rounded.DeleteOutline,
                        contentDescription = if (isSkipped) "Restore" else "Skip",
                        tint = if (isSkipped) SonzaColors.Primary else SonzaOnBackground.copy(alpha = 0.60f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Step 5: Duplicate Resolution View.
 */
@Composable
private fun DuplicateResolutionView(
    playlistTitle: String,
    onChooseStrategy: (DuplicateStrategy) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.SpaceLg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = Color(0xFFFF9500),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Playlist Already Exists",
            style = SonzaTypography.TitleLarge,
            fontWeight = FontWeight.Bold,
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A playlist named \"$playlistTitle\" is already in your Sonza library. How would you like to proceed?",
            style = SonzaTypography.BodyMedium,
            color = SonzaOnBackground.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onChooseStrategy(DuplicateStrategy.CREATE_NEW_COPY) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SonzaColors.Primary)
        ) {
            Text("Create a New Copy", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { onChooseStrategy(DuplicateStrategy.ADD_MISSING) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Add Missing Tracks", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { onChooseStrategy(DuplicateStrategy.REPLACE) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Replace Existing Playlist", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { onChooseStrategy(DuplicateStrategy.CANCEL) }
        ) {
            Text("Cancel", style = SonzaTypography.BodyMedium, color = SonzaOnBackground.copy(alpha = 0.70f))
        }
    }
}

/**
 * Step 6: Migrating Progress.
 */
@Composable
private fun MigratingProgressView(
    playlistTitle: String,
    current: Int,
    total: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.SpaceLg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = SonzaColors.Primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Migrating $playlistTitle",
            style = SonzaTypography.TitleLarge,
            fontWeight = FontWeight.Bold,
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Writing tracks and playlist structure to Sonza...",
            style = SonzaTypography.BodyMedium,
            color = SonzaOnBackground.copy(alpha = 0.70f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Step 7: Migration Complete Screen.
 */
@Composable
private fun MigrationCompleteView(
    record: MigrationRecord?,
    onOpenPlaylist: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.SpaceLg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF34C759).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF34C759),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Migration Complete",
            style = SonzaTypography.TitleLarge,
            fontWeight = FontWeight.Bold,
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = record?.playlistTitle ?: "Your Playlist",
            style = SonzaTypography.BodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = SonzaColors.Primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Breakdown Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SonzaSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatRow(label = "Tracks found", value = "${record?.totalTracks ?: 0}")
                StatRow(label = "Tracks added", value = "${record?.matchedCount ?: 0}", color = Color(0xFF34C759))
                if ((record?.skippedCount ?: 0) > 0) {
                    StatRow(label = "Tracks skipped", value = "${record?.skippedCount ?: 0}", color = Color(0xFFFF9500))
                }
                if ((record?.unavailableCount ?: 0) > 0) {
                    StatRow(label = "Tracks unavailable", value = "${record?.unavailableCount ?: 0}", color = Color(0xFFFF3B30))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onOpenPlaylist,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SonzaColors.Primary)
        ) {
            Text("Open Playlist", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Done", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color = SonzaOnBackground) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = SonzaTypography.BodyMedium, color = SonzaOnBackground.copy(alpha = 0.70f))
        Text(text = value, style = SonzaTypography.BodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

/**
 * Migration History View.
 */
@Composable
private fun MigrationHistoryView(
    history: List<MigrationRecord>,
    onOpenPlaylist: (String) -> Unit
) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No migration history yet", style = SonzaTypography.BodyLarge, color = SonzaOnBackground.copy(alpha = 0.60f))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SpacingTokens.SpaceLg),
            contentPadding = PaddingValues(vertical = SpacingTokens.SpaceMd),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceSm)
        ) {
            items(history) { record ->
                MigrationHistoryCard(record = record, onClick = {
                    record.targetPlaylistId?.let(onOpenPlaylist)
                })
            }
        }
    }
}

@Composable
private fun MigrationHistoryCard(
    record: MigrationRecord,
    onClick: () -> Unit
) {
    val dateStr = remember(record.timestamp) {
        SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(record.timestamp))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = SonzaSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.sourceName} → Sonza",
                    style = SonzaTypography.LabelSmall,
                    color = SonzaColors.Primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = record.playlistTitle,
                    style = SonzaTypography.BodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground
                )
                Text(
                    text = "${record.matchedCount} of ${record.totalTracks} imported • $dateStr",
                    style = SonzaTypography.BodySmall,
                    color = SonzaOnBackground.copy(alpha = 0.60f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SonzaOnBackground.copy(alpha = 0.50f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Manual Track Search & Resolution Dialog.
 */
@Composable
private fun ManualMatchSearchDialog(
    uiState: PlaylistMigrationUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectSong: (Song) -> Unit
) {
    val targetItem = uiState.manualSearchTargetIndex?.let { uiState.matchResults.getOrNull(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Search Sonza Catalog", style = SonzaTypography.TitleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (targetItem != null) {
                    Text(
                        text = "Original: ${targetItem.sourceTrack.title} - ${targetItem.sourceTrack.artist}",
                        style = SonzaTypography.BodySmall,
                        color = SonzaOnBackground.copy(alpha = 0.70f)
                    )
                }

                OutlinedTextField(
                    value = uiState.manualSearchQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search song or artist...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search", tint = SonzaColors.Primary)
                        }
                    }
                )

                if (uiState.isSearchingManual) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = SonzaColors.Primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(uiState.manualSearchResults) { candidate ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectSong(candidate) },
                                color = SonzaSurface,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = candidate.thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = candidate.title,
                                            style = SonzaTypography.BodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SonzaOnBackground,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = candidate.artist,
                                            style = SonzaTypography.LabelSmall,
                                            color = SonzaOnBackground.copy(alpha = 0.60f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SonzaColors.Primary)
            }
        },
        containerColor = SonzaSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
