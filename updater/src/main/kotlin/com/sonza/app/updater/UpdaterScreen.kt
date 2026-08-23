package com.sonza.app.updater

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CardShape = RoundedCornerShape(24.dp)
private val ButtonShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdaterScreen(
    currentVersionCode: Int,
    currentVersionName: String,
    viewModel: UpdateViewModel,
    onBackClick: () -> Unit,
    onViewPreviousUpdates: (() -> Unit)? = null,
    appLogo: (@Composable () -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()

    var showHistorySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Compute previous releases strictly older than current installed version
    val previousReleases = remember(uiState.allReleases, currentVersionName) {
        uiState.allReleases.filter {
            VersionComparator.compare(it.versionName, currentVersionName) < 0
        }
    }

    // Auto-check on first launch if not already checked
    LaunchedEffect(currentVersionName, currentVersionCode) {
        if (uiState.lastCheckedTimestamp == null) {
            viewModel.checkForUpdate(
                currentVersionName = currentVersionName,
                currentVersionCode = currentVersionCode
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Update",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onBackClick)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Main Update Management Card
            item {
                MainUpdateCard(
                    installedVersionName = currentVersionName,
                    uiState = uiState,
                    downloadState = downloadState,
                    appLogo = appLogo,
                    onCheckAgain = {
                        viewModel.checkForUpdate(
                            currentVersionName = currentVersionName,
                            currentVersionCode = currentVersionCode
                        )
                    },
                    onUpdateNow = { info -> viewModel.downloadAndInstallUpdate(info) },
                    onDismissError = { viewModel.resetUpdateState() }
                )
            }

            // Secondary Action: View Previous Updates (shown ONLY if historical releases exist)
            if (previousReleases.isNotEmpty()) {
                item {
                    PreviousUpdatesButton(
                        releaseCount = previousReleases.size,
                        onClick = {
                            if (onViewPreviousUpdates != null) {
                                onViewPreviousUpdates()
                            } else {
                                showHistorySheet = true
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Modal Bottom Sheet for historical releases if previous versions exist and no dedicated route callback is supplied
    if (showHistorySheet && previousReleases.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Update History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${previousReleases.size} ${if (previousReleases.size == 1) "version" else "versions"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(previousReleases) { release ->
                        HistoricalReleaseCard(release = release)
                    }
                }
            }
        }
    }
}

/**
 * Main Update Card implementing clean information hierarchy:
 * 1. App Logo Badge & Version
 * 2. Update status / metadata
 * 3. Release notes (for relevant update only)
 * 4. Primary action button / Download progress
 */
@Composable
private fun MainUpdateCard(
    installedVersionName: String,
    uiState: SystemUpdateUiState,
    downloadState: DownloadState,
    appLogo: (@Composable () -> Unit)?,
    onCheckAgain: () -> Unit,
    onUpdateNow: (UpdateInfo) -> Unit,
    onDismissError: () -> Unit
) {
    Surface(
        shape = CardShape,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                // 1. Download in progress
                downloadState is DownloadState.Downloading -> {
                    DownloadingStateContent(
                        targetVersionName = uiState.nextUpdate?.versionName ?: installedVersionName,
                        downloadState = downloadState,
                        appLogo = appLogo
                    )
                }

                // 2. Download error
                downloadState is DownloadState.Error -> {
                    ErrorStateContent(
                        title = "Download Failed",
                        description = downloadState.message,
                        onRetry = {
                            uiState.nextUpdate?.let(onUpdateNow) ?: onCheckAgain()
                        }
                    )
                }

                // 3. Update available (Immediate next version only)
                uiState.hasUpdate && uiState.nextUpdate != null -> {
                    UpdateAvailableContent(
                        installedVersionName = installedVersionName,
                        updateInfo = uiState.nextUpdate,
                        releaseNotes = uiState.nextReleaseNotes,
                        appLogo = appLogo,
                        onUpdateNow = { onUpdateNow(uiState.nextUpdate) }
                    )
                }

                // 4. Error state
                uiState.errorMessage != null && !uiState.isChecking -> {
                    ErrorStateContent(
                        title = "Couldn't check for updates",
                        description = "Check your internet connection and try again.",
                        onRetry = onCheckAgain
                    )
                }

                // 5. Up to date / Checking for updates
                else -> {
                    UpToDateContent(
                        installedVersionName = installedVersionName,
                        lastCheckedTimestamp = uiState.lastCheckedTimestamp,
                        isChecking = uiState.isChecking,
                        appLogo = appLogo,
                        onCheckAgain = onCheckAgain
                    )
                }
            }
        }
    }
}

@Composable
private fun UpToDateContent(
    installedVersionName: String,
    lastCheckedTimestamp: Long?,
    isChecking: Boolean = false,
    appLogo: (@Composable () -> Unit)?,
    onCheckAgain: () -> Unit
) {
    // Top official Sonza app logo badge
    UpdateIconBadge(
        isChecking = false,
        appLogo = appLogo
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Sonza $installedVersionName",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    if (lastCheckedTimestamp != null) {
        val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        Text(
            text = "Last checked: ${timeFormat.format(Date(lastCheckedTimestamp))}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Success Status Pill
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF4CAF50).copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "You're using the latest version",
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Button(
        onClick = onCheckAgain,
        enabled = !isChecking,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isChecking) {
            UpdaterLoadingIndicator(
                size = 20.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Checking...",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        } else {
            Text(
                text = "Check Again",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    installedVersionName: String,
    updateInfo: UpdateInfo,
    releaseNotes: Release?,
    appLogo: (@Composable () -> Unit)?,
    onUpdateNow: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Row: Sonza Logo Badge + Version + Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UpdateIconBadge(
                modifier = Modifier.size(56.dp),
                isChecking = false,
                appLogo = appLogo
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sonza ${updateInfo.versionName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (updateInfo.size.isNotBlank()) {
                    Text(
                        text = updateInfo.size,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "A new version of Sonza is available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(20.dp))

        // What's New Section for this specific version only
        Text(
            text = "What's New",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (releaseNotes?.date?.isNotBlank() == true) {
            Text(
                text = releaseNotes.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Release Notes Bullet Points
        val rawChangelog = releaseNotes?.description?.ifBlank { updateInfo.changelog } ?: updateInfo.changelog
        val bulletLines = rawChangelog
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (bulletLines.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bulletLines.forEach { line ->
                    val cleanLine = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = cleanLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Performance improvements and bug fixes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (updateInfo.releaseNotesUrl.isNotBlank()) {
            TextButton(
                onClick = { uriHandler.openUri(updateInfo.releaseNotesUrl) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Learn More...",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Action Button
        Button(
            onClick = onUpdateNow,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Update Now",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun ErrorStateContent(
    title: String,
    description: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.error
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 6.dp)
    )

    Spacer(modifier = Modifier.height(28.dp))

    Button(
        onClick = onRetry,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = "Try Again",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun DownloadingStateContent(
    targetVersionName: String,
    downloadState: DownloadState.Downloading,
    appLogo: (@Composable () -> Unit)?
) {
    UpdateIconBadge(
        isChecking = false,
        appLogo = appLogo
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Downloading Sonza $targetVersionName",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Progress percentage and MBs
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val downloadedMb = String.format(Locale.US, "%.1f", downloadState.bytesDownloaded / (1024f * 1024f))
        val totalMb = String.format(Locale.US, "%.1f", downloadState.totalBytes / (1024f * 1024f))
        Text(
            text = "$downloadedMb MB / $totalMb MB",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${(downloadState.progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    LinearProgressIndicator(
        progress = { downloadState.progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}

/**
 * App Logo Badge replacing the generic download icon.
 * Displays the official Sonza logo inside a modern squircle container with gradient backdrop.
 */
@Composable
private fun UpdateIconBadge(
    modifier: Modifier = Modifier,
    isChecking: Boolean = false,
    appLogo: (@Composable () -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "IconPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val badgeShape = RoundedCornerShape(20.dp)
    val context = LocalContext.current

    // Dynamically retrieve the real launcher icon as fallback when custom composable is not supplied
    val appIconBitmap = remember(context) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            val drawable = context.packageManager.getApplicationIcon(appInfo)
            val size = 512
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .size(76.dp)
            .scale(if (isChecking) pulseScale else 1f)
            .clip(badgeShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (appLogo != null) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                appLogo()
            }
        } else if (appIconBitmap != null) {
            Image(
                bitmap = appIconBitmap,
                contentDescription = "Sonza App Icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_sonza_logo),
                contentDescription = "Sonza Logo",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Official hardware-accelerated animated GIF loading indicator using `loading.gif`.
 * Smoothly scales to specified dimensions with zero distortion.
 */
@Composable
fun UpdaterLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val request = remember(context) {
        ImageRequest.Builder(context)
            .data(R.drawable.loading)
            .build()
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = request,
            imageLoader = imageLoader,
            contentDescription = "Loading...",
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun PreviousUpdatesButton(
    releaseCount: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = CardShape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "View Previous Updates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$releaseCount previous ${if (releaseCount == 1) "version" else "versions"} available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistoricalReleaseCard(release: Release) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v${release.versionName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (release.date.isNotBlank()) {
                    Text(
                        text = release.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (release.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = release.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
