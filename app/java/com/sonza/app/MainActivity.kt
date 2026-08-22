package com.sonza.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.CloudOff
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Lock
import com.sonza.app.data.SessionManager
import com.sonza.app.core.model.AppTheme
import com.sonza.app.core.model.OutputDevice
import com.sonza.app.core.model.ThemeMode
import com.sonza.app.core.model.MiniPlayerStyle
import com.sonza.app.navigation.Destination
import com.sonza.app.navigation.NavGraph
import com.sonza.app.ui.components.ExpressiveBottomNav
import com.sonza.app.ui.components.player.ExpandablePlayerSheet
import com.sonza.app.ui.components.DominantColors
import com.sonza.app.ui.components.rememberDominantColors
import com.sonza.app.core.model.ArtistCreditInfo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.sonza.app.ui.screens.player.components.VolumeIndicator
import com.sonza.app.ui.screens.player.components.SystemVolumeObserver
import com.sonza.app.ui.theme.SonzaTheme
import com.sonza.app.ui.viewmodel.PlayerViewModel
import com.sonza.app.ui.viewmodel.MainViewModel
import com.sonza.app.updater.UpdateViewModel
import com.sonza.app.updater.UpdateDialog
import com.sonza.app.updater.UpdateState
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.sonza.app.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.sonza.app.pip.PipHelper
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.compose.runtime.produceState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import com.sonza.app.ui.utils.LocalDeviceFormFactor
import com.sonza.app.ui.utils.rememberDeviceFormFactor
import com.sonza.app.ui.utils.DeviceFormFactor

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModel()
    private val updateViewModel: UpdateViewModel by viewModel()
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var youTubeRepository: com.sonza.app.data.repository.YouTubeRepository
    
    @Inject
    lateinit var downloadRepository: com.sonza.app.data.repository.DownloadRepository
    
    @Inject
    lateinit var musicPlayer: com.sonza.app.player.MusicPlayer

    @Inject
    lateinit var pipHelper: PipHelper

    private lateinit var audioManager: AudioManager
    
    // Track whether song is playing for volume key interception
    private var isSongPlaying: Boolean = false
    
    // Track whether in-app volume slider is enabled (if false, show system UI)
    private var isVolumeSliderEnabled: Boolean = true

    // Track whether Picture-in-Picture is enabled in settings
    private var isPipEnabled: Boolean = false
    
    // Flow to emit volume key events to the UI
    private val _volumeKeyEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (perm, granted) ->
            if (!granted) {
                android.util.Log.w("MainActivity", "Permission denied: $perm")
            }
        }
        // Notifications power lock-screen / Android Auto / wear controls and
        // the foreground media service notification. A silent denial means
        // the user thinks the app is broken; surface a Toast so they know
        // the playback notification won't appear and they can re-enable it
        // in system settings.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            permissions[Manifest.permission.POST_NOTIFICATIONS] == false
        ) {
            android.widget.Toast.makeText(
                this,
                "Notifications disabled — playback controls won't appear on the lock screen. Enable it in App settings.",
                android.widget.Toast.LENGTH_LONG,
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        com.sonza.app.util.AppLog.i("MainActivity") { "onCreate started" }

        // Apply the variant-matching SplashScreen theme BEFORE installSplashScreen
        // reads it. Android's activity-alias android:theme attribute should make
        // the system propagate the correct theme on Android 12+, but in practice
        // the AndroidX SplashScreen library (which handles the post-system-splash
        // drawable + the keep-on-screen extension) reads the theme from
        // MainActivity's current state — so without this override the user sees
        // the Classic splash drawable even after switching variants.
        //
        // We can't use DataStore here because it's async and would race the splash
        // initialisation; SessionManager mirrors the chosen variant to a
        // SharedPreferences slot specifically for this synchronous read.
        applyVariantSplashTheme()

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep splash screen on until ViewModel reports isReady = true
        splashScreen.setKeepOnScreenCondition {
            !mainViewModel.uiState.value.isReady
        }
        
        enableEdgeToEdge()
        com.sonza.app.util.SnackbarUtil.setRootView(findViewById(android.R.id.content))
        enableMaxRefreshRate()
        
        lifecycleScope.launch {
            // Check for manual updates on launch
            val channel = sessionManager.getUpdateChannel()
            updateViewModel.checkForUpdate(
                com.sonza.app.BuildConfig.VERSION_CODE,
                isNightly = channel == com.sonza.app.core.model.UpdateChannel.NIGHTLY
            )

            // Also check if PeriodicUpdateWorker found anything while app was closed
            val pendingCode = sessionManager.getPendingUpdateVersionCode()
            if (pendingCode != null && pendingCode > com.sonza.app.BuildConfig.VERSION_CODE) {
                val pendingName = sessionManager.getPendingUpdateVersionName() ?: "New Version"
                // Trigger the UpdateAvailable state in ViewModel so the dialog shows
                updateViewModel.triggerUpdateAvailable(pendingCode, pendingName, com.sonza.app.BuildConfig.VERSION_CODE)
            }
        }
        
        // Initialize audio manager for volume control
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        requestPermissions()
        
        setContent {
            val themeMode by sessionManager.themeModeFlow.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val dynamicColor by sessionManager.dynamicColorFlow.collectAsStateWithLifecycle(initialValue = true)
            val appTheme by sessionManager.appThemeFlow.collectAsStateWithLifecycle(initialValue = AppTheme.DEFAULT)
            val pureBlackEnabled by sessionManager.pureBlackEnabledFlow.collectAsStateWithLifecycle(initialValue = false)
            val albumArtDynamicColorsEnabled by sessionManager.albumArtDynamicColorsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
            // Only the current song's thumbnail URL is needed here (to drive the album-art
            // theme colors). Collecting the whole PlayerState would recompose this top-level
            // theme scope — and therefore the entire app — on every ~400ms playback tick.
            val currentThumbnailUrl by remember {
                musicPlayer.playerState
                    .map { it.currentSong?.thumbnailUrl }
                    .distinctUntilChanged()
            }.collectAsStateWithLifecycle(initialValue = musicPlayer.playerState.value.currentSong?.thumbnailUrl)
            val forceMaxRefreshRate by sessionManager.forceMaxRefreshRateFlow.collectAsStateWithLifecycle(initialValue = true)
            val systemDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(forceMaxRefreshRate) {
                if (forceMaxRefreshRate) {
                    enableMaxRefreshRate()
                } else {
                    // Reset to default
                    window.attributes = window.attributes.apply {
                        preferredDisplayModeId = 0 // 0 means default/no preference
                    }
                }
            }

            // Observe PiP enabled state globally
            LaunchedEffect(Unit) {
                sessionManager.dynamicIslandEnabledFlow.collect { enabled ->
                    isPipEnabled = enabled
                    pipHelper.updatePipParams(this@MainActivity, isPipEnabled)
                }
            }

            val darkTheme = remember(themeMode, systemDarkTheme) {
                when (themeMode) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> systemDarkTheme
                }
            }
            
            val albumArtColors = rememberDominantColors(
                imageUrl = currentThumbnailUrl,
                isDarkTheme = darkTheme
            )

            SonzaTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor,
                appTheme = appTheme,
                pureBlack = pureBlackEnabled,
                albumArtColors = if (albumArtDynamicColorsEnabled && currentThumbnailUrl != null) albumArtColors else null
            ) {
                val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                val foldingFeature by produceState<FoldingFeature?>(null) {
                    WindowInfoTracker.getOrCreate(this@MainActivity)
                        .windowLayoutInfo(this@MainActivity)
                        .collect { info ->
                            value = info.displayFeatures
                                .filterIsInstance<FoldingFeature>()
                                .firstOrNull()
                        }
                }
                
                val formFactor = rememberDeviceFormFactor(
                    windowSizeClass = windowSizeClass,
                    foldingFeature = foldingFeature
                )

                CompositionLocalProvider(
                    LocalDeviceFormFactor provides formFactor
                ) {
                    SonzaApp(
                        intent = intent,
                        networkMonitor = networkMonitor,
                        audioManager = audioManager,
                        volumeKeyEvents = _volumeKeyEvents,
                        downloadRepository = downloadRepository,
                        sessionManager = sessionManager, // Pass the injected instance
                        youTubeRepository = youTubeRepository,
                        updateViewModel = updateViewModel,
                        onPlaybackStateChanged = { hasSong -> 
                            isSongPlaying = hasSong
                            // Update PiP params whenever playback state changes
                            // so the play/pause icon stays in sync
                            pipHelper.updatePipParams(this@MainActivity, isPipEnabled)
                        },
                        onVolumeSliderEnabledChanged = { enabled ->
                            isVolumeSliderEnabled = enabled
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Intercept hardware volume keys to control music volume
     * without showing the system volume UI panel - only when song is playing
     * and in-app volume slider is enabled.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Only intercept volume keys when a song is playing AND in-app volume slider is enabled
        // When volume slider is disabled, let system handle it (shows system volume UI)
        if (!isSongPlaying || !isVolumeSliderEnabled) {
            return super.dispatchKeyEvent(event)
        }
        
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        0 // No flags = no system UI
                    )
                    _volumeKeyEvents.tryEmit(Unit)
                }
                true // Consume the event
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        0 // No flags = no system UI
                    )
                    _volumeKeyEvents.tryEmit(Unit)
                }
                true // Consume the event
            }
            else -> super.dispatchKeyEvent(event)
        }
    }
    
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
    
    private fun requestPermissions() {
        val missingPermissions = com.sonza.app.util.PermissionUtils.getMissingPermissions(this)
        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    /**
     * Synchronously read the user's stored logo variant from the branding
     * SharedPreferences mirror (kept in sync by SessionManager.setLogoVariant)
     * and apply the matching SplashScreen theme so installSplashScreen() picks
     * up the correct windowSplashScreenAnimatedIcon. Must run before
     * installSplashScreen() and before super.onCreate().
     */
    private fun applyVariantSplashTheme() {
        val prefs = getSharedPreferences("sonza_branding", Context.MODE_PRIVATE)
        val variantName = prefs.getString("logo_variant", null) ?: "PULSE"
        // The splash drawable lives at the CONCEPT level — sub-styles
        // (App Icon / Mono / Light / Tone) reuse the same hero PNG for the
        // splash to keep the asset count manageable. So we derive the splash
        // theme from the variant name's prefix rather than its exact value.
        val themeRes = when {
            variantName.startsWith("PULSE") -> R.style.Theme_Sonza_SplashScreen_Pulse
            variantName.startsWith("RESONANCE") -> R.style.Theme_Sonza_SplashScreen_Resonance
            variantName.startsWith("AETHER") -> R.style.Theme_Sonza_SplashScreen_Aether
            variantName == "CLASSIC" -> R.style.Theme_Sonza_SplashScreen
            // Fresh install / unknown value — match the in-app PULSE default.
            else -> R.style.Theme_Sonza_SplashScreen_Pulse
        }
        setTheme(themeRes)
    }

    private fun enableMaxRefreshRate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val modes = display?.supportedModes
            val maxRefreshRate = modes?.maxByOrNull { it.refreshRate }?.refreshRate ?: return
            val preferredMode = modes.find { it.refreshRate == maxRefreshRate } ?: return
            
            window.attributes = window.attributes.apply {
                preferredDisplayModeId = preferredMode.modeId
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        com.sonza.app.service.DynamicIslandOverlayService.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        com.sonza.app.service.DynamicIslandOverlayService.setAppForeground(false)
        com.sonza.app.service.DynamicIslandOverlayService.start(this)
        
        // Disable video track for bandwidth optimization when backgrounded
        // but NOT when entering PiP mode (video needs to remain active for PiP)
        if (!isInPictureInPictureMode) {
            musicPlayer.optimizeBandwidth(true)
        }
    }

    override fun onStop() {
        super.onStop()
        com.sonza.app.service.DynamicIslandOverlayService.setAppForeground(false)
        com.sonza.app.service.DynamicIslandOverlayService.start(this)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isSongPlaying && isPipEnabled) {
            // Enter PiP synchronously when user leaves the activity (Home or App Switcher)
            val isVideoMode = musicPlayer.playerState.value.isVideoMode
            pipHelper.enterPipIfEligible(this, forceVideoPip = isVideoMode, isPipEnabled = isPipEnabled)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        mainViewModel.setPictureInPictureMode(isInPictureInPictureMode)
    }
    
    override fun onResume() {
        super.onResume()
        com.sonza.app.service.DynamicIslandOverlayService.setAppForeground(true)
        
        // Re-enable video track when returning to foreground
        musicPlayer.optimizeBandwidth(false)
        
        // Update PiP params (e.g., sync play/pause icon after returning from PiP)
        pipHelper.updatePipParams(this, isPipEnabled)
    }
}

@Composable
fun SonzaApp(
    intent: Intent? = null,
    networkMonitor: NetworkMonitor,
    audioManager: AudioManager,
    volumeKeyEvents: SharedFlow<Unit>? = null,
    downloadRepository: com.sonza.app.data.repository.DownloadRepository? = null,
    sessionManager: SessionManager, // Injected instance passed from MainActivity
    youTubeRepository: com.sonza.app.data.repository.YouTubeRepository,
    updateViewModel: com.sonza.app.updater.UpdateViewModel,
    onPlaybackStateChanged: (Boolean) -> Unit,
    onVolumeSliderEnabledChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collect volume slider enabled preference
    val volumeSliderEnabled by sessionManager.volumeSliderEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val isAlbumArtDynamicColorsEnabled by sessionManager.albumArtDynamicColorsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val miniPlayerAlpha by sessionManager.miniPlayerAlphaFlow.collectAsStateWithLifecycle(initialValue = 0f)
    val miniPlayerStyle by sessionManager.miniPlayerStyleFlow.collectAsStateWithLifecycle(initialValue = MiniPlayerStyle.YT_MUSIC)
    val miniPlayerGlassBlur by sessionManager.miniPlayerGlassBlurFlow.collectAsStateWithLifecycle(initialValue = 50f)
    val swipeDownToDismissEnabled by sessionManager.swipeDownToDismissEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val dynamicIslandEnabled by sessionManager.dynamicIslandEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = koinViewModel()
    val mainViewModel: MainViewModel = koinViewModel()
    val playlistManagementViewModel: com.sonza.app.ui.viewmodel.PlaylistManagementViewModel = koinViewModel()
    
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val playlistManagementUiState by playlistManagementViewModel.uiState.collectAsStateWithLifecycle()

    val isLoggedIn by sessionManager.isLoggedInFlow.collectAsStateWithLifecycle(initialValue = false)

    val scope = androidx.compose.runtime.rememberCoroutineScope()    
    
    // Handle events from other viewmodels for global sheets
    val homeViewModel: com.sonza.app.ui.viewmodel.HomeViewModel = koinViewModel()
    val searchViewModel: com.sonza.app.ui.viewmodel.SearchViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        homeViewModel.events.collect { event ->
            if (event is com.sonza.app.ui.viewmodel.HomeEvent.ShowAddToPlaylistSheet) {
                playlistManagementViewModel.showAddToPlaylistSheet(event.song)
            }
        }
    }

    LaunchedEffect(Unit) {
        searchViewModel.events.collect { event ->
            if (event is com.sonza.app.ui.viewmodel.SearchEvent.ShowAddToPlaylistSheet) {
                playlistManagementViewModel.showAddToPlaylistSheet(event.song)
            }
        }
    }

    // Handle messages from PlaylistManagement
    LaunchedEffect(playlistManagementUiState.successMessage, playlistManagementUiState.errorMessage) {
        playlistManagementUiState.successMessage?.let {
            com.sonza.app.util.SnackbarUtil.showMessage(it)
            playlistManagementViewModel.clearMessages()
        }
        playlistManagementUiState.errorMessage?.let {
            com.sonza.app.util.SnackbarUtil.showError(it)
            playlistManagementViewModel.clearMessages()
        }
    }
    // Optimized states to reduce recompositions.
    // `playbackInfo` is a position/buffer-filtered view of the player state, so it only
    // emits when a meaningful field changes (song, play/pause, shuffle, etc.) — safe to
    // read at this top scope. The RAW `playerState` (which ticks ~every 400ms with the
    // current position) is intentionally NOT collected here anymore: collecting it at this
    // scope forced the whole app (nav host + mini player + current screen) to recompose on
    // every position tick during playback. It is now collected only inside the expanded
    // player content below, which is composed solely while the player is open.
    val playbackInfo by playerViewModel.playbackInfo.collectAsStateWithLifecycle(initialValue = com.sonza.app.core.model.PlayerState())
    // Stable progress provider for the mini player. Derived from a distinct progress
    // flow and read only inside the progress bar's draw scope, so neither this scope
    // nor the mini player recomposes on the ~400ms position tick.
    val miniPlayerProgressState = remember {
        playerViewModel.playerState.map { it.progress }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = 0f)
    val miniPlayerProgressProvider = remember { { miniPlayerProgressState.value } }
    val isPlayerExpanded by playerViewModel.isPlayerExpanded.collectAsStateWithLifecycle(initialValue = false)
    val keepScreenOnEnabled by sessionManager.keepScreenOnEnabledFlow.collectAsStateWithLifecycle(initialValue = false)
    val artworkShape by playerViewModel.artworkShape.collectAsStateWithLifecycle()

    val rootView = LocalView.current
    val shouldKeepScreenOn = keepScreenOnEnabled && isPlayerExpanded
    DisposableEffect(shouldKeepScreenOn) {
        rootView.keepScreenOn = shouldKeepScreenOn
        onDispose {
            rootView.keepScreenOn = false
        }
    }
    
    val lyrics by playerViewModel.lyricsState.collectAsStateWithLifecycle(initialValue = null)
    val isFetchingLyrics by playerViewModel.isFetchingLyrics.collectAsStateWithLifecycle(initialValue = false)
    val relatedSongs by playerViewModel.relatedSongsState.collectAsStateWithLifecycle(initialValue = emptyList())
    val isFetchingRelated by playerViewModel.isFetchingRelated.collectAsStateWithLifecycle(initialValue = false)
    val selectedRelatedIndices by playerViewModel.selectedRelatedIndices.collectAsStateWithLifecycle(initialValue = emptySet())
    val selectedLyricsProvider by playerViewModel.selectedLyricsProvider.collectAsStateWithLifecycle(initialValue = com.sonza.app.providers.lyrics.LyricsProviderType.AUTO)

    val artistCredits by playerViewModel.artistCredits.collectAsStateWithLifecycle(initialValue = emptyList<ArtistCreditInfo>())
    val showMultipleArtistsDialog by playerViewModel.showMultipleArtistsDialog.collectAsStateWithLifecycle(initialValue = false)
    
    // Track if song is playing for Activity-level volume interception and PiP state updates
    // Use playbackInfo (stable) to avoid recomposing the whole app shell on progress updates
    val hasSong = playbackInfo.currentSong != null
    LaunchedEffect(hasSong, playbackInfo.isPlaying, playbackInfo.isVideoMode) {
        onPlaybackStateChanged(hasSong)
    }
    
    // Sync volume slider enabled state to Activity
    LaunchedEffect(volumeSliderEnabled) {
        onVolumeSliderEnabledChanged(volumeSliderEnabled)
    }

    // Handle intent changes
    LaunchedEffect(intent) {
        if (intent != null) {
            if (intent.action == Intent.ACTION_VIEW && intent.type?.startsWith("audio/") == true) {
                mainViewModel.handleAudioIntent(intent.data)
            } else {
                mainViewModel.handleDeepLink(intent.data)
            }
        }
    }

    // Handle MainEvents (Navigation, Toasts)
    // Collect under repeatOnLifecycle so deep-link playback/navigation events
    // are only handled while the Activity is at least STARTED. Without this the
    // LaunchedEffect(Unit) collector keeps firing during pause/destroy and can
    // act against a dead Activity or nav controller.
    val eventsLifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(eventsLifecycleOwner) {
        eventsLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            mainViewModel.events.collect { event ->
            when (event) {
                is com.sonza.app.ui.viewmodel.MainEvent.PlayFromDeepLink -> {
                    playerViewModel.playFromDeepLink(event.videoId)
                }
                is com.sonza.app.ui.viewmodel.MainEvent.PlayFromLocalUri -> {
                    playerViewModel.playFromLocalUri(context, event.uri)
                }
                is com.sonza.app.ui.viewmodel.MainEvent.NavigateToAlbum -> {
                    navController.navigate(
                        com.sonza.app.navigation.Destination.Album(
                            albumId = event.browseId, name = null, thumbnailUrl = null
                        )
                    )
                }
                is com.sonza.app.ui.viewmodel.MainEvent.NavigateToPlaylist -> {
                    navController.navigate(
                        com.sonza.app.navigation.Destination.Playlist(
                            playlistId = event.playlistId, name = null, thumbnailUrl = null
                        )
                    )
                }
                is com.sonza.app.ui.viewmodel.MainEvent.NavigateToArtist -> {
                    navController.navigate(com.sonza.app.navigation.Destination.Artist(event.channelId))
                }
                is com.sonza.app.ui.viewmodel.MainEvent.NavigateToSearch -> {
                    navController.navigate(com.sonza.app.navigation.Destination.Search)
                    // TODO: pre-fill search with event.query once Search screen exposes a seed param.
                }
                is com.sonza.app.ui.viewmodel.MainEvent.ShowToast -> {
                    com.sonza.app.util.SnackbarUtil.showMessage(event.message)
                }
            }
            }
        }
    }

    // Volume control states for global indicator
    var maxVolume by remember {
        mutableStateOf(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
    }
    var currentVolume by remember {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var lastVolumeChangeTime by remember { mutableStateOf(0L) }
    
    // Listen for Volume Key Events (Manual Trigger)
    LaunchedEffect(volumeKeyEvents) {
        volumeKeyEvents?.collect {
            // Update current volume (it might have changed, or not if at boundaries)
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            // Show indicator
            lastVolumeChangeTime = System.currentTimeMillis()
        }
    }
    
    // Listen for System Volume Changes
    SystemVolumeObserver(context = context) { newVol, newMax ->
        maxVolume = newMax
        if (currentVolume != newVol) {
            currentVolume = newVol
            lastVolumeChangeTime = System.currentTimeMillis()
        }
    }
    
    // Auto-hide volume indicator
    LaunchedEffect(lastVolumeChangeTime) {
        if (lastVolumeChangeTime > 0 && hasSong) {
            showVolumeIndicator = true
            kotlinx.coroutines.delay(2000) // 2 seconds delay
            showVolumeIndicator = false
        }
    }
    
    // Monitor network connectivity
    val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle(initialValue = networkMonitor.isCurrentlyConnected())
    
    // Show snackbar when offline for 30 seconds
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            snackbarHostState.showSnackbar(
                message = "No internet connection",
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            // Auto-dismiss after 30 seconds
            delay(30000)
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }
    
    // Sleep Timer
    val sleepTimerOption by playerViewModel.sleepTimerOption.collectAsStateWithLifecycle(initialValue = com.sonza.app.player.SleepTimerOption.OFF)
    val sleepTimerRemainingMs by playerViewModel.sleepTimerRemainingMs.collectAsStateWithLifecycle(initialValue = null)
    
    // Radio Mode
    val isRadioMode by playerViewModel.isRadioMode.collectAsStateWithLifecycle(initialValue = false)
    val isLoadingMoreSongs by playerViewModel.isLoadingMoreSongs.collectAsStateWithLifecycle(initialValue = false)
    val isMiniPlayerDismissed by playerViewModel.isMiniPlayerDismissed.collectAsStateWithLifecycle(initialValue = false)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destination = navBackStackEntry?.destination
    
    var currentDestination by remember { mutableStateOf<Destination>(Destination.Home) }
    
    // Restore Playback only if no deep link handled
    var restoreAttempted by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!restoreAttempted && intent?.data == null) {
            restoreAttempted = true
            if (playbackInfo.currentSong == null) {
                playerViewModel.restoreLastPlayback()
            }
        }
    }
    
    // Update current destination based on route
    currentDestination = when {
        destination?.hasRoute<Destination.Home>() == true -> Destination.Home
        destination?.hasRoute<Destination.Search>() == true -> Destination.Search
        destination?.hasRoute<Destination.Library>() == true -> Destination.Library
        destination?.hasRoute<Destination.Settings>() == true -> Destination.Settings
        else -> currentDestination
    }
    
    val showBottomNav = destination?.let {
        it.hasRoute<Destination.Home>() ||
        it.hasRoute<Destination.Search>() ||
        it.hasRoute<Destination.Library>() ||
        it.hasRoute<Destination.Settings>()
    } ?: false
    
    // Auto-show MiniPlayer when returning to Home
    LaunchedEffect(destination) {
        if (destination?.hasRoute<Destination.Home>() == true) {
            playerViewModel.showMiniPlayer()
        }
    }
    
    
    // Don't show MiniPlayer on Player screen itself or if explicitly dismissed
    // With bottom sheet, "Player screen" is just the expanded state.
    // We hide the sheet if the current route is one where we don't want player (e.g. login?)
    // or if dismissed.
    val showMiniPlayer = !isMiniPlayerDismissed && destination?.hasRoute<Destination.YouTubeLogin>() != true && hasSong
    
    // Don't show global volume indicator on PlayerScreen (it has its own)
    val showGlobalVolumeIndicator = hasSong && !isPlayerExpanded
    
    // Use MaterialTheme colors as current dominant colors for components.
    // Remembered against the actual color inputs so we don't allocate a fresh
    // DominantColors (and re-trigger downstream recomposition) on every tick.
    val bg = androidx.compose.material3.MaterialTheme.colorScheme.background
    val surfaceVariant = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val onBg = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
    val currentDominantColors = remember(bg, surfaceVariant, primaryColor, onBg) {
        DominantColors(
            primary = bg,
            secondary = surfaceVariant,
            accent = primaryColor,
            onBackground = onBg
        )
    }
    
    // Welcome Dialog State
    val onboardingCompleted by sessionManager.onboardingCompletedFlow.collectAsStateWithLifecycle(initialValue = true) // Start assuming true to avoid flicker if already done
    var showWelcomeDialog by remember { mutableStateOf(false) }
    // One-time "What's new" screen for existing users after an update.
    var showWhatsNew by remember { mutableStateOf(false) }

    // Check actual onboarding status on launch
    LaunchedEffect(Unit) {
        val currentVersion = com.sonza.app.BuildConfig.VERSION_CODE
        if (!sessionManager.isOnboardingCompleted()) {
            showWelcomeDialog = true
            // Brand-new installs start on the current build, so they've effectively
            // seen everything up to now — don't also surface the What's New screen.
            sessionManager.setWhatsNewSeenVersion(currentVersion)
        } else if (sessionManager.getWhatsNewSeenVersion() < currentVersion) {
            // Existing user who just updated — show the one-time What's New screen.
            showWhatsNew = true
        }
    }

    // Determine device form factor for adaptive layouts
    val formFactor = LocalDeviceFormFactor.current

    if (showWelcomeDialog) {
        com.sonza.app.ui.components.WelcomeOnboardingDialog(
            onLoginClick = {
                showWelcomeDialog = false
                navController.navigate(Destination.YouTubeLogin)
            },
            onContinueAsGuest = { languages ->
                showWelcomeDialog = false
                scope.launch {
                    if (languages.isNotEmpty()) {
                        sessionManager.setPreferredLanguages(languages)
                    }
                    sessionManager.setOnboardingCompleted(true)
                }
            }
        )
    }

    if (showWhatsNew) {
        com.sonza.app.ui.components.WhatsNewDialog(
            versionLabel = "Version ${com.sonza.app.BuildConfig.VERSION_NAME}",
            onDismiss = {
                showWhatsNew = false
                scope.launch {
                    sessionManager.setWhatsNewSeenVersion(com.sonza.app.BuildConfig.VERSION_CODE)
                }
            }
        )
    }

        val density = androidx.compose.ui.platform.LocalDensity.current
        val navBarPadding = androidx.compose.foundation.layout.WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val imePadding = androidx.compose.foundation.layout.WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        val isKeyboardOpen = imePadding > 0.dp
        val shouldShowExpressiveBottomNav = showBottomNav && !isKeyboardOpen && !isPlayerExpanded && formFactor.isPhoneLike
        val navBarHeight = if (shouldShowExpressiveBottomNav && formFactor != DeviceFormFactor.TV) {
            com.sonza.app.ui.components.ExpressiveBottomNavTokens.getBottomSafePadding(playbackInfo.currentSong != null)
        } else 0.dp
        val floatingSystemHeight = 0.dp
        val snackbarBottomPadding = when {
            isPlayerExpanded -> navBarPadding + 12.dp
            shouldShowExpressiveBottomNav -> navBarPadding + navBarHeight + 12.dp
            else -> navBarPadding + 12.dp
        }

        // In Picture-in-Picture mode (Dynamic Island / Floating player), render lightweight PiP player directly
        if (mainUiState.isInPictureInPictureMode) {
            val playerState by playerViewModel.playerState.collectAsStateWithLifecycle(initialValue = com.sonza.app.core.model.PlayerState())
            com.sonza.app.ui.screens.player.PiPPlayerContent(
                song = playbackInfo.currentSong,
                isVideoMode = playerState.isVideoMode,
                player = playerViewModel.getPlayer()
            )
            return
        }

        Box(modifier = Modifier.fillMaxSize()) {
             Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = shouldShowExpressiveBottomNav,
                        enter = fadeIn(androidx.compose.animation.core.tween(200)) + androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(200)) { it },
                        exit = fadeOut(androidx.compose.animation.core.tween(200)) + androidx.compose.animation.slideOutVertically(androidx.compose.animation.core.tween(200)) { it }
                    ) {
                        Column {
                            // Floating bottom navigation (pill when idle, Home + Mini-Player + Search when playing)
                            val navBarAlpha by sessionManager.navBarAlphaFlow.collectAsStateWithLifecycle(initialValue = 1.0f)
                            val navBarBlur by sessionManager.navBarBlurFlow.collectAsStateWithLifecycle(initialValue = 60.0f)
                            val iosLiquidGlassEnabled by sessionManager.iosLiquidGlassEnabledFlow.collectAsStateWithLifecycle(initialValue = false)

                            val lastHomeClickTime = remember { mutableLongStateOf(0L) }

                            ExpressiveBottomNav(
                                currentDestination = currentDestination,
                                onDestinationChange = { dest ->
                                    navController.navigate(dest) {
                                        popUpTo<Destination.Home> {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onReClick = { dest ->
                                    if (dest == Destination.Home) {
                                        val currentTime = System.currentTimeMillis()
                                        if ((currentTime - lastHomeClickTime.longValue) < 500L) {
                                            // Double tap -> Refresh
                                            homeViewModel.triggerRefresh()
                                        } else {
                                            // Single tap -> Scroll to top
                                            homeViewModel.scrollToTop()
                                        }
                                        lastHomeClickTime.longValue = currentTime
                                    }
                                },
                                currentSong = playbackInfo.currentSong,
                                isPlaying = playbackInfo.isPlaying,
                                isLoading = playbackInfo.isLoading,
                                onPlayPause = { playerViewModel.togglePlayPause() },
                                onExpandPlayer = { playerViewModel.expandPlayer() },
                                progressProvider = miniPlayerProgressProvider,
                                dominantColors = currentDominantColors,
                                alpha = navBarAlpha,
                                iosLiquidGlassEnabled = iosLiquidGlassEnabled,
                                iosNavBarBlur = navBarBlur
                            )
                        }
                    }
                }
            ) { innerPadding ->
                // Make innerPadding available to children
                // We need to pass the bottom padding to the ExpandablePlayerSheet so it sits above the nav bar
                val currentBottomPadding = innerPadding.calculateBottomPadding()

                Row(modifier = Modifier.fillMaxSize()) {
                    // Side navigation rail for TV and Tablet
                    if (showBottomNav && !formFactor.isPhoneLike) {
                        when {
                            formFactor == DeviceFormFactor.TV -> {
                                com.sonza.app.ui.components.TvNavigationRail(
                                    currentDestination = currentDestination,
                                    onDestinationChange = { dest ->
                                        navController.navigate(dest) {
                                            popUpTo<Destination.Home> {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                            formFactor.isTabletLike -> {
                                com.sonza.app.ui.components.AdaptiveNavigationRail(
                                    currentDestination = currentDestination,
                                    onDestinationChange = { dest ->
                                        navController.navigate(dest) {
                                            popUpTo<Destination.Home> {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                            else -> { /* Phone handled by bottomBar */ }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                    // NavGraph content with its own bottom padding for the nav bar
                    val miniPlayerHeight = 0.dp
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = 0.dp // Allow content to flow behind nav bar for glass effect
                            )
                    ) {
                        NavGraph(
                            navController = navController,
                            playbackInfo = playbackInfo,
                            // NavGraph doesn't read this param; pass the distinct playbackInfo
                            // (not the per-tick playerState) so this call site doesn't recompose
                            // the whole app shell on every position update.
                            playerState = playbackInfo,
                            sessionManager = sessionManager,
                            youTubeRepository = youTubeRepository,
                            onPlaySong = { songs, index ->
                                if (songs.isNotEmpty() && index in songs.indices) {
                                    playerViewModel.playSong(songs[index], songs, index)
                                }
                            },
                            onPlayPause = { playerViewModel.togglePlayPause() },
                            onSeekTo = { playerViewModel.seekTo(it) },
                            onNext = { playerViewModel.seekToNext() },
                            onPrevious = { playerViewModel.seekToPrevious() },
                            onDownloadCurrentSong = { playerViewModel.downloadCurrentSong() },
                            onLikeCurrentSong = { playerViewModel.likeCurrentSong() },
                            onDislikeCurrentSong = { playerViewModel.dislikeCurrentSong() },
                            onShuffleToggle = { playerViewModel.toggleShuffle() },
                            onRepeatToggle = { playerViewModel.toggleRepeat() },
                            onToggleAutoplay = { playerViewModel.toggleAutoplay() },
                            onToggleVideoMode = { playerViewModel.toggleVideoMode() },
                            onDismissVideoError = { playerViewModel.dismissVideoError() },
                            onLoadMoreRadioSongs = { playerViewModel.loadMoreAutoplaySongs() },
                            isRadioMode = isRadioMode,
                            isLoadingMoreSongs = isLoadingMoreSongs,
                            onSwitchDevice = { playerViewModel.switchOutputDevice(it) },
                            onRefreshDevices = { playerViewModel.refreshDevices() },
                            player = playerViewModel.getPlayer(),
                            lyrics = lyrics,
                            isFetchingLyrics = isFetchingLyrics,
                            isLoggedIn = isLoggedIn,
                            sleepTimerOption = sleepTimerOption,
                            sleepTimerRemainingMs = sleepTimerRemainingMs,
                            onSetSleepTimer = { option, minutes -> playerViewModel.setSleepTimer(option, minutes) },
                            onSetPlaybackParameters = { speed, pitch -> playerViewModel.setPlaybackParameters(speed, pitch) },
                            volumeKeyEvents = volumeKeyEvents,
                            downloadRepository = downloadRepository,
                            selectedLyricsProvider = selectedLyricsProvider,
                            enabledLyricsProviders = playerViewModel.enabledLyricsProviders.collectAsStateWithLifecycle().value,
                            onLyricsProviderChange = { playerViewModel.switchLyricsProvider(it) },
                            startDestination = Destination.Home, // Always start at Home
                            // Removed sharedTransitionScope
                            dominantColors = currentDominantColors,
                            snackbarHostState = snackbarHostState
                        )
                    }


                }
            }
        }

    // Offline banner — slides in under the status bar whenever connectivity drops,
    // so failures elsewhere in the app have visible context.
    val isOnline by mainViewModel.isOnline.collectAsStateWithLifecycle()
    androidx.compose.animation.AnimatedVisibility(
        visible = !isOnline,
        enter = androidx.compose.animation.slideInVertically { -it } + fadeIn(),
        exit = androidx.compose.animation.slideOutVertically { -it } + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(10f)
    ) {
        androidx.compose.material3.Surface(
            color = androidx.compose.material3.MaterialTheme.colorScheme.inverseSurface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.CloudOff,
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(16.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                androidx.compose.material3.Text(
                    text = androidx.compose.ui.res.stringResource(R.string.msg_offline_banner),
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }

    // Expandable Player Sheet - Overlay
    // Sits above Scaffold, aligned to bottom
    androidx.compose.animation.AnimatedVisibility(
        visible = isPlayerExpanded || (!formFactor.isPhoneLike && showMiniPlayer),
        enter = fadeIn(androidx.compose.animation.core.tween(250)) + androidx.compose.animation.slideInVertically(
            animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        ) { it },
        exit = fadeOut(androidx.compose.animation.core.tween(200)) + androidx.compose.animation.slideOutVertically(
            animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        ) { it },
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        val density = LocalDensity.current
        val imePadding = androidx.compose.foundation.layout.WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        val isKeyboardOpen = imePadding > 0.dp
        val bottomPaddingPx = if (formFactor.isPhoneLike) {
            val navBarHeight = if (shouldShowExpressiveBottomNav) {
                com.sonza.app.ui.components.ExpressiveBottomNavTokens.getBottomSafePadding(playbackInfo.currentSong != null)
            } else 0.dp
            with(density) { navBarPadding.toPx() + navBarHeight.toPx() }
        } else {
            val navBarHeight = if (showBottomNav && !isKeyboardOpen && formFactor != DeviceFormFactor.TV) com.sonza.app.ui.components.ExpressiveBottomNavTokens.TotalBottomBarHeight else 0.dp
            with(density) { navBarPadding.toPx() + navBarHeight.toPx() }
        }

        val lastHomeClickTime = remember { mutableLongStateOf(0L) }

        ExpandablePlayerSheet(
            currentSong = playbackInfo.currentSong,
            isPlaying = playbackInfo.isPlaying,
            isLoading = playbackInfo.isLoading,
            progressProvider = miniPlayerProgressProvider,
            dominantColors = currentDominantColors,
            onPlayPause = { playerViewModel.togglePlayPause() },
            onNext = { playerViewModel.seekToNext() },
            onPrevious = { playerViewModel.seekToPrevious() },
            onClose = { playerViewModel.stop() },
            bottomPadding = bottomPaddingPx,
            isExpanded = isPlayerExpanded,
            userAlpha = miniPlayerAlpha,
            style = miniPlayerStyle,
            artworkShape = artworkShape,
            glassBlurAmount = miniPlayerGlassBlur,
            swipeDownToDismissEnabled = swipeDownToDismissEnabled,
            currentDestination = currentDestination,
            onHomeClick = {
                if (currentDestination == Destination.Home) {
                    val currentTime = System.currentTimeMillis()
                    if ((currentTime - lastHomeClickTime.longValue) < 500L) {
                        homeViewModel.triggerRefresh()
                    } else {
                        homeViewModel.scrollToTop()
                    }
                    lastHomeClickTime.longValue = currentTime
                } else {
                    navController.navigate(Destination.Home) {
                        popUpTo<Destination.Home> {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onSearchClick = {
                if (currentDestination != Destination.Search) {
                    navController.navigate(Destination.Search) {
                        popUpTo<Destination.Home> {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            isPhoneLike = formFactor.isPhoneLike,
            onExpandChange = { expanded ->
                if (expanded) playerViewModel.expandPlayer() else playerViewModel.collapsePlayer()
            },
            modifier = Modifier.fillMaxWidth(),
            expandedContent = { onCollapse ->
                // Collected HERE (not at the top scope) so the raw, ~400ms-ticking player
                // state only recomposes the open player screen — never the nav host, mini
                // player, or the underlying browse screen. This lambda is composed solely
                // while the player is expanded (see ExpandablePlayerSheet.showFullPlayer).
                val playerState by playerViewModel.playerState.collectAsStateWithLifecycle(initialValue = com.sonza.app.core.model.PlayerState())
                val playerScreenState = com.sonza.app.ui.screens.player.PlayerScreenState(
                    playbackInfo = playbackInfo,
                    playerState = playerState,
                    lyrics = lyrics,
                    isFetchingLyrics = isFetchingLyrics,
                    relatedSongs = relatedSongs,
                    isFetchingRelated = isFetchingRelated,
                    selectedRelatedIndices = selectedRelatedIndices,
                    isLoggedIn = isLoggedIn,
                    isRadioMode = isRadioMode,
                    isLoadingMoreSongs = isLoadingMoreSongs,
                    selectedLyricsProvider = selectedLyricsProvider,
                    enabledLyricsProviders = playerViewModel.enabledLyricsProviders.collectAsStateWithLifecycle().value,
                    sleepTimerOption = sleepTimerOption,
                    sleepTimerRemainingMs = sleepTimerRemainingMs
                )

                val playerScreenActions = com.sonza.app.ui.screens.player.PlayerScreenActions(
                    onPlayPause = { playerViewModel.togglePlayPause() },
                    onSeekTo = { playerViewModel.seekTo(it) },
                    onNext = { playerViewModel.seekToNext() },
                    onPrevious = { playerViewModel.seekToPrevious() },
                    onBack = onCollapse,
                    onDownload = { playerViewModel.downloadCurrentSong() },
                    onToggleLike = { playerViewModel.likeCurrentSong() },
                    onToggleDislike = { playerViewModel.dislikeCurrentSong() },
                    onShuffleToggle = { playerViewModel.toggleShuffle() },
                    onRepeatToggle = { playerViewModel.toggleRepeat() },
                    onToggleAutoplay = { playerViewModel.toggleAutoplay() },
                    onToggleVideoMode = { playerViewModel.toggleVideoMode() },
                    onSwitchAudioSource = { playerViewModel.switchAudioSource() },
                    onDismissVideoError = { playerViewModel.dismissVideoError() },
                    onLoadMoreRadioSongs = { playerViewModel.loadMoreAutoplaySongs() },
                    onPlayFromQueue = { index ->
                        if (playerState.queue.isNotEmpty() && index in playerState.queue.indices) {
                            playerViewModel.playSong(playerState.queue[index], playerState.queue, index)
                        }
                    },
                    onSwitchDevice = { device -> playerViewModel.switchOutputDevice(device) },
                    onRefreshDevices = { playerViewModel.refreshDevices() },
                    onArtistClick = { artistIdOrName ->
                        if (artistCredits.size > 1) {
                            playerViewModel.toggleMultipleArtistsDialog(true)
                        } else {
                            val finalId = artistCredits.firstOrNull()?.artistId ?: artistIdOrName
                            onCollapse()
                            navController.navigate(Destination.Artist(finalId))
                        }
                    },
                    onAlbumClick = { albumId ->
                        onCollapse()
                        navController.navigate(Destination.Album(albumId = albumId, name = null, thumbnailUrl = null))
                    },
                    onSetPlaybackParameters = { speed, pitch -> playerViewModel.setPlaybackParameters(speed, pitch) },
                    onLyricsProviderChange = { playerViewModel.switchLyricsProvider(it) },
                    onImportLyrics = { playerViewModel.importLyrics(it) },
                    onSetSleepTimer = { option, minutes -> playerViewModel.setSleepTimer(option, minutes) },
                    onPlayRelated = { song ->
                        playerViewModel.startRadio(song, null)
                    },
                    onToggleRelatedSelection = { playerViewModel.toggleRelatedSelection(it) },
                    onSelectAllRelated = { playerViewModel.selectAllRelated() },
                    onClearRelatedSelection = { playerViewModel.clearRelatedSelection() },
                    onAddRelatedToQueue = { songs ->
                        songs.forEach { playerViewModel.addToQueue(it) }
                    },
                    onAddRelatedToPlaylist = { songs ->
                        playlistManagementViewModel.showAddToPlaylistSheet(songs)
                    },
                    onShowAIEqualizer = {
                        playerViewModel.collapsePlayer()
                        if (navController.currentDestination?.hasRoute<Destination.AIEqualizer>() != true) {
                            navController.navigate(Destination.AIEqualizer) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                com.sonza.app.ui.screens.player.PlayerScreen(
                    state = playerScreenState,
                    originalActions = playerScreenActions,
                    player = playerViewModel.getPlayer(),
                    playerViewModel = playerViewModel,
                    volumeKeyEvents = volumeKeyEvents
                )
             }
        )

        // Multiple Artists Selection Dialog
        if (showMultipleArtistsDialog) {
            com.sonza.app.ui.components.player.MultipleArtistsDialog(
                artists = artistCredits,
                onArtistClick = { artistId ->
                    playerViewModel.toggleMultipleArtistsDialog(false)
                    playerViewModel.collapsePlayer()
                    navController.navigate(Destination.Artist(artistId))
                },
                onDismiss = { playerViewModel.toggleMultipleArtistsDialog(false) },
                dominantColors = currentDominantColors
            )
        }
    }    
        // Global Volume Indicator (shows on all screens except PlayerScreen when song is playing)
        if (showGlobalVolumeIndicator && volumeSliderEnabled) {
            VolumeIndicator(
                isVisible = showVolumeIndicator,
                currentVolume = currentVolume,
                maxVolume = maxVolume,
                dominantColors = currentDominantColors,
                onVolumeChange = { newVolume ->
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        newVolume,
                        0
                    )
                    currentVolume = newVolume
                    lastVolumeChangeTime = System.currentTimeMillis()
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            )
        }

        val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()
        if (updateState is UpdateState.UpdateAvailable) {
            val info = (updateState as UpdateState.UpdateAvailable).info
            UpdateDialog(
                updateInfo = info,
                onDismiss = { 
                    scope.launch { sessionManager.clearPendingUpdateInfo() }
                    updateViewModel.dismissDialog() 
                },
                onUpdate = { 
                    scope.launch { sessionManager.clearPendingUpdateInfo() }
                    updateViewModel.downloadAndInstallUpdate(info) 
                }
            )
        }
        
        if (playlistManagementUiState.selectedSongs.isNotEmpty()) {
            com.sonza.app.ui.components.AddToPlaylistSheet(
                songs = playlistManagementUiState.selectedSongs,
                isVisible = playlistManagementUiState.showAddToPlaylistSheet,
                playlists = playlistManagementUiState.userPlaylists,
                isLoading = playlistManagementUiState.isLoadingPlaylists || playlistManagementUiState.isAddingSong,
                onDismiss = { playlistManagementViewModel.hideAddToPlaylistSheet() },
                onAddToPlaylist = { playlistId -> playlistManagementViewModel.addSongsToPlaylist(playlistId) },
                onCreateNewPlaylist = { playlistManagementViewModel.showCreatePlaylistDialog() }
            )
            
            com.sonza.app.ui.components.CreatePlaylistDialog(
                isVisible = playlistManagementUiState.showCreatePlaylistDialog,
                isCreating = playlistManagementUiState.isCreatingPlaylist,
                isLoggedIn = isLoggedIn,
                onDismiss = { playlistManagementViewModel.hideCreatePlaylistDialog() },
                onCreate = { title, desc, isPrivate, sync ->
                    playlistManagementViewModel.createPlaylist(title, desc, isPrivate, sync)
                }
            )
        }

        // Dynamic Island Overlay (floating music pill near top camera cutout)
        if (dynamicIslandEnabled && playbackInfo.currentSong != null && !isPlayerExpanded) {
            val rawPlayerState by playerViewModel.playerState.collectAsStateWithLifecycle(initialValue = com.sonza.app.core.model.PlayerState())
            com.sonza.app.ui.components.dynamicisland.DynamicIsland(
                currentSong = playbackInfo.currentSong,
                isPlaying = playbackInfo.isPlaying,
                isLoading = rawPlayerState.isLoading,
                currentPosition = rawPlayerState.currentPosition,
                duration = rawPlayerState.duration,
                isLiked = playbackInfo.isLiked,
                onPlayPause = { playerViewModel.togglePlayPause() },
                onNext = { playerViewModel.seekToNext() },
                onPrevious = { playerViewModel.seekToPrevious() },
                onSeekTo = { playerViewModel.seekTo(it) },
                onLikeToggle = { playerViewModel.likeCurrentSong() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(90f)
            )
        }

        // Global Snackbar Host - Always on top
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = snackbarBottomPadding)
                .zIndex(100f) // Ensure it's above everything including player sheet
        )
    }
}
