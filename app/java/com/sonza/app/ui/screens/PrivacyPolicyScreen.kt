package com.sonza.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SettingsCard
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomSystemHeight = ExpressiveBottomNavTokens.getBottomSafePadding(false)
    val bottomInset = navBarPadding + bottomSystemHeight + SpacingTokens.Space2Xl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaBackground)
            .statusBarsPadding()
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
                bottom = bottomInset
            ),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            // Header
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
                            contentDescription = "Back",
                            tint = SonzaOnBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

                    Text(
                        text = "Privacy Policy",
                        style = SonzaTypography.Headline,
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnBackground
                    )
                }
            }

            // Summary Hero Card
            item {
                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.SpaceLg)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = SquircleShape,
                                color = accentColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
                            Column {
                                Text(
                                    text = "Privacy by Design",
                                    style = SonzaTypography.TitleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = SonzaOnBackground
                                )
                                Text(
                                    text = "Your data remains solely yours",
                                    style = SonzaTypography.BodySmall,
                                    color = SonzaOnSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                        Text(
                            text = "Sonza is built with a strict privacy-first philosophy. We do not sell personal data, maintain tracking profiles, or run advertising telemetry. All sensitive credentials are encrypted directly on your device.",
                            style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                            color = SonzaOnSurfaceVariant
                        )
                    }
                }
            }

            // Section 1: Authentication & Credentials
            item {
                PolicySection(
                    icon = Icons.Default.Lock,
                    title = "YouTube Music Authentication",
                    accentColor = accentColor,
                    description = "When you sign in to YouTube Music through Sonza, authentication occurs directly with Google / YouTube endpoints. Authentication tokens and session cookies are stored exclusively inside your device's encrypted storage (Android Keystore / EncryptedSharedPreferences). Sonza has no intermediary servers and never intercepts or collects your credentials."
                )
            }

            // Section 2: Local Storage & Downloads
            item {
                PolicySection(
                    icon = Icons.Default.Storage,
                    title = "Local Data & Offline Caching",
                    accentColor = accentColor,
                    description = "Your playlists, favorites, listening history, search history, and cached/downloaded audio tracks are stored strictly on your local device. This data is not synchronized with any proprietary Sonza server."
                )
            }

            // Section 3: Third-Party Metadata Services
            item {
                PolicySection(
                    icon = Icons.Default.Sync,
                    title = "Third-Party Metadata Providers",
                    accentColor = accentColor,
                    description = "To provide rich musical experiences, Sonza communicates with public APIs for lyrics (LRCLIB, Kugou), scrobbling (Last.fm, if enabled by you), and SponsorBlock. These requests only transmit search queries or track titles required to fulfill the metadata request."
                )
            }

            // Section 4: Analytics & Tracking
            item {
                PolicySection(
                    icon = Icons.Default.VisibilityOff,
                    title = "Zero Telemetry & Tracking",
                    accentColor = accentColor,
                    description = "Sonza does not include third-party advertising SDKs, ad-trackers, or behavioral analytics. Network traffic from Sonza is limited solely to media playback streams, lyrics fetching, metadata retrieval, and optional manual update checks."
                )
            }

            // Section 5: Data Control & Deletion
            item {
                PolicySection(
                    icon = Icons.Default.PrivacyTip,
                    title = "Your Rights & Data Control",
                    accentColor = accentColor,
                    description = "You can sign out of YouTube Music, clear your listening history, delete downloaded tracks, or reset application storage at any time via the Profile screen or your Android system application settings."
                )
            }
        }
    }
}

@Composable
private fun PolicySection(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    description: String
) {
    SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.SpaceLg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = SpacingTokens.SpaceSm)
            ) {
                Surface(
                    shape = SquircleShape,
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
                Text(
                    text = title,
                    style = SonzaTypography.BodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground
                )
            }

            Text(
                text = description,
                style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                color = SonzaOnSurfaceVariant
            )
        }
    }
}
