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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape

/**
 * Rebuilt Privacy Policy screen delivering a professional, document-oriented legal reading
 * experience with clear hierarchy, readable typography, and layout-aware bottom insets
 * preventing global mini-player overlap.
 */
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    currentSong: Song? = null
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
    val uriHandler = LocalUriHandler.current

    // Dynamic mini-player aware bottom padding
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val hasMiniPlayer = currentSong != null
    val bottomSystemHeight = if (hasMiniPlayer) ExpressiveBottomNavTokens.TotalBottomBarHeight else 0.dp
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
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(
                start = SpacingTokens.SpaceLg,
                end = SpacingTokens.SpaceLg,
                top = SpacingTokens.SpaceSm,
                bottom = bottomInset
            ),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceLg)
        ) {
            // 1. Navigation Top Bar
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

            // 2. Document Header & Hero Overview
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.SpaceXs, bottom = SpacingTokens.SpaceSm)
                ) {
                    Text(
                        text = "DATA TRANSPARENCY & PRIVACY",
                        style = SonzaTypography.Kicker,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Privacy at Sonza",
                        style = SonzaTypography.Display.copy(fontSize = 26.sp, lineHeight = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Last updated: August 2026 • Free & Open Source",
                        style = SonzaTypography.BodySmall,
                        color = SonzaOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                    // Lead Summary Callout
                    Surface(
                        shape = SquircleShape,
                        color = SonzaSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            accentColor.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SpacingTokens.SpaceLg),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = SquircleShape,
                                color = accentColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Our Privacy Commitment",
                                    style = SonzaTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SonzaOnBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sonza is built from the ground up on a strict privacy-first foundation. We do not sell personal data, maintain tracking profiles, run advertising telemetry, or operate intermediate cloud servers that harvest your activity. All user authentication tokens and sensitive credentials remain encrypted directly on your local device.",
                                    style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                                    color = SonzaOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Section 1: Information We Collect
            item {
                LegalSectionContainer(
                    number = "1",
                    title = "Information We Collect",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "Sonza is a client application designed to operate with minimal data collection. We strictly distinguish between local on-device data and external network transmissions:"
                    )

                    LegalBulletPoint(
                        title = "Local Device Data",
                        description = "Your playlists, favorites, listening history, search queries, customized settings, equalizer presets, and cached media tracks are stored strictly inside your device's private storage. This data is never synchronized with any proprietary Sonza server.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "No Account Creation on Sonza",
                        description = "You are not required to create a Sonza account. Sonza does not maintain user databases, email lists, or remote user tracking profiles.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Optional User-Initiated Feedback",
                        description = "If you choose to submit in-app feedback or report an issue via the Support screen, basic technical metadata (device model, Android OS version, app version, and screen resolution) is transmitted solely to help diagnose and resolve bugs. Providing your name or email is strictly optional.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 2: YouTube Music Authentication
            item {
                LegalSectionContainer(
                    number = "2",
                    title = "YouTube Music Authentication",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "When you choose to sign in to your YouTube Music account within Sonza to access your personal playlists and library:"
                    )

                    LegalBulletPoint(
                        title = "Direct Google Authentication",
                        description = "Authentication takes place directly through official Google / YouTube web endpoints. Sonza never requests, collects, or intercepts your Google account password directly.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Hardware-Backed Encrypted Storage",
                        description = "Session cookies and authentication tokens are stored exclusively on your device using Android EncryptedSharedPreferences backed by the hardware Android Keystore with AES-256-GCM encryption. Credentials are never exposed in plaintext or transmitted to any third-party proxy.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "No Intermediary Servers",
                        description = "All network traffic for authenticated requests travels directly between your device and Google's servers. Sonza operates zero backend servers that could read or intercept your session credentials.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 3: Local Data & Offline Storage
            item {
                LegalSectionContainer(
                    number = "3",
                    title = "Local Data & Offline Storage",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "Sonza stores various application data locally on your device to enable offline playback, fast navigation, and continuous listening. This includes:"
                    )

                    LegalBulletPoint(
                        title = "Local Database (Room)",
                        description = "Custom playlists, song favorites, artists, albums, playback history, genre associations, and blocked/disliked songs are stored in an on-device SQLite database.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Lyrics Cache",
                        description = "Synchronized and static LRC lyrics fetched during song playback are cached locally to minimize redundant network bandwidth on repeat listens.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Media Cache & Downloads",
                        description = "Stream segments and downloaded songs are kept in private app storage. Downloaded tracks remain fully accessible for offline listening and are not uploaded elsewhere.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "User Preferences",
                        description = "UI appearance, accent color themes, audio playback quality settings, SponsorBlock categories, and equalizer profiles are kept locally in DataStore.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 4: Third-Party Services
            item {
                LegalSectionContainer(
                    number = "4",
                    title = "Third-Party Services & Integrations",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "To provide audio playback, lyrics, scrobbling, and rich metadata, Sonza communicates directly with third-party and public APIs. Here is what is transmitted:"
                    )

                    LegalBulletPoint(
                        title = "YouTube & Google Innertube API",
                        description = "Used to search music, stream audio/video tracks, and load album/artist metadata. Requests transmit search queries and standard HTTP headers directly to Google servers.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "LRCLIB",
                        description = "An open-source lyrics provider. Sonza queries LRCLIB with the track title, artist name, and duration to retrieve time-synced lyrics. No user identifiers are sent.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Kugou Lyrics",
                        description = "A supplementary lyrics provider used to fetch synchronized lyrics. Queries only contain track title and artist search parameters.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Last.fm Scrobbling (Optional)",
                        description = "If you explicitly link your Last.fm account, Sonza transmits the title and artist of played songs to your personal Last.fm profile. Session keys are encrypted on-device.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "SponsorBlock (Optional)",
                        description = "Queries public community databases with YouTube video IDs to automatically skip non-music intros, outros, and sponsor segments. No personal information is transmitted.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "AI Providers (Optional)",
                        description = "If you configure an optional AI provider (Google Gemini, OpenAI, Anthropic), your user-provided API key is stored securely in EncryptedSharedPreferences and used solely to fulfill your prompt requests.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "GitHub Releases API",
                        description = "Sonza queries the public GitHub Releases API to check for application updates. No telemetry or device tracking identifiers are attached.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 5: Analytics & Tracking
            item {
                LegalSectionContainer(
                    number = "5",
                    title = "Analytics, Telemetry & Tracking",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "Sonza is committed to zero behavioral surveillance and tracking:"
                    )

                    LegalBulletPoint(
                        title = "Zero Advertising Networks",
                        description = "Sonza contains no advertising SDKs, banner ads, tracking pixels, or monetization trackers.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Zero Analytics Telemetry",
                        description = "We do not integrate Google Firebase Analytics, Mixpanel, Segment, or any other user behavioral telemetry SDKs.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "No Background Crash Tracking",
                        description = "Sonza does not run automated third-party crash telemetry (such as Firebase Crashlytics or Sentry). Errors are logged only locally to Android logcat for debugging.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 6: Data Retention & Deletion
            item {
                LegalSectionContainer(
                    number = "6",
                    title = "Data Retention & Deletion",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "You maintain complete ownership and control over your data lifecycle:"
                    )

                    LegalBulletPoint(
                        title = "Signing Out",
                        description = "Signing out of YouTube Music immediately purges all encrypted session cookies, authentication tokens, and cached account details from your device's Keystore.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Manual Data Deletion",
                        description = "You can individually clear your listening history, delete downloaded audio files, wipe cached lyrics, or clear playback cache directly from the Profile / Settings screen.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "App Uninstallation",
                        description = "Uninstalling Sonza deletes all local Room databases, cached audio streams, EncryptedSharedPreferences, and settings stored in private app storage.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "External Services Retention",
                        description = "Information stored on external services (such as your YouTube account history or Last.fm scrobble profile) is retained on those independent platforms and governed by their respective privacy policies.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 7: Your Rights & Choices
            item {
                LegalSectionContainer(
                    number = "7",
                    title = "Your Rights & Choices",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "Sonza provides direct settings allowing you to customize your privacy posture:"
                    )

                    LegalBulletPoint(
                        title = "Toggle External Providers",
                        description = "Enable or disable lyrics providers (LRCLIB, Kugou), SponsorBlock segment skipping, or Last.fm integration at any time in Settings.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Control Storage & Cache",
                        description = "Set cache size limits, clear media caches, and manage offline downloads via the Storage & Cache settings.",
                        accentColor = accentColor
                    )

                    LegalBulletPoint(
                        title = "Export & Import Data",
                        description = "Export and back up your custom playlists in standard formats without depending on proprietary cloud lockers.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 8: Changes to This Privacy Policy
            item {
                LegalSectionContainer(
                    number = "8",
                    title = "Changes to This Privacy Policy",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "We may update this Privacy Policy from time to time to reflect new features, provider integrations, or regulatory requirements. Any modifications will be reflected by updating the 'Last updated' date at the top of this document and will be documented in the application changelog and GitHub release notes."
                    )
                }
            }

            // Section 9: Contact & Inquiries
            item {
                LegalSectionContainer(
                    number = "9",
                    title = "Contact & Inquiries",
                    accentColor = accentColor
                ) {
                    LegalParagraph(
                        text = "If you have questions, feedback, or concerns regarding Sonza's privacy practices or security implementation, please reach out via any of our official channels:"
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))

                    Surface(
                        shape = SquircleShape,
                        color = SonzaSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            SonzaOutline.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            LegalContactRow(
                                icon = Icons.Default.Code,
                                title = "GitHub Repository",
                                subtitle = "github.com/princekjha-dev/Sonza",
                                accentColor = accentColor,
                                onClick = { uriHandler.openUri("https://github.com/princekjha-dev/Sonza") }
                            )
                            HorizontalDivider(
                                color = SonzaOutline.copy(alpha = 0.25f),
                                modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                            )
                            LegalContactRow(
                                icon = Icons.Default.Email,
                                title = "Developer Email",
                                subtitle = "pkjha2028@gmail.com",
                                accentColor = accentColor,
                                onClick = { uriHandler.openUri("mailto:pkjha2028@gmail.com") }
                            )
                            HorizontalDivider(
                                color = SonzaOutline.copy(alpha = 0.25f),
                                modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                            )
                            LegalContactRow(
                                icon = Icons.Default.Language,
                                title = "Official Website",
                                subtitle = "princekjha-dev.github.io/Sonza-Website",
                                accentColor = accentColor,
                                onClick = { uriHandler.openUri("https://princekjha-dev.github.io/Sonza-Website/") }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean, document-oriented section container with clear visual separation.
 */
@Composable
private fun LegalSectionContainer(
    number: String,
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.SpaceXs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = SpacingTokens.SpaceSm)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = SonzaTypography.LabelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            Text(
                text = title,
                style = SonzaTypography.TitleLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = SonzaOnBackground
            )
        }

        content()

        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        HorizontalDivider(
            color = SonzaOutline.copy(alpha = 0.25f),
            modifier = Modifier.padding(top = SpacingTokens.SpaceSm)
        )
    }
}

/**
 * Standard readable legal body paragraph.
 */
@Composable
private fun LegalParagraph(
    text: String
) {
    Text(
        text = text,
        style = SonzaTypography.BodyMedium.copy(
            fontSize = 14.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp
        ),
        color = SonzaOnSurfaceVariant,
        modifier = Modifier.padding(bottom = SpacingTokens.SpaceSm)
    )
}

/**
 * Structured bullet point item with title and description.
 */
@Composable
private fun LegalBulletPoint(
    title: String,
    description: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor)
        )

        Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SonzaTypography.BodyLarge.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = SonzaOnBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = SonzaTypography.BodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp
                ),
                color = SonzaOnSurfaceVariant
            )
        }
    }
}

/**
 * Interactive link row for contact and external references.
 */
@Composable
private fun LegalContactRow(
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
            .padding(
                horizontal = SpacingTokens.SpaceLg,
                vertical = SpacingTokens.SpaceMd
            ),
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
                modifier = Modifier.padding(9.dp)
            )
        }

        Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SonzaTypography.BodyLarge.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = SonzaOnBackground
            )
            Text(
                text = subtitle,
                style = SonzaTypography.BodySmall,
                color = SonzaOnSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SonzaOnSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}
