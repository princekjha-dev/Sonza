package com.sonza.app.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.BuildConfig
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SettingsCard
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape
import com.sonza.app.ui.utils.horizontalSwipeNavigation
import com.sonza.app.ui.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Rebuilt Sonza Profile page.
 * Acts as the centralized Profile & About center for the application.
 *
 * Sections:
 * 1. Header (Back arrow + Title)
 * 2. Profile / Account Card
 * 3. Activity & Insights
 * 4. About & Support
 * 5. App Information
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

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomSystemHeight = ExpressiveBottomNavTokens.getBottomSafePadding(currentSong != null)
    val dynamicBottomInset = navBarPadding + bottomSystemHeight + SpacingTokens.Space2Xl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaBackground)
            .statusBarsPadding()
            .horizontalSwipeNavigation(
                onSwipeLeft = null,
                onSwipeRight = onNavigateToLibrary
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(
                start = SpacingTokens.SpaceLg,
                end = SpacingTokens.SpaceLg,
                top = SpacingTokens.SpaceSm,
                bottom = dynamicBottomInset
            ),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            // 1. Header (Back Arrow + Title)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.SpaceXs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Library",
                            tint = SonzaOnBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

                    Text(
                        text = "Profile",
                        style = SonzaTypography.Headline,
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnBackground
                    )
                }
            }

            // 2. Profile / Account Card
            item {
                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.SpaceLg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Container
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(2.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!uiState.userAvatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = uiState.userAvatarUrl,
                                    contentDescription = "User Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                        // User Display Name
                        Text(
                            text = uiState.userName ?: "Sonza Listener",
                            style = SonzaTypography.TitleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = SonzaOnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Status subtitle
                        Text(
                            text = if (uiState.isLoggedIn) {
                                "Connected to YouTube Music"
                            } else {
                                "Sign in for personalized mixes & cloud playlists"
                            },
                            style = SonzaTypography.BodyMedium,
                            color = SonzaOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceLg))

                        // Action Button (Sign In or Sign Out)
                        if (uiState.isLoggedIn) {
                            Surface(
                                shape = RoundedCornerShape(RadiusTokens.Lg),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.40f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.75.dp,
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.40f)
                                ),
                                modifier = Modifier
                                    .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                                        showSignOutDialog = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Sign Out",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Sign Out",
                                        style = SonzaTypography.LabelLarge,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(RadiusTokens.Lg),
                                color = accentColor.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.75.dp,
                                    accentColor.copy(alpha = 0.50f)
                                ),
                                modifier = Modifier
                                    .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                                        onLoginClick()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = "Sign In",
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Sign in to YouTube Music",
                                        style = SonzaTypography.LabelLarge,
                                        color = accentColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Activity & Insights Section
            item {
                Text(
                    text = "Activity & Insights",
                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    ProfileOptionRow(
                        icon = Icons.Default.QueryStats,
                        title = "Listening Stats",
                        subtitle = "View your listening history, top artists & tracks",
                        accentColor = accentColor,
                        onClick = onStatsClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.History,
                        title = "History & Recents",
                        subtitle = "Recently played tracks and discovery timeline",
                        accentColor = accentColor,
                        onClick = onHistoryClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Download,
                        title = "Offline Downloads",
                        subtitle = "Manage cached audio & offline tracks",
                        accentColor = accentColor,
                        onClick = onDownloadsClick
                    )
                }
            }

            // 4. About & Support Section
            item {
                Text(
                    text = "About & Support",
                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    ProfileOptionRow(
                        icon = Icons.Default.BugReport,
                        title = "Report a Bug",
                        subtitle = "Report issues or unexpected behavior",
                        accentColor = accentColor,
                        onClick = { uriHandler.openUri("https://github.com/princekjha-dev/Sonza/issues") }
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Feedback,
                        title = "Send Feedback",
                        subtitle = "Share suggestions and feature requests",
                        accentColor = accentColor,
                        onClick = onFeedbackClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        subtitle = "Learn how Sonza handles your data",
                        accentColor = accentColor,
                        onClick = onPrivacyPolicyClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Service",
                        subtitle = "Sonza usage terms and conditions",
                        accentColor = accentColor,
                        onClick = onTermsOfServiceClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Info,
                        title = "About Sonza",
                        subtitle = "Learn more about Sonza",
                        accentColor = accentColor,
                        onClick = onAboutSonzaClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Code,
                        title = "About the Developer",
                        subtitle = "Meet the developer behind Sonza",
                        accentColor = accentColor,
                        onClick = onAboutDeveloperClick
                    )
                }
            }

            // 5. App Information Section
            item {
                Text(
                    text = "App Information",
                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    ProfileVersionRow(
                        icon = Icons.Default.Layers,
                        title = "Sonza Version",
                        versionName = "v${BuildConfig.VERSION_NAME}",
                        accentColor = accentColor
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.SystemUpdate,
                        title = "Check for Updates",
                        subtitle = "Check whether a newer version is available",
                        accentColor = accentColor,
                        onClick = onCheckForUpdatesClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.MenuBook,
                        title = "Open Source Licenses",
                        subtitle = "Third-party libraries used by Sonza",
                        accentColor = accentColor,
                        onClick = onOpenSourceLicensesClick
                    )
                }
            }
        }
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", style = SonzaTypography.TitleMedium, color = SonzaOnBackground) },
            text = {
                Text(
                    "Are you sure you want to sign out of your YouTube Music account? Cached local music will remain intact.",
                    style = SonzaTypography.BodyMedium,
                    color = SonzaOnSurfaceVariant
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
                    Text("Cancel", color = SonzaOnSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(RadiusTokens.Lg)
        )
    }
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
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = SquircleShape,
            color = accentColor.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SonzaTypography.BodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = SonzaOnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = SonzaTypography.BodySmall,
                color = SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = SonzaOnSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
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
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = SquircleShape,
            color = accentColor.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SonzaTypography.BodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = SonzaOnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Current installed release",
                style = SonzaTypography.BodySmall,
                color = SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Surface(
            shape = RoundedCornerShape(RadiusTokens.Pill),
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = versionName,
                style = SonzaTypography.BodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
