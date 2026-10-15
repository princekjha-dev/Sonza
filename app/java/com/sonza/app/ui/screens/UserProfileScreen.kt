package com.sonza.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.sonza.app.ui.components.DynamicArtworkBackground
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

    // Dynamic background container deriving atmosphere from current song or avatar fallback
    DynamicArtworkBackground(
        artworkModel = currentSong?.thumbnailUrl ?: currentAvatar.drawableRes,
        colors = dynamicColors,
        modifier = Modifier
            .fillMaxSize()
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
            // HERO: Solid Premium Card (No Glass Door, Single Avatar Control)
            // ========================================================
            item {
                val cardShape = RoundedCornerShape(24.dp)

                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = cardShape,
                            spotColor = Color.Black.copy(alpha = 0.35f),
                            ambientColor = Color.Black.copy(alpha = 0.15f)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Single Avatar with edit badge - Single Source of Truth
                        Box(
                            modifier = Modifier
                                .size(136.dp)
                                .clip(CircleShape)
                                .clickable { showAvatarPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(136.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.5.dp, accentColor.copy(alpha = 0.8f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = currentAvatar.drawableRes),
                                    contentDescription = currentAvatar.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            }

                            // Single clean edit badge overlapping lower-right edge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 2. Title
                        val displayName = if (uiState.isLoggedIn && !uiState.userName.isNullOrBlank()) {
                            uiState.userName!!
                        } else {
                            "Sonza Listener"
                        }
                        Text(
                            text = displayName,
                            style = SonzaTypography.Headline.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3. Subtitle
                        Text(
                            text = if (uiState.isLoggedIn) {
                                "Connected to YouTube Music • Cloud & mix sync active"
                            } else {
                                "Sign in for personalized mixes & cloud playlists"
                            },
                            style = SonzaTypography.BodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 4. Primary CTA Button
                        Button(
                            onClick = {
                                if (uiState.isLoggedIn) {
                                    showSignOutDialog = true
                                } else {
                                    onLoginClick()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isLoggedIn) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    accentColor
                                },
                                contentColor = if (uiState.isLoggedIn) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    Color.White
                                }
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isLoggedIn) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (uiState.isLoggedIn) "Sign out of YouTube" else "Sign in to YouTube",
                                    style = SonzaTypography.BodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ========================================================
            // PRESERVED SETTINGS & INSIGHTS (Solid Surface Cards)
            // ========================================================

            // Activity & Insights Section
            item {
                ProfileSectionTitle(title = "Activity & Insights")

                SolidSectionCard {
                    ProfileOptionRow(
                        icon = Icons.Default.QueryStats,
                        title = "Listening Stats",
                        subtitle = "View your listening history, top artists & tracks",
                        accentColor = accentColor,
                        onClick = onStatsClick
                    )
                    SolidSectionDivider()
                    ProfileOptionRow(
                        icon = Icons.Default.History,
                        title = "History & Recents",
                        subtitle = "Recently played tracks and discovery timeline",
                        accentColor = accentColor,
                        onClick = onHistoryClick
                    )
                    SolidSectionDivider()
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

                SolidSectionCard {
                    ProfileOptionRow(
                        icon = Icons.Default.BugReport,
                        title = "Report a Bug",
                        subtitle = "Report issues or unexpected behavior",
                        accentColor = accentColor,
                        onClick = { uriHandler.openUri("https://github.com/princekjha-dev/Sonza/issues") }
                    )
                    SolidSectionDivider()
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

                SolidSectionCard {
                    ProfileOptionRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        subtitle = "Learn how Sonza handles your data",
                        accentColor = accentColor,
                        onClick = onPrivacyPolicyClick
                    )
                    SolidSectionDivider()
                    ProfileOptionRow(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Service",
                        subtitle = "Sonza usage terms and conditions",
                        accentColor = accentColor,
                        onClick = onTermsOfServiceClick
                    )
                    SolidSectionDivider()
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

                SolidSectionCard {
                    ProfileOptionRow(
                        icon = Icons.Default.Info,
                        title = "About Sonza",
                        subtitle = "Learn more about Sonza",
                        accentColor = accentColor,
                        onClick = onAboutSonzaClick
                    )
                    SolidSectionDivider()
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

                SolidSectionCard {
                    ProfileVersionRow(
                        icon = Icons.Default.Layers,
                        title = "Sonza Version",
                        versionName = "v${BuildConfig.VERSION_NAME}",
                        accentColor = accentColor
                    )
                    SolidSectionDivider()
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
 * Solid surface card for profile options
 */
@Composable
private fun SolidSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)
    Surface(
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun SolidSectionDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        modifier = Modifier.padding(horizontal = 18.dp)
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
