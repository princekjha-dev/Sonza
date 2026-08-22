package com.sonza.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.BuildConfig
import com.sonza.app.R
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.UpdateChannel
import com.sonza.app.ui.components.BetaBadge
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SettingsCard
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.components.glass.GlassModalBottomSheet
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape
import com.sonza.app.ui.utils.horizontalSwipeNavigation
import com.sonza.app.ui.viewmodel.SettingsViewModel
import com.sonza.app.updater.UpdateState
import com.sonza.app.updater.UpdateViewModel
import com.sonza.app.util.SnackbarUtil
import com.sonza.app.util.dpadFocusable
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sonza Settings Screen — Part P Redesign & Data Cleanup.
 *
 * Implements a clean, intentional music settings layout:
 * - Clear section hierarchy (Storage & Data, About & Support, Updates, Audio & Features).
 * - Unified reusable SonzaSettingsRow with 0.97 touch bounce and TalkBack accessibility.
 * - Dynamically sourced app version from BuildConfig.VERSION_NAME (2.6.5.0).
 * - Real live update checking with user-friendly feedback.
 * - Real destinations for Support, Credits, Privacy Policy, and About.
 * - Dynamic bottom insets ensuring the last item is never obscured by navigation or player.
 * - Supports horizontal swipe-right gesture to return to Library.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    updateViewModel: UpdateViewModel = koinViewModel(),
    currentSong: Song? = null,
    onLoginClick: () -> Unit = {},
    onPlaybackClick: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onCustomizationClick: () -> Unit = {},
    onStorageClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onMiscClick: () -> Unit = {},
    onSponsorBlockClick: () -> Unit = {},
    onLastFmClick: () -> Unit = {},
    onDiscordClick: () -> Unit = {},
    onAISettingsClick: () -> Unit = {},
    onUpdaterClick: () -> Unit = {},
    onMigratePlaylistsClick: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by updateViewModel.updateState.collectAsState()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAccountsSheet by remember { mutableStateOf(false) }
    var showUpdateChannelSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Dynamic inset calculation to prevent Mini Player & Bottom Nav from covering content
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomSystemHeight = com.sonza.app.ui.components.ExpressiveBottomNavTokens.getBottomSafePadding(currentSong != null)
    val dynamicBottomInset = navBarPadding + bottomSystemHeight + SpacingTokens.Space2Xl

    var settingsQuery by remember { mutableStateOf("") }
    val settingsSearchIndex = remember {
        listOf(
            SettingsSearchEntry("Appearance", "Theme, dark mode, colors", "theme dark mode light colors dynamic material amoled gradient", Icons.Default.DarkMode, onAppearanceClick),
            SettingsSearchEntry("Playback", "Audio quality, gapless, equalizer", "audio quality bitrate gapless equalizer eq crossfade normalization loudness spatial pitch speed preload", Icons.Default.GraphicEq, onPlaybackClick),
            SettingsSearchEntry("Customization", "Player UI, artwork style", "player ui artwork shape size seekbar style mini player vinyl glass", Icons.Default.Tune, onCustomizationClick),
            SettingsSearchEntry("AI Assistant", "OpenAI, Anthropic, Gemini", "ai assistant openai anthropic gemini equalizer smart", Icons.Default.Psychology, onAISettingsClick),
            SettingsSearchEntry("SponsorBlock", "Skip non-music segments", "sponsorblock skip segments intro outro sponsor", Icons.Default.FastForward, onSponsorBlockClick),
            SettingsSearchEntry("Last.fm", "Scrobbling", "lastfm last.fm scrobble scrobbling", Icons.Default.MusicNote, onLastFmClick),
            SettingsSearchEntry("Advanced", "Diagnostics, experimental & extra options", "advanced misc diagnostics experimental logs developer", Icons.Default.Tune, onMiscClick),
            SettingsSearchEntry("Storage Manager", "Manage downloads & cache", "storage downloads cache clear space data", Icons.Default.Storage, onStorageClick),
            SettingsSearchEntry("Listening stats", "Your listening activity", "stats statistics listening history wrapped activity", Icons.Default.Info, onStatsClick),
            SettingsSearchEntry("Support Sonza", "Help support Sonza's development", "support donate sponsor project", Icons.Default.Favorite, onSupportClick),
            SettingsSearchEntry("About Sonza", "Version ${uiState.currentVersion}", "about version app info changelog", Icons.Default.Info, onAboutClick),
            SettingsSearchEntry("Privacy Policy", "How Sonza handles your data", "privacy policy terms security data", Icons.Default.Security) {
                uriHandler.openUri("https://princekjha-dev.github.io/Sonza-Website/sonza-privacy.html")
            },
            SettingsSearchEntry("Update Channel", uiState.updateChannel.label, "update channel stable beta nightly", Icons.Default.SystemUpdate) {
                showUpdateChannelSheet = true
            },
            SettingsSearchEntry("Check for Updates", "Check for app updates", "update updates ota check changelog", Icons.Default.Download, onUpdaterClick)
        )
    }

    val floatingPlayerEnabled by viewModel.dynamicIslandEnabled.collectAsState(initial = false)
    val sponsorBlockEnabled by viewModel.sponsorBlockEnabled.collectAsState(initial = true)
    val context = androidx.compose.ui.platform.LocalContext.current

    // Handle update check state toasts
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateState.NoUpdate -> {
                SnackbarUtil.showSuccess("You're up to date")
                updateViewModel.resetUpdateState()
            }
            is UpdateState.UpdateAvailable -> {
                SnackbarUtil.showSuccess("Update available: v${state.info.versionName}")
            }
            is UpdateState.Error -> {
                SnackbarUtil.showWarning("Couldn't check for updates")
                updateViewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaColors.Background)
            .statusBarsPadding()
            .horizontalSwipeNavigation(
                onSwipeRight = onNavigateToLibrary,
                onSwipeLeft = null
            ),
        containerColor = SonzaColors.Background,
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = dynamicBottomInset)
            ) {
                // Main Header
                item {
                    Text(
                        text = "Settings",
                        style = SonzaTypography.Headline,
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnBackground,
                        modifier = Modifier
                            .padding(horizontal = SpacingTokens.SpaceLg)
                            .padding(top = SpacingTokens.SpaceLg, bottom = SpacingTokens.SpaceSm)
                    )
                }

                // Search Bar in Settings
                item {
                    OutlinedTextField(
                        value = settingsQuery,
                        onValueChange = { settingsQuery = it },
                        placeholder = {
                            Text(
                                text = "Search settings",
                                style = SonzaTypography.BodyLarge,
                                color = SonzaOnSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = SonzaOnSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(RadiusTokens.Lg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SpacingTokens.SpaceLg)
                            .padding(bottom = SpacingTokens.SpaceMd)
                    )
                }

                if (settingsQuery.isNotBlank()) {
                    val q = settingsQuery.trim().lowercase()
                    val matches = settingsSearchIndex.filter {
                        it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q) || it.keywords.contains(q)
                    }
                    item {
                        if (matches.isEmpty()) {
                            Text(
                                text = "No settings match \"$settingsQuery\"",
                                style = SonzaTypography.BodyMedium,
                                color = SonzaOnSurfaceVariant,
                                modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceLg)
                            )
                        } else {
                            SettingsCard(flat = true, modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)) {
                                matches.forEachIndexed { index, entry ->
                                    if (index > 0) HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))
                                    SonzaSettingsRow(
                                        icon = entry.icon,
                                        title = entry.title,
                                        description = entry.subtitle,
                                        accentColor = accentColor,
                                        onClick = entry.onClick
                                    )
                                }
                            }
                        }
                    }
                } else {

                    // ── 1. Account Section ──
                    item {
                        SonzaSectionHeader("Account")
                        SettingsCard(flat = true, modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)) {
                            if (uiState.isLoggedIn) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(SpacingTokens.SpaceLg),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (uiState.userAvatarUrl != null) {
                                        AsyncImage(
                                            model = uiState.userAvatarUrl,
                                            contentDescription = "Avatar",
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(SquircleShape)
                                                .border(1.dp, SonzaOutline.copy(alpha = 0.5f), SquircleShape)
                                        )
                                    } else {
                                        Surface(
                                            shape = SquircleShape,
                                            color = accentColor.copy(alpha = 0.15f),
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.padding(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = uiState.userName ?: "Signed in",
                                            style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = SonzaOnBackground
                                        )
                                        val email = uiState.storedAccounts.firstOrNull()?.email
                                        Text(
                                            text = email ?: "YouTube Music connected",
                                            style = SonzaTypography.BodyMedium,
                                            color = SonzaOnSurfaceVariant
                                        )
                                    }
                                }

                                HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                                SonzaSettingsRow(
                                    icon = Icons.Default.SwitchAccount,
                                    title = "Switch Account",
                                    description = "Change active YouTube Music session",
                                    accentColor = accentColor,
                                    onClick = {
                                        viewModel.fetchAvailableAccounts()
                                        showAccountsSheet = true
                                    }
                                )

                                HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                                SonzaSettingsRow(
                                    icon = Icons.AutoMirrored.Filled.Logout,
                                    title = "Sign Out",
                                    description = "Disconnect account from this device",
                                    accentColor = MaterialTheme.colorScheme.error,
                                    iconTint = MaterialTheme.colorScheme.error,
                                    onClick = { showSignOutDialog = true }
                                )
                            } else {
                                SonzaSettingsRow(
                                    icon = Icons.AutoMirrored.Filled.Login,
                                    title = "Sign in to YouTube Music",
                                    description = "Sync your playlists, likes, and library",
                                    accentColor = accentColor,
                                    onClick = onLoginClick
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))
                    }

                    // ── 2. Player & Audio ──
                    item {
                        SonzaSectionHeader("Player & Audio")
                        SettingsCard(flat = true, modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)) {
                            SonzaSettingsRow(
                                icon = Icons.Default.GraphicEq,
                                title = "Playback",
                                description = "Audio quality, gapless, equalizer & crossfade",
                                accentColor = accentColor,
                                onClick = onPlaybackClick
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsRow(
                                icon = Icons.Default.Tune,
                                title = "Customization",
                                description = "Player UI, artwork style, seekbar & animations",
                                accentColor = accentColor,
                                onClick = onCustomizationClick
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsRow(
                                icon = Icons.Default.Psychology,
                                title = "AI Assistant",
                                description = "Smart music suggestions with AI models",
                                accentColor = accentColor,
                                onClick = onAISettingsClick
                            )
                        }
                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))
                    }

                    // ── 3. General & Appearance ──
                    item {
                        SonzaSectionHeader("General & Appearance")
                        SettingsCard(flat = true, modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)) {
                            SonzaSettingsRow(
                                icon = Icons.Default.DarkMode,
                                title = "Appearance",
                                description = "Theme, dark mode, colors & background style",
                                accentColor = accentColor,
                                onClick = onAppearanceClick
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsSwitchRow(
                                icon = Icons.Default.VisibilityOff,
                                title = "Incognito Mode",
                                description = "Pause listening history and activity logging",
                                checked = uiState.incognitoModeEnabled,
                                accentColor = accentColor,
                                onCheckedChange = { viewModel.setIncognitoModeEnabled(it) }
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsSwitchRow(
                                icon = Icons.Default.PictureInPicture,
                                title = "Dynamic Island Floating Player",
                                description = "Show floating Dynamic Island outside app when music is playing",
                                checked = floatingPlayerEnabled,
                                accentColor = accentColor,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        viewModel.setDynamicIslandEnabled(enabled)
                                        if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                            if (!android.provider.Settings.canDrawOverlays(context)) {
                                                try {
                                                    val intent = Intent(
                                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                        Uri.parse("package:${context.packageName}")
                                                    )
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    // Fallback to generic settings if package uri fails
                                                    try {
                                                        context.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                                                    } catch (e2: Exception) {
                                                        // Ignore
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsSwitchRow(
                                icon = Icons.Default.HeadsetMic,
                                title = "Bluetooth Autoplay",
                                description = "Resume playback when bluetooth connects",
                                checked = uiState.bluetoothAutoplayEnabled,
                                accentColor = accentColor,
                                onCheckedChange = { scope.launch { viewModel.setBluetoothAutoplayEnabled(it) } }
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsRow(
                                icon = Icons.Default.Tune,
                                title = "Advanced Options",
                                description = "Lyrics providers, experimental options & logs",
                                accentColor = accentColor,
                                onClick = onMiscClick
                            )
                        }
                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))
                    }

                    // ── 4. Storage & Data ──
                    item {
                        SonzaSectionHeader("Storage & Data")
                        SettingsCard(flat = true, modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)) {
                            SonzaSettingsRow(
                                icon = Icons.Default.Storage,
                                title = "Storage Manager",
                                description = "Manage downloads, cache & offline storage",
                                accentColor = accentColor,
                                onClick = onStorageClick
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsRow(
                                icon = Icons.Default.Download,
                                title = "Migrate Playlists",
                                description = "Bring your playlists from other music services",
                                accentColor = accentColor,
                                onClick = onMigratePlaylistsClick
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsRow(
                                icon = Icons.Default.Info,
                                title = "Listening Stats",
                                description = "Your listening activity, top songs & recap",
                                accentColor = accentColor,
                                onClick = onStatsClick
                            )
                        }
                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))
                    }

                    // ── 5. About & Support ──
                    item {
                        SonzaSectionHeader("About & Support")
                        SettingsCard(flat = true, modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)) {
                            SonzaSettingsRow(
                                icon = Icons.Default.Favorite,
                                title = "Support Sonza",
                                description = "Help support Sonza's development",
                                accentColor = accentColor,
                                onClick = onSupportClick
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            SonzaSettingsRow(
                                icon = Icons.Default.Security,
                                title = "Privacy Policy",
                                description = "How Sonza handles your data",
                                accentColor = accentColor,
                                onClick = {
                                    uriHandler.openUri("https://princekjha-dev.github.io/Sonza-Website/sonza-privacy.html")
                                }
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            val displayVersion = if (uiState.currentVersion.isNotBlank()) "Version ${uiState.currentVersion}" else "Version ${BuildConfig.VERSION_NAME}"
                            SonzaSettingsRow(
                                icon = Icons.Default.Info,
                                title = "About Sonza",
                                description = displayVersion,
                                accentColor = accentColor,
                                onClick = onAboutClick
                            )
                        }
                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))
                    }

                    // ── 6. Updates ──
                    item {
                        SonzaSectionHeader("Updates")
                        SettingsCard(flat = true, modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)) {
                            SonzaSettingsRow(
                                icon = Icons.Default.SystemUpdate,
                                title = "Update Channel",
                                description = uiState.updateChannel.label,
                                accentColor = accentColor,
                                onClick = { showUpdateChannelSheet = true }
                            )

                            HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg))

                            val isChecking = updateState is UpdateState.Checking
                            SonzaSettingsRow(
                                icon = Icons.Default.Download,
                                title = "Check for Updates",
                                description = if (isChecking) "Checking for updates..." else "Check for the latest Sonza version",
                                trailingBadge = if (isChecking) {
                                    {
                                        com.sonza.app.ui.components.SonzaLoadingIndicator(
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null,
                                accentColor = accentColor,
                                onClick = {
                                    if (!isChecking) {
                                        updateViewModel.checkForUpdate(
                                            currentVersionCode = BuildConfig.VERSION_CODE,
                                            isNightly = uiState.updateChannel == UpdateChannel.NIGHTLY
                                        )
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceXl))
                    }
                }
            }
        }
    }

    // ── Dialogs & Bottom Sheets ──

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Sign out?",
                    style = SonzaTypography.TitleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground
                )
            },
            text = {
                Text(
                    text = "You will be disconnected from YouTube Music on this device.",
                    style = SonzaTypography.BodyMedium,
                    color = SonzaOnSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        android.webkit.CookieManager.getInstance().removeAllCookies(null)
                        android.webkit.CookieManager.getInstance().flush()
                        viewModel.prepareAddAccount()
                        showSignOutDialog = false
                    }
                ) {
                    Text(text = "Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = SonzaOnBackground)
                }
            },
            containerColor = SonzaSurface,
            shape = RoundedCornerShape(RadiusTokens.Lg)
        )
    }

    // Accounts Bottom Sheet
    if (showAccountsSheet) {
        GlassModalBottomSheet(
            onDismissRequest = { showAccountsSheet = false },
            sheetState = sheetState,
            fallbackContainerColor = SonzaSurface,
            contentColor = SonzaOnBackground,
            shape = RoundedCornerShape(topStart = RadiusTokens.Lg, topEnd = RadiusTokens.Lg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpacingTokens.Space2Xl)
            ) {
                Text(
                    text = "Switch Account",
                    style = SonzaTypography.TitleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = SpacingTokens.SpaceXl, vertical = SpacingTokens.SpaceLg)
                )

                if (uiState.availableAccounts.isNotEmpty()) {
                    Text(
                        text = "Channels",
                        style = SonzaTypography.TitleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceXl, vertical = SpacingTokens.SpaceSm)
                    )

                    uiState.availableAccounts.forEach { account ->
                        val isCurrent = account.authUserIndex == (uiState.storedAccounts.firstOrNull { it.email == "current" }?.authUserIndex ?: 0) &&
                                account.name == (uiState.storedAccounts.firstOrNull { it.email == "current" }?.name ?: "")

                        ListItem(
                            headlineContent = {
                                Text(
                                    account.name,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = SonzaOnBackground
                                )
                            },
                            supportingContent = { Text(account.email, color = SonzaOnSurfaceVariant) },
                            leadingContent = {
                                AsyncImage(
                                    model = account.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(SquircleShape)
                                        .border(
                                            if (isCurrent) 1.5.dp else 0.dp,
                                            accentColor,
                                            SquircleShape
                                        )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .dpadFocusable(
                                    onClick = {
                                        viewModel.switchAccount(account)
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            showAccountsSheet = false
                                        }
                                    },
                                    shape = SquircleShape
                                )
                                .padding(horizontal = SpacingTokens.SpaceSm),
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f))
                }

                if (uiState.storedAccounts.isNotEmpty()) {
                    Text(
                        text = "Saved Sessions",
                        style = SonzaTypography.TitleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceXl, vertical = SpacingTokens.SpaceSm)
                    )
                    uiState.storedAccounts.forEach { account ->
                        val isCurrent = account.email == (uiState.storedAccounts.firstOrNull { it.email == "current" }?.email ?: "")

                        ListItem(
                            headlineContent = {
                                Text(
                                    account.name,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = SonzaOnBackground
                                )
                            },
                            supportingContent = { Text(account.email, color = SonzaOnSurfaceVariant) },
                            leadingContent = {
                                AsyncImage(
                                    model = account.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(SquircleShape)
                                        .border(
                                            if (isCurrent) 1.5.dp else 0.dp,
                                            accentColor,
                                            SquircleShape
                                        )
                                )
                            },
                            trailingContent = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove account session",
                                    tint = SonzaOnSurfaceVariant,
                                    modifier = Modifier
                                        .clickable { viewModel.removeAccount(account.email) }
                                        .padding(8.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .dpadFocusable(
                                    onClick = {
                                        viewModel.switchAccount(account)
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            showAccountsSheet = false
                                        }
                                    },
                                    shape = SquircleShape
                                )
                                .padding(horizontal = SpacingTokens.SpaceSm),
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))
                }

                ListItem(
                    headlineContent = { Text("Add another account", fontWeight = FontWeight.Medium, color = SonzaOnBackground) },
                    leadingContent = {
                        Surface(
                            shape = SquircleShape,
                            color = SonzaSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.padding(10.dp),
                                tint = SonzaOnSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(
                            onClick = {
                                viewModel.clearWebViewCookies()
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showAccountsSheet = false
                                    onLoginClick()
                                }
                            },
                            shape = SquircleShape
                        )
                        .padding(horizontal = SpacingTokens.SpaceSm),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }

    // Update Channel Bottom Sheet
    if (showUpdateChannelSheet) {
        GlassModalBottomSheet(
            onDismissRequest = { showUpdateChannelSheet = false },
            fallbackContainerColor = SonzaSurface,
            contentColor = SonzaOnBackground,
            shape = RoundedCornerShape(topStart = RadiusTokens.Lg, topEnd = RadiusTokens.Lg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpacingTokens.Space2Xl)
            ) {
                Text(
                    text = "Update Channel",
                    style = SonzaTypography.TitleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = SpacingTokens.SpaceXl, vertical = SpacingTokens.SpaceLg)
                )

                UpdateChannel.entries.forEach { channel ->
                    val isSelected = uiState.updateChannel == channel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setUpdateChannel(channel)
                                showUpdateChannelSheet = false
                            }
                            .padding(horizontal = SpacingTokens.SpaceXl, vertical = SpacingTokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                viewModel.setUpdateChannel(channel)
                                showUpdateChannelSheet = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = accentColor,
                                unselectedColor = SonzaOnSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
                        Text(
                            text = channel.label,
                            style = SonzaTypography.BodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = SonzaOnBackground
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// Reusable Sonza Settings Components
// ──────────────────────────────────────────

/**
 * Reusable Settings Section Title per Part P requirements.
 */
@Composable
private fun SonzaSectionHeader(title: String) {
    Text(
        text = title,
        style = SonzaTypography.TitleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        ),
        color = SonzaOnSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = SpacingTokens.SpaceLg + SpacingTokens.SpaceXs,
                end = SpacingTokens.SpaceLg,
                top = SpacingTokens.SpaceSm,
                bottom = SpacingTokens.SpaceXs
            )
    )
}

/**
 * Reusable Sonza Settings Row.
 *
 * Structure:
 * [Tinted Icon Box]   Title
 *                     Description                 [ > / Badge ]
 */
@Composable
private fun SonzaSettingsRow(
    icon: ImageVector,
    title: String,
    description: String? = null,
    accentColor: Color,
    iconTint: Color = accentColor,
    trailingBadge: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "$title, ${description ?: ""}, button"
            },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in squircle tinted box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            // Title & Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SonzaTypography.TitleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = SonzaTypography.BodyMedium.copy(fontSize = 13.sp),
                        color = SonzaOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Trailing action or badge
            if (trailingBadge != null) {
                Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
                trailingBadge()
            } else {
                Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = SonzaOnSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Reusable Settings Switch Row.
 */
@Composable
private fun SonzaSettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String? = null,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .semantics {
                role = Role.Switch
                contentDescription = "$title, ${description ?: ""}, ${if (checked) "enabled" else "disabled"}"
            },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SonzaTypography.TitleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = SonzaTypography.BodyMedium.copy(fontSize = 13.sp),
                        color = SonzaOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = SonzaOnSurfaceVariant,
                    uncheckedTrackColor = SonzaSurfaceVariant
                )
            )
        }
    }
}

private data class SettingsSearchEntry(
    val title: String,
    val subtitle: String,
    val keywords: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

