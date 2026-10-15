package com.sonza.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.BuildConfig
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.avatar.AvatarRepository
import com.sonza.app.ui.components.avatar.AvatarSelectionSheet
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape
import com.sonza.app.ui.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Redesigned Sonza Profile page.
 *
 * Visual Architecture:
 * - Full dark navy/black background with multi-stop ambient radial waves and glows.
 * - One large centered futuristic glass card with translucent dark-blue surface,
 *   subtle purple lighting on the top/left, cyan/blue lighting on the right,
 *   and thin glowing glass border.
 * - Top-center: large circular avatar (~150-180dp) with purple -> pink -> blue glowing ring,
 *   soft outer glow, real persisted selected avatar, and overlapping dark-glass edit pencil button.
 * - Underneath: translucent glass "Change Avatar" pill button opening the avatar picker.
 * - Centered title: "Sonza" in bright white + "Listener" in subtle purple -> blue gradient.
 * - Subtitle: "Sign in for personalized mixes & cloud playlists".
 * - Bottom of glass card: large glowing full-width button with YouTube-style play icon,
 *   bright purple -> blue -> cyan gradient, and right arrow.
 * - Fully responsive layout adapting safely across all screen sizes and navigation bar insets.
 * - Existing profile settings and insights preserved seamlessly below in matching futuristic glass styling.
 */
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit = onBackClick,
    onNavigateToLibrary: () -> Unit = onBackClick,
    onLoginClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsOfServiceClick: () -> Unit = {},
    onAboutSonzaClick: () -> Unit = {},
    onAboutDeveloperClick: () -> Unit = {},
    onCheckForUpdatesClick: () -> Unit = {},
    onOpenSourceLicensesClick: () -> Unit = {},
    currentSong: Song? = null,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
    val uriHandler = LocalUriHandler.current
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomSystemHeight = ExpressiveBottomNavTokens.getBottomSafePadding(currentSong != null)
    val dynamicBottomInset = navBarPadding + bottomSystemHeight + SpacingTokens.Space2Xl

    // Resolve active avatar entity from single repository of truth
    val currentAvatar = remember(uiState.selectedAvatarId) {
        AvatarRepository.getAvatarById(uiState.selectedAvatarId)
    }

    // Material 24dp Camera Vector
    val cameraIcon = remember {
        ImageVector.Builder(
            name = "CameraIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9f, 2f)
                lineTo(7.17f, 4f)
                horizontalLineTo(4f)
                curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
                verticalLineToRelative(12f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(16f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(6f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-3.17f)
                lineTo(15f, 2f)
                horizontalLineTo(9f)
                close()
                moveTo(12f, 17f)
                curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
                reflectiveCurveToRelative(2.24f, -5f, 5f, -5f)
                reflectiveCurveToRelative(5f, 2.24f, 5f, 5f)
                reflectiveCurveToRelative(-2.24f, 5f, -5f, 5f)
                close()
                moveTo(12f, 9f)
                curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
                reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
                reflectiveCurveToRelative(3f, -1.34f, 3f, -3f)
                reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f)
                close()
            }
        }.build()
    }

    // YouTube-style Play Vector
    val playVector = remember {
        ImageVector.Builder(
            name = "PlayVector",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(8f, 5f)
                verticalLineToRelative(14f)
                lineToRelative(11f, -7f)
                close()
            }
        }.build()
    }

    // Outer screen background container with soft ambient futuristic glows
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070812))
            .drawBehind {
                // Top-left subtle purple lighting
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7C3AED).copy(alpha = 0.24f),
                            Color(0xFF6D28D9).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.18f),
                        radius = size.width * 0.85f
                    )
                )
                // Mid-right cyan/blue subtle lighting
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF06B6D4).copy(alpha = 0.20f),
                            Color(0xFF0284C7).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.90f, size.height * 0.52f),
                        radius = size.width * 0.80f
                    )
                )
                // Lower soft indigo wave
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4F46E5).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.35f, size.height * 0.80f),
                        radius = size.width * 0.70f
                    )
                )
            }
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = dynamicBottomInset
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Sleek translucent back button + Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onBackClick,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier
                            .size(42.dp)
                            .bounceClick(scaleDown = MotionTokens.CardTapScale) { onBackClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "Profile",
                        style = SonzaTypography.Headline.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = Color.White
                    )
                }
            }

            // ========================================================
            // HERO: Centered Futuristic Glass Panel / Card
            // ========================================================
            item {
                val cardShape = RoundedCornerShape(38.dp)

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val screenWidth = maxWidth
                    val avatarSize = if (screenWidth < 360.dp) 150.dp else 172.dp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .background(Color(0xFF0F1428).copy(alpha = 0.82f))
                            .border(
                                width = 1.25.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFA855F7).copy(alpha = 0.60f), // Purple highlight top-left
                                        Color.White.copy(alpha = 0.35f),       // Specular white sheen
                                        Color(0xFF38BDF8).copy(alpha = 0.55f), // Cyan highlight bottom-right
                                        Color(0xFF1E293B).copy(alpha = 0.20f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                ),
                                shape = cardShape
                            )
                            .drawBehind {
                                // Subtle purple lighting on left/top of card
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF8B5CF6).copy(alpha = 0.22f),
                                            Color.Transparent
                                        ),
                                        center = Offset(0f, 0f),
                                        radius = size.width * 0.75f
                                    )
                                )
                                // Subtle cyan/blue lighting on right of card
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF06B6D4).copy(alpha = 0.18f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width, size.height * 0.70f),
                                        radius = size.width * 0.70f
                                    )
                                )
                                // Polished specular sheen on top lip
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0.0f to Color.White.copy(alpha = 0.12f),
                                        0.28f to Color.Transparent
                                    )
                                )
                            }
                            .padding(horizontal = 20.dp, vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ----------------------------------------------------
                            // 1. Large Avatar at top with glowing ring & edit button
                            // ----------------------------------------------------
                            Box(
                                modifier = Modifier.size(avatarSize + 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Soft outer luminous glow
                                Box(
                                    modifier = Modifier
                                        .size(avatarSize + 26.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFA855F7).copy(alpha = 0.40f),
                                                    Color(0xFFEC4899).copy(alpha = 0.25f),
                                                    Color(0xFF06B6D4).copy(alpha = 0.18f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                // Purple → pink → blue glowing ring
                                val ringBrush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFA855F7), // Purple
                                        Color(0xFFEC4899), // Pink
                                        Color(0xFF6366F1), // Indigo
                                        Color(0xFF06B6D4), // Cyan
                                        Color(0xFFA855F7)  // Loop
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .size(avatarSize + 8.dp)
                                        .clip(CircleShape)
                                        .border(3.2.dp, ringBrush, CircleShape)
                                )

                                // Circular glass container behind the avatar
                                Box(
                                    modifier = Modifier
                                        .size(avatarSize)
                                        .clip(CircleShape)
                                        .background(Color(0xFF141930).copy(alpha = 0.90f))
                                        .border(
                                            1.dp,
                                            Color.White.copy(alpha = 0.22f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Real selected 3D avatar from AvatarRepository
                                    Image(
                                        painter = painterResource(id = currentAvatar.drawableRes),
                                        contentDescription = currentAvatar.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp)
                                    )

                                    // Subtle specular glass highlight
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(
                                                Brush.verticalGradient(
                                                    0.0f to Color.White.copy(alpha = 0.20f),
                                                    0.45f to Color.Transparent
                                                )
                                            )
                                    )
                                }

                                // Small circular edit/pencil button overlapping lower-right edge
                                Surface(
                                    onClick = { showAvatarPicker = true },
                                    shape = CircleShape,
                                    color = Color(0xFF12162B).copy(alpha = 0.95f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFFA855F7),
                                                Color(0xFF38BDF8)
                                            )
                                        )
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .size(42.dp)
                                        .shadow(
                                            elevation = 12.dp,
                                            shape = CircleShape,
                                            spotColor = Color(0xFFA855F7).copy(alpha = 0.65f),
                                            ambientColor = Color(0xFF38BDF8).copy(alpha = 0.40f)
                                        )
                                        .bounceClick(scaleDown = 0.88f) {
                                            showAvatarPicker = true
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Avatar",
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // ----------------------------------------------------
                            // 2. Change Avatar Pill Button
                            // ----------------------------------------------------
                            Surface(
                                onClick = { showAvatarPicker = true },
                                shape = CircleShape,
                                color = Color(0xFF1D243E).copy(alpha = 0.72f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.30f),
                                            Color(0xFF60A5FA).copy(alpha = 0.45f),
                                            Color.White.copy(alpha = 0.20f)
                                        )
                                    )
                                ),
                                modifier = Modifier
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        spotColor = Color(0xFF38BDF8).copy(alpha = 0.30f)
                                    )
                                    .bounceClick(scaleDown = 0.93f) {
                                        showAvatarPicker = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = cameraIcon,
                                        contentDescription = null,
                                        tint = Color(0xFFE2E8F0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Change Avatar",
                                        style = SonzaTypography.LabelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            letterSpacing = 0.15.sp
                                        ),
                                        color = Color(0xFFF8FAFC)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // ----------------------------------------------------
                            // 3. Sonza Listener Title
                            // ----------------------------------------------------
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sonza ",
                                    style = SonzaTypography.Display.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "Listener",
                                    style = SonzaTypography.Display.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp,
                                        letterSpacing = (-0.5).sp,
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFC084FC), // Light purple
                                                Color(0xFF818CF8), // Indigo
                                                Color(0xFF38BDF8)  // Cyan
                                            )
                                        )
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // ----------------------------------------------------
                            // 4. Subtitle
                            // ----------------------------------------------------
                            Text(
                                text = if (uiState.isLoggedIn) {
                                    if (!uiState.userName.isNullOrBlank()) "Connected as ${uiState.userName} • Cloud & mix sync active"
                                    else "Connected to YouTube Music • Cloud & mix sync active"
                                } else {
                                    "Sign in for personalized mixes & cloud playlists"
                                },
                                style = SonzaTypography.BodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            // ----------------------------------------------------
                            // 5. YouTube Music Sign-in / Sign-out Button
                            // ----------------------------------------------------
                            val actionButtonShape = RoundedCornerShape(22.dp)
                            val actionGradient = if (uiState.isLoggedIn) {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFDC2626),
                                        Color(0xFFB91C1C),
                                        Color(0xFF991B1B)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF9333EA), // Purple
                                        Color(0xFF4F46E5), // Indigo
                                        Color(0xFF0284C7), // Blue
                                        Color(0xFF06B6D4)  // Cyan
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = actionButtonShape,
                                        spotColor = if (uiState.isLoggedIn) Color(0xFFDC2626).copy(alpha = 0.5f) else Color(0xFF8B5CF6).copy(alpha = 0.55f),
                                        ambientColor = Color(0xFF3B82F6).copy(alpha = 0.35f)
                                    )
                                    .clip(actionButtonShape)
                                    .background(actionGradient)
                                    .border(
                                        1.dp,
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.45f),
                                                Color(0xFF38BDF8).copy(alpha = 0.60f)
                                            )
                                        ),
                                        actionButtonShape
                                    )
                                    .bounceClick(scaleDown = 0.96f) {
                                        if (uiState.isLoggedIn) {
                                            showSignOutDialog = true
                                        } else {
                                            onLoginClick()
                                        }
                                    }
                                    .padding(horizontal = 18.dp, vertical = 15.dp)
                            ) {
                                // Top specular sheen
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(actionButtonShape)
                                        .background(
                                            Brush.verticalGradient(
                                                0.0f to Color.White.copy(alpha = 0.22f),
                                                0.45f to Color.Transparent
                                            )
                                        )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Left: YouTube-style play icon in translucent circular badge
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.20f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = playVector,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Center Text
                                    Text(
                                        text = if (uiState.isLoggedIn) "Sign out from YouTube Music" else "Sign in to YouTube Music",
                                        style = SonzaTypography.TitleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            letterSpacing = (-0.2).sp
                                        ),
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Right: Arrow
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.90f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ========================================================
            // PRESERVED SETTINGS & INSIGHTS (Requirement 8)
            // Rendered in sleek matching translucent dark glass cards
            // ========================================================

            // Activity & Insights Section
            item {
                ProfileSectionTitle(title = "Activity & Insights")

                FuturisticGlassCard {
                    ProfileOptionRow(
                        icon = Icons.Default.QueryStats,
                        title = "Listening Stats",
                        subtitle = "View your listening history, top artists & tracks",
                        accentColor = accentColor,
                        onClick = onStatsClick
                    )
                    FuturisticDivider()
                    ProfileOptionRow(
                        icon = Icons.Default.History,
                        title = "History & Recents",
                        subtitle = "Recently played tracks and discovery timeline",
                        accentColor = accentColor,
                        onClick = onHistoryClick
                    )
                    FuturisticDivider()
                    ProfileOptionRow(
                        icon = Icons.Default.Download,
                        title = "Offline Downloads",
                        subtitle = "Manage cached audio & offline tracks",
                        accentColor = accentColor,
                        onClick = onDownloadsClick
                    )
                }
            }

            // Support & Feedback Section
            item {
                ProfileSectionTitle(title = "Support & Feedback")

                FuturisticGlassCard {
                    ProfileOptionRow(
                        icon = Icons.Default.BugReport,
                        title = "Report a Bug",
                        subtitle = "Report issues or unexpected behavior",
                        accentColor = accentColor,
                        onClick = { uriHandler.openUri("https://github.com/princekjha-dev/Sonza/issues") }
                    )
                    FuturisticDivider()
                    ProfileOptionRow(
                        icon = Icons.Default.Feedback,
                        title = "Send Feedback",
                        subtitle = "Share suggestions and feature requests",
                        accentColor = accentColor,
                        onClick = onFeedbackClick
                    )
                }
            }

            // Privacy & Legal Section
            item {
                ProfileSectionTitle(title = "Privacy & Legal")

                FuturisticGlassCard {
                    ProfileOptionRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        subtitle = "Learn how Sonza handles your data",
                        accentColor = accentColor,
                        onClick = onPrivacyPolicyClick
                    )
                    FuturisticDivider()
                    ProfileOptionRow(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Service",
                        subtitle = "Sonza usage terms and conditions",
                        accentColor = accentColor,
                        onClick = onTermsOfServiceClick
                    )
                    FuturisticDivider()
                    ProfileOptionRow(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "Open Source Licenses",
                        subtitle = "Third-party libraries used by Sonza",
                        accentColor = accentColor,
                        onClick = onOpenSourceLicensesClick
                    )
                }
            }

            // About Section
            item {
                ProfileSectionTitle(title = "About")

                FuturisticGlassCard {
                    ProfileOptionRow(
                        icon = Icons.Default.Info,
                        title = "About Sonza",
                        subtitle = "Learn more about Sonza",
                        accentColor = accentColor,
                        onClick = onAboutSonzaClick
                    )
                    FuturisticDivider()
                    ProfileOptionRow(
                        icon = Icons.Default.Code,
                        title = "About the Developer",
                        subtitle = "Meet the developer behind Sonza",
                        accentColor = accentColor,
                        onClick = onAboutDeveloperClick
                    )
                }
            }

            // App Information Section
            item {
                ProfileSectionTitle(title = "App Information")

                FuturisticGlassCard {
                    ProfileVersionRow(
                        icon = Icons.Default.Layers,
                        title = "Sonza Version",
                        versionName = "v${BuildConfig.VERSION_NAME}",
                        accentColor = accentColor
                    )
                    FuturisticDivider()
                    ProfileOptionRow(
                        icon = Icons.Default.SystemUpdate,
                        title = "Check for Updates",
                        subtitle = "Check whether a newer version is available",
                        accentColor = accentColor,
                        onClick = onCheckForUpdatesClick
                    )
                }
            }
        }
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of your YouTube Music account? Cached local music will remain intact.",
                    style = SonzaTypography.BodyMedium,
                    color = Color(0xFF94A3B8)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showSignOutDialog = false
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF131728),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Avatar Selection Sheet - Single Source of Truth
    AvatarSelectionSheet(
        isOpen = showAvatarPicker,
        selectedAvatarId = uiState.selectedAvatarId,
        onAvatarSelected = { avatarId ->
            viewModel.setSelectedAvatarId(avatarId)
        },
        onDismiss = { showAvatarPicker = false }
    )
}

/**
 * Section Title for auxiliary profile sections
 */
@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        style = SonzaTypography.TitleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        ),
        color = Color(0xFFCBD5E1),
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/**
 * Matching dark futuristic glass card for options
 */
@Composable
private fun FuturisticGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFF0E1326).copy(alpha = 0.65f))
            .border(
                1.dp,
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.12f),
                    1.0f to Color(0xFF38BDF8).copy(alpha = 0.08f)
                ),
                cardShape
            )
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
private fun FuturisticDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.06f),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = SquircleShape,
            color = accentColor.copy(alpha = 0.14f),
            border = androidx.compose.foundation.BorderStroke(0.75.dp, accentColor.copy(alpha = 0.30f)),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SonzaTypography.BodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp
                ),
                color = Color(0xFFF1F5F9),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = SonzaTypography.BodySmall.copy(fontSize = 12.5.sp),
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun ProfileVersionRow(
    icon: ImageVector,
    title: String,
    versionName: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = SquircleShape,
            color = accentColor.copy(alpha = 0.14f),
            border = androidx.compose.foundation.BorderStroke(0.75.dp, accentColor.copy(alpha = 0.30f)),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SonzaTypography.BodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp
                ),
                color = Color(0xFFF1F5F9),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Current installed release",
                style = SonzaTypography.BodySmall.copy(fontSize = 12.5.sp),
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Surface(
            shape = RoundedCornerShape(RadiusTokens.Pill),
            color = accentColor.copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = versionName,
                style = SonzaTypography.BodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFE2E8F0),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
