package com.sonza.app.ui.screens.migration

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
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
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SonzaLoadingIndicator
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.viewmodel.MigrationStep
import com.sonza.app.ui.viewmodel.PlaylistMigrationUiState
import com.sonza.app.ui.viewmodel.PlaylistMigrationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rebuilt Migrate Playlists Screen & Flow.
 *
 * Implements a premium, Apple Music-inspired dark UI with calm neutral tones,
 * clean typography, subtle Apple-style list rows, and Sonza's existing dynamic accent colors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigratePlaylistsScreen(
    viewModel: PlaylistMigrationViewModel,
    onNavigateBack: () -> Unit,
    onOpenPlaylist: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary
    val onAccentColor = dynamicColors.onAccent.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.onPrimary

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
                        style = SonzaTypography.TitleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = SonzaOnBackground
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
                                tint = accentColor
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
                        accentColor = accentColor,
                        onSelectSource = { viewModel.onSelectSource(it) },
                        onImportFile = { viewModel.startFileImport(it) },
                        history = uiState.history,
                        onViewHistory = { viewModel.setStep(MigrationStep.HISTORY) },
                        onOpenPlaylist = onOpenPlaylist
                    )

                    MigrationStep.INPUT_URL -> InputUrlView(
                        source = uiState.selectedSource ?: MigrationSource.SPOTIFY,
                        url = uiState.urlInput,
                        accentColor = accentColor,
                        onAccentColor = onAccentColor,
                        onUrlChange = { viewModel.onUrlChanged(it) },
                        onStartImport = { viewModel.startUrlImport() },
                        onImportFile = { viewModel.startFileImport(it) }
                    )

                    MigrationStep.ANALYZING -> AnalyzingProgressView(
                        current = uiState.progressCurrent,
                        total = uiState.progressTotal,
                        statusText = uiState.currentAnalyzingTitle,
                        accentColor = accentColor
                    )

                    MigrationStep.REVIEW_MATCHES -> ReviewMatchesView(
                        uiState = uiState,
                        accentColor = accentColor,
                        onAccentColor = onAccentColor,
                        onFilterSelected = { viewModel.setReviewFilter(it) },
                        onToggleSkip = { viewModel.toggleSkipTrack(it) },
                        onManualSearch = { viewModel.openManualSearch(it) },
                        onProceed = { viewModel.proceedFromReview() }
                    )

                    MigrationStep.DUPLICATE_PROMPT -> DuplicateResolutionView(
                        playlistTitle = uiState.parsedPlaylist?.title ?: "Playlist",
                        accentColor = accentColor,
                        onAccentColor = onAccentColor,
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
                        accentColor = accentColor
                    )

                    MigrationStep.COMPLETED -> MigrationCompleteView(
                        record = uiState.migrationRecord,
                        accentColor = accentColor,
                        onAccentColor = onAccentColor,
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
                        accentColor = accentColor,
                        onOpenPlaylist = onOpenPlaylist
                    )
                }
            }

            // Manual Search Dialog
            if (uiState.manualSearchTargetIndex != null) {
                ManualMatchSearchDialog(
                    uiState = uiState,
                    accentColor = accentColor,
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
 * Step 1: Landing screen displaying supported music services with Apple-style list rows.
 */
@Composable
private fun MigrationLandingView(
    accentColor: Color,
    onSelectSource: (MigrationSource) -> Unit,
    onImportFile: (Uri) -> Unit,
    history: List<MigrationRecord>,
    onViewHistory: () -> Unit,
    onOpenPlaylist: (String) -> Unit
) {
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onImportFile(uri)
    }

    val bottomSafePadding = ExpressiveBottomNavTokens.getBottomSafePadding(false) + 16.dp

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = bottomSafePadding)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Bring your music to Sonza",
                    style = SonzaTypography.Headline.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = SonzaOnBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Choose a service and transfer your library and playlists to Sonza",
                    style = SonzaTypography.BodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    ),
                    color = SonzaOnBackground.copy(alpha = 0.65f)
                )
            }
        }

        // Section Title: Select a Music Service
        item {
            Text(
                text = "Select a Music Service",
                style = SonzaTypography.TitleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = SonzaOnBackground,
                modifier = Modifier.padding(bottom = 12.dp)
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
            AppleStyleServiceRow(
                source = source,
                onClick = {
                    if (source == MigrationSource.FILE_EXPORT) {
                        filePicker.launch("*/*")
                    } else if (source.isSupported) {
                        onSelectSource(source)
                    }
                }
            )
            HorizontalDivider(
                color = SonzaOutline.copy(alpha = 0.12f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 58.dp)
            )
        }

        // Recent Migrations Section
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Migrations",
                        style = SonzaTypography.TitleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = SonzaOnBackground
                    )
                    Text(
                        text = "See All",
                        style = SonzaTypography.LabelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = accentColor,
                        modifier = Modifier.clickable { onViewHistory() }
                    )
                }
            }

            items(history.take(3)) { record ->
                MigrationHistoryCard(
                    record = record,
                    accentColor = accentColor,
                    onClick = {
                        record.targetPlaylistId?.let(onOpenPlaylist)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Apple-style clean list row for music migration services.
 */
@Composable
private fun AppleStyleServiceRow(
    source: MigrationSource,
    onClick: () -> Unit
) {
    // Keep authentic service branding colors exclusively inside the icon
    val serviceColor = when (source) {
        MigrationSource.SPOTIFY -> Color(0xFF1DB954)
        MigrationSource.YOUTUBE_MUSIC, MigrationSource.YOUTUBE -> Color(0xFFFF0000)
        MigrationSource.FILE_EXPORT -> Color(0xFF4A90E2)
        MigrationSource.APPLE_MUSIC -> Color(0xFFFA243C)
        MigrationSource.AMAZON_MUSIC -> Color(0xFF00A8E1)
        MigrationSource.DEEZER -> Color(0xFFA238FF)
        MigrationSource.TIDAL -> Color(0xFFE5E5E5)
    }

    val serviceIcon: ImageVector = when (source) {
        MigrationSource.SPOTIFY -> Icons.Rounded.MusicNote
        MigrationSource.YOUTUBE_MUSIC -> Icons.Rounded.PlayCircleFilled
        MigrationSource.YOUTUBE -> Icons.Rounded.SmartDisplay
        MigrationSource.FILE_EXPORT -> Icons.AutoMirrored.Rounded.InsertDriveFile
        MigrationSource.APPLE_MUSIC -> Icons.Rounded.GraphicEq
        MigrationSource.AMAZON_MUSIC -> Icons.Rounded.CloudDownload
        MigrationSource.DEEZER -> Icons.Rounded.LibraryMusic
        MigrationSource.TIDAL -> Icons.Rounded.Headset
    }

    val isSupported = source.isSupported

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isSupported,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 13.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Service Icon Container with authentic brand color
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSupported) serviceColor.copy(alpha = 0.14f)
                    else Color(0xFF27272A).copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = serviceIcon,
                contentDescription = source.displayName,
                tint = if (isSupported) serviceColor else Color.Gray.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.displayName,
                style = SonzaTypography.BodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = if (isSupported) SonzaOnBackground else SonzaOnBackground.copy(alpha = 0.40f)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isSupported) source.description else "Coming soon",
                style = SonzaTypography.BodySmall.copy(fontSize = 13.sp),
                color = if (isSupported) SonzaOnBackground.copy(alpha = 0.55f) else SonzaOnBackground.copy(alpha = 0.35f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trailing indicator: Chevron or Coming soon badge
        if (isSupported) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Select",
                tint = SonzaOnBackground.copy(alpha = 0.30f),
                modifier = Modifier.size(14.dp)
            )
        } else {
            Text(
                text = "Coming soon",
                style = SonzaTypography.LabelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = SonzaOnBackground.copy(alpha = 0.35f)
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
    accentColor: Color,
    onAccentColor: Color,
    onUrlChange: (String) -> Unit,
    onStartImport: () -> Unit,
    onImportFile: (Uri) -> Unit
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

    val bottomSafePadding = ExpressiveBottomNavTokens.getBottomSafePadding(false) + 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = bottomSafePadding)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Paste playlist link",
            style = SonzaTypography.TitleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Paste a public ${source.displayName} playlist or album link below:",
            style = SonzaTypography.BodyMedium.copy(fontSize = 14.sp),
            color = SonzaOnBackground.copy(alpha = 0.65f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            placeholder = { Text("https://...", color = SonzaOnBackground.copy(alpha = 0.40f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            trailingIcon = {
                if (url.isNotBlank()) {
                    IconButton(onClick = { onUrlChange("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = SonzaOnBackground.copy(alpha = 0.6f))
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
                        Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste", tint = accentColor)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = SonzaOutline.copy(alpha = 0.30f),
                focusedTextColor = SonzaOnBackground,
                unfocusedTextColor = SonzaOnBackground,
                cursorColor = accentColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartImport,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = onAccentColor
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
            border = BorderStroke(0.75.dp, SonzaOutline.copy(alpha = 0.20f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.InsertDriveFile, contentDescription = null, tint = accentColor)
                Column {
                    Text("Or import from file", style = SonzaTypography.BodyMedium, fontWeight = FontWeight.SemiBold, color = SonzaOnBackground)
                    Text("Supports M3U, JSON, CSV, and TXT files", style = SonzaTypography.BodySmall, color = SonzaOnBackground.copy(alpha = 0.55f))
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
    statusText: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SonzaLoadingIndicator(
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Analyzing Tracks",
            style = SonzaTypography.TitleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (total > 0) {
            Text(
                text = "$current / $total tracks",
                style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = statusText,
            style = SonzaTypography.BodySmall,
            color = SonzaOnBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Step 4: Review Matches Screen — Neutral Apple-style summary, dynamic filters, and clean list rows.
 */
@Composable
private fun ReviewMatchesView(
    uiState: PlaylistMigrationUiState,
    accentColor: Color,
    onAccentColor: Color,
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

    val bottomSafePadding = ExpressiveBottomNavTokens.getBottomSafePadding(false) + 12.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Neutral Summary Header (NO rainbow dots, checkmarks, or colored borders)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = playlist?.title ?: "Imported Playlist",
                style = SonzaTypography.Headline.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.4).sp
                ),
                color = SonzaOnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${matches.size} tracks found",
                style = SonzaTypography.BodySmall.copy(fontSize = 13.sp),
                color = SonzaOnBackground.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Neutral text summary breakdown: "17 Matched     2 Review     0 Unavailable"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$perfectCount Matched",
                    style = SonzaTypography.BodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = SonzaOnBackground.copy(alpha = 0.85f)
                )
                Text(
                    text = "$possibleCount Review",
                    style = SonzaTypography.BodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = SonzaOnBackground.copy(alpha = 0.85f)
                )
                Text(
                    text = "$unavailableCount Unavailable",
                    style = SonzaTypography.BodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = SonzaOnBackground.copy(alpha = 0.85f)
                )
            }
        }

        // Dynamic Accent Review Filter Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ReviewFilterPill(
                    label = "All (${matches.size})",
                    isSelected = uiState.activeReviewFilter == null,
                    accentColor = accentColor,
                    onAccentColor = onAccentColor,
                    onClick = { onFilterSelected(null) }
                )
            }
            item {
                ReviewFilterPill(
                    label = "Matched ($perfectCount)",
                    isSelected = uiState.activeReviewFilter == MatchConfidence.PERFECT_MATCH,
                    accentColor = accentColor,
                    onAccentColor = onAccentColor,
                    onClick = { onFilterSelected(MatchConfidence.PERFECT_MATCH) }
                )
            }
            item {
                ReviewFilterPill(
                    label = "Review ($possibleCount)",
                    isSelected = uiState.activeReviewFilter == MatchConfidence.POSSIBLE_MATCH,
                    accentColor = accentColor,
                    onAccentColor = onAccentColor,
                    onClick = { onFilterSelected(MatchConfidence.POSSIBLE_MATCH) }
                )
            }
            item {
                ReviewFilterPill(
                    label = "Unavailable ($unavailableCount)",
                    isSelected = uiState.activeReviewFilter == MatchConfidence.UNAVAILABLE,
                    accentColor = accentColor,
                    onAccentColor = onAccentColor,
                    onClick = { onFilterSelected(MatchConfidence.UNAVAILABLE) }
                )
            }
        }

        // Match Results List (Apple-style list rows with thin dividers)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(filteredMatches, key = { (idx, item) -> "${idx}_${item.sourceTrack.title}" }) { (originalIndex, result) ->
                MatchResultRow(
                    result = result,
                    accentColor = accentColor,
                    onToggleSkip = { onToggleSkip(originalIndex) },
                    onManualSearch = { onManualSearch(originalIndex) }
                )
                HorizontalDivider(
                    color = SonzaOutline.copy(alpha = 0.10f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 62.dp)
                )
            }
        }

        // Action Bar with Dynamic Accent CTA
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = bottomSafePadding),
            color = Color.Transparent
        ) {
            Button(
                onClick = onProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = onAccentColor
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Migrate ${perfectCount + possibleCount} Tracks",
                    style = SonzaTypography.BodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

/**
 * Filter pill chip responding dynamically to Sonza's active theme accent color.
 */
@Composable
private fun ReviewFilterPill(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onAccentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = if (isSelected) accentColor else SonzaSurface,
        border = if (!isSelected) BorderStroke(0.75.dp, SonzaOutline.copy(alpha = 0.25f)) else null,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = SonzaTypography.BodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isSelected) onAccentColor else SonzaOnBackground.copy(alpha = 0.75f)
            )
        }
    }
}

/**
 * Clean Apple-style match row without rainbow borders or colored badges.
 */
@Composable
private fun MatchResultRow(
    result: TrackMatchResult,
    accentColor: Color,
    onToggleSkip: () -> Unit,
    onManualSearch: () -> Unit
) {
    val isSkipped = result.isSkipped

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artwork thumbnail
        if (result.matchedSong?.thumbnailUrl != null && !isSkipped) {
            AsyncImage(
                model = result.matchedSong.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                .background(SonzaColors.SurfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSkipped) Icons.Rounded.Block else Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = SonzaOnBackground.copy(alpha = 0.40f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Metadata Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.sourceTrack.title,
                style = SonzaTypography.BodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = if (isSkipped) SonzaOnBackground.copy(alpha = 0.40f) else SonzaOnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = result.sourceTrack.artist,
                style = SonzaTypography.BodySmall.copy(fontSize = 13.sp),
                color = SonzaOnBackground.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Neutral Match Status Line (NO rainbow colors or badges)
            val statusLabel = when {
                isSkipped -> "Skipped"
                result.confidence == MatchConfidence.PERFECT_MATCH && result.matchedSong != null -> "Matched"
                result.confidence == MatchConfidence.POSSIBLE_MATCH && result.matchedSong != null -> "Needs review"
                else -> "Unavailable"
            }

            Text(
                text = statusLabel,
                style = SonzaTypography.LabelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = SonzaOnBackground.copy(alpha = 0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Interactive Action Buttons (Search & Delete/Restore)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(
                onClick = onManualSearch,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search match",
                    tint = SonzaOnBackground.copy(alpha = 0.60f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onToggleSkip,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isSkipped) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.DeleteOutline,
                    contentDescription = if (isSkipped) "Restore" else "Skip",
                    tint = if (isSkipped) accentColor else SonzaOnBackground.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
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
    accentColor: Color,
    onAccentColor: Color,
    onChooseStrategy: (DuplicateStrategy) -> Unit
) {
    val bottomSafePadding = ExpressiveBottomNavTokens.getBottomSafePadding(false) + 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = bottomSafePadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = SonzaOnBackground.copy(alpha = 0.75f),
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Playlist Already Exists",
            style = SonzaTypography.TitleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A playlist named \"$playlistTitle\" is already in your Sonza library. How would you like to proceed?",
            style = SonzaTypography.BodyMedium,
            color = SonzaOnBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { onChooseStrategy(DuplicateStrategy.CREATE_NEW_COPY) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = onAccentColor
            )
        ) {
            Text("Create a New Copy", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { onChooseStrategy(DuplicateStrategy.ADD_MISSING) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(percent = 50)
        ) {
            Text("Add Missing Tracks", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.SemiBold, color = SonzaOnBackground)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { onChooseStrategy(DuplicateStrategy.REPLACE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(percent = 50)
        ) {
            Text("Replace Existing Playlist", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.SemiBold, color = SonzaOnBackground)
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = { onChooseStrategy(DuplicateStrategy.CANCEL) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Cancel", style = SonzaTypography.BodyMedium, color = SonzaOnBackground.copy(alpha = 0.60f))
        }
    }
}

/**
 * Step 6: Migrating Progress.
 */
@Composable
private fun MigratingProgressView(
    playlistTitle: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SonzaLoadingIndicator(
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Migrating $playlistTitle",
            style = SonzaTypography.TitleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Writing tracks and playlist structure to Sonza...",
            style = SonzaTypography.BodyMedium,
            color = SonzaOnBackground.copy(alpha = 0.65f),
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
    accentColor: Color,
    onAccentColor: Color,
    onOpenPlaylist: () -> Unit,
    onDone: () -> Unit
) {
    val bottomSafePadding = ExpressiveBottomNavTokens.getBottomSafePadding(false) + 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = bottomSafePadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Migration Complete",
            style = SonzaTypography.TitleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = SonzaOnBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = record?.playlistTitle ?: "Your Playlist",
            style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = accentColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Breakdown Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SonzaSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.75.dp, SonzaOutline.copy(alpha = 0.20f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatRow(label = "Tracks found", value = "${record?.totalTracks ?: 0}")
                StatRow(label = "Tracks added", value = "${record?.matchedCount ?: 0}")
                if ((record?.skippedCount ?: 0) > 0) {
                    StatRow(label = "Tracks skipped", value = "${record?.skippedCount ?: 0}")
                }
                if ((record?.unavailableCount ?: 0) > 0) {
                    StatRow(label = "Tracks unavailable", value = "${record?.unavailableCount ?: 0}")
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onOpenPlaylist,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = onAccentColor
            )
        ) {
            Text("Open Playlist", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(percent = 50)
        ) {
            Text("Done", style = SonzaTypography.BodyLarge, fontWeight = FontWeight.SemiBold, color = SonzaOnBackground)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = SonzaTypography.BodyMedium, color = SonzaOnBackground.copy(alpha = 0.65f))
        Text(text = value, style = SonzaTypography.BodyMedium, fontWeight = FontWeight.Bold, color = SonzaOnBackground)
    }
}

/**
 * Migration History View.
 */
@Composable
private fun MigrationHistoryView(
    history: List<MigrationRecord>,
    accentColor: Color,
    onOpenPlaylist: (String) -> Unit
) {
    val bottomSafePadding = ExpressiveBottomNavTokens.getBottomSafePadding(false) + 16.dp

    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No migration history yet", style = SonzaTypography.BodyLarge, color = SonzaOnBackground.copy(alpha = 0.55f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = bottomSafePadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { record ->
                MigrationHistoryCard(
                    record = record,
                    accentColor = accentColor,
                    onClick = {
                        record.targetPlaylistId?.let(onOpenPlaylist)
                    }
                )
            }
        }
    }
}

@Composable
private fun MigrationHistoryCard(
    record: MigrationRecord,
    accentColor: Color,
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
        border = BorderStroke(0.75.dp, SonzaOutline.copy(alpha = 0.20f))
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
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = record.playlistTitle,
                    style = SonzaTypography.BodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${record.matchedCount} of ${record.totalTracks} imported • $dateStr",
                    style = SonzaTypography.BodySmall,
                    color = SonzaOnBackground.copy(alpha = 0.55f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = SonzaOnBackground.copy(alpha = 0.35f),
                modifier = Modifier.size(14.dp)
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
    accentColor: Color,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectSong: (Song) -> Unit
) {
    val targetItem = uiState.manualSearchTargetIndex?.let { uiState.matchResults.getOrNull(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Search Sonza Catalog", style = SonzaTypography.TitleMedium, fontWeight = FontWeight.Bold, color = SonzaOnBackground)
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
                        color = SonzaOnBackground.copy(alpha = 0.65f)
                    )
                }

                OutlinedTextField(
                    value = uiState.manualSearchQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search song or artist...", color = SonzaOnBackground.copy(alpha = 0.40f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search", tint = accentColor)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = SonzaOutline.copy(alpha = 0.30f),
                        focusedTextColor = SonzaOnBackground,
                        unfocusedTextColor = SonzaOnBackground,
                        cursorColor = accentColor
                    )
                )

                if (uiState.isSearchingManual) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        SonzaLoadingIndicator(modifier = Modifier.size(36.dp))
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
                                color = SonzaColors.SurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = candidate.thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(6.dp)),
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
                Text("Close", color = accentColor, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = SonzaSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
