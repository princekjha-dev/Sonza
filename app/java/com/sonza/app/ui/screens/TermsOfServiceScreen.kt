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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun TermsOfServiceScreen(
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
                        text = "Terms of Service",
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
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
                            Column {
                                Text(
                                    text = "Terms & Conditions",
                                    style = SonzaTypography.TitleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = SonzaOnBackground
                                )
                                Text(
                                    text = "General guidelines & open source terms",
                                    style = SonzaTypography.BodySmall,
                                    color = SonzaOnSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                        Text(
                            text = "Please read these terms carefully before using Sonza. Sonza is an open-source, non-commercial software client provided free of charge to enhance your personal music playback experience.",
                            style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                            color = SonzaOnSurfaceVariant
                        )
                    }
                }
            }

            // Section 1: Acceptance of Terms
            item {
                TermsSection(
                    icon = Icons.Default.Description,
                    title = "1. Acceptance of Terms",
                    accentColor = accentColor,
                    description = "By downloading, installing, or utilizing the Sonza application, you agree to be bound by these Terms of Service. If you do not agree with any part of these terms, please discontinue use of the application."
                )
            }

            // Section 2: Non-Commercial Personal Use
            item {
                TermsSection(
                    icon = Icons.Default.Person,
                    title = "2. Personal & Non-Commercial Use",
                    accentColor = accentColor,
                    description = "Sonza is intended solely for personal, non-commercial purposes. You may not sell, sublicense, or commercially exploit the application or any stream content accessed through it."
                )
            }

            // Section 3: Third-Party Content & YouTube Music Disclaimer
            item {
                TermsSection(
                    icon = Icons.Default.Info,
                    title = "3. Third-Party Content Disclaimer",
                    accentColor = accentColor,
                    description = "Sonza is an independent client application and is not associated with, sponsored by, or endorsed by Google LLC or YouTube Music. All audio streams, album covers, titles, and artist trademarks remain the property of their respective copyright owners."
                )
            }

            // Section 4: Open Source License
            item {
                TermsSection(
                    icon = Icons.Default.Code,
                    title = "4. Open Source Licensing",
                    accentColor = accentColor,
                    description = "Sonza is open-source software licensed under the GNU General Public License v3.0 (GPLv3). You are encouraged to inspect, contribute, or modify the source code under the obligations specified in the GPLv3 license."
                )
            }

            // Section 5: Limitation of Liability
            item {
                TermsSection(
                    icon = Icons.Default.Warning,
                    title = "5. Disclaimer of Warranties",
                    accentColor = accentColor,
                    description = "Sonza is provided on an \"AS IS\" and \"AS AVAILABLE\" basis without warranties of any kind. The developers shall not be liable for any direct, indirect, or incidental damages resulting from the use or inability to use this application."
                )
            }
        }
    }
}

@Composable
private fun TermsSection(
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
