package com.sonza.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
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
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape

/**
 * Rebuilt Terms of Service screen delivering a legal-grade document reading experience
 * with clear numbered section hierarchy, comfortable typography, and dynamic mini-player
 * safe insets preventing content obstruction.
 */
@Composable
fun TermsOfServiceScreen(
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
                        text = "Terms of Service",
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
                        text = "TERMS OF USE & LICENSING",
                        style = SonzaTypography.Kicker,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Terms & Conditions",
                        style = SonzaTypography.Display.copy(fontSize = 26.sp, lineHeight = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Last updated: August 2026 • GNU General Public License v3.0",
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
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Agreement Overview",
                                    style = SonzaTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SonzaOnBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Please read these Terms of Service carefully before utilizing Sonza. Sonza is a free, open-source, non-commercial software client provided to give users an expressive, ad-free, and privacy-respecting audio playback experience. By downloading, installing, or using the application, you agree to be bound by the terms outlined below.",
                                    style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                                    color = SonzaOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Section 1: Acceptance of Terms
            item {
                TermsSectionContainer(
                    number = "1",
                    title = "Acceptance of Terms",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "By accessing, downloading, installing, or otherwise using the Sonza application, you acknowledge that you have read, understood, and agree to be bound by these Terms of Service and all applicable laws and regulations. If you do not agree to any portion of these terms, you must immediately uninstall and discontinue using Sonza."
                    )
                }
            }

            // Section 2: Personal & Non-Commercial Use
            item {
                TermsSectionContainer(
                    number = "2",
                    title = "Personal & Non-Commercial Use",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "Sonza is developed and distributed exclusively for personal, non-commercial use. Under these terms:"
                    )

                    TermsBulletPoint(
                        title = "Non-Commercial License",
                        description = "You may not sell, rent, lease, sublicense, redistribute for commercial gain, or monetize Sonza or any media streams accessed through the application.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "Individual Consumption",
                        description = "The application is intended solely to facilitate individual media listening and personal playlist management.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 3: Third-Party Content Disclaimer
            item {
                TermsSectionContainer(
                    number = "3",
                    title = "Third-Party Content Disclaimer",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "Sonza functions strictly as an independent open-source client and media player:"
                    )

                    TermsBulletPoint(
                        title = "No Affiliation with Google or YouTube",
                        description = "Sonza is an independent project and is not affiliated, associated, authorized, endorsed by, or in any way officially connected with Google LLC, YouTube, YouTube Music, or Alphabet Inc.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "Intellectual Property of Media",
                        description = "All music tracks, audio streams, album cover art, song lyrics, artist names, and trademarks accessed through Sonza remain the sole intellectual property of their respective copyright holders.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "No Content Hosting",
                        description = "Sonza does not host, upload, or own any of the audio or video content streamed through the app. All stream data is fetched directly from YouTube/Google endpoints.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 4: Music & Copyright Compliance
            item {
                TermsSectionContainer(
                    number = "4",
                    title = "Music & Copyright Compliance",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "Sonza respects the intellectual property rights of artists, creators, and record labels. By utilizing the app, you agree that:"
                    )

                    TermsBulletPoint(
                        title = "Compliance with Service Terms",
                        description = "Your use of YouTube Music streams through Sonza remains subject to YouTube's Terms of Service and Community Guidelines.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "No Unauthorized Redistribution",
                        description = "You may not extract, re-upload, broadcast, or commercially syndicate audio streams obtained through Sonza in violation of applicable copyright laws.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 5: User Responsibilities
            item {
                TermsSectionContainer(
                    number = "5",
                    title = "User Responsibilities",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "As a user of Sonza, you are solely responsible for:"
                    )

                    TermsBulletPoint(
                        title = "Account Security",
                        description = "Maintaining the confidentiality and security of your Google/YouTube account and any personal API keys (e.g. Last.fm, Gemini, OpenAI) configured inside the app.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "Lawful Usage",
                        description = "Ensuring that your use of the application complies with all local, state, national, and international laws, regulations, and copyright statutes.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 6: Prohibited Use
            item {
                TermsSectionContainer(
                    number = "6",
                    title = "Prohibited Use",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "You agree not to engage in any of the following prohibited actions while using Sonza:"
                    )

                    TermsBulletPoint(
                        title = "Commercial Exploitation",
                        description = "Selling access to the software, bundling Sonza with paid commercial packages, or embedding commercial advertising overlays.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "Malicious Modification",
                        description = "Distributing altered or compromised versions of Sonza designed to harvest user credentials, inject malware, or violate user privacy.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "Automated Abuse & Scraping",
                        description = "Using automated scripts, scrapers, or bots to overwhelm, disrupt, or impair third-party APIs (YouTube, LRCLIB, Kugou, SponsorBlock) integrated with Sonza.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 7: Third-Party Services
            item {
                TermsSectionContainer(
                    number = "7",
                    title = "Third-Party Services & Integrations",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "Sonza integrates with several external services (YouTube Music, LRCLIB, Kugou, Last.fm, SponsorBlock, and AI providers) to enhance audio playback and metadata discovery. You acknowledge that:"
                    )

                    TermsBulletPoint(
                        title = "Independent Availability",
                        description = "Third-party APIs operate independently. Sonza developers cannot guarantee the uninterrupted availability, uptime, or data accuracy of external services.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "External Terms Apply",
                        description = "Your interaction with third-party providers is governed by their respective terms of service and privacy agreements.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 8: Availability & Changes
            item {
                TermsSectionContainer(
                    number = "8",
                    title = "Availability & Modifications",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "Sonza is maintained by volunteer open-source contributors. Features, integrations, UI layouts, and supported Android versions may be added, updated, modified, or retired at any time without prior notice. We make no guarantee that specific third-party APIs or features will remain available indefinitely."
                    )
                }
            }

            // Section 9: Disclaimer of Warranties
            item {
                TermsSectionContainer(
                    number = "9",
                    title = "Disclaimer of Warranties",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "SONZA IS PROVIDED ON AN \"AS IS\" AND \"AS AVAILABLE\" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE, NON-INFRINGEMENT, OR FREEDOM FROM COMPUTER VIRUSES OR DEFECTS."
                    )
                    TermsParagraph(
                        text = "THE DEVELOPERS AND CONTRIBUTORS DO NOT WARRANT THAT SONZA WILL BE UNINTERRUPTED, ERROR-FREE, SECURE, OR THAT DEFECTS WILL BE IMMEDIATELY CORRECTED."
                    )
                }
            }

            // Section 10: Limitation of Liability
            item {
                TermsSectionContainer(
                    number = "10",
                    title = "Limitation of Liability",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "TO THE FULLEST EXTENT PERMITTED BY APPLICABLE LAW, IN NO EVENT SHALL THE DEVELOPERS, MAINTAINERS, OR CONTRIBUTORS OF SONZA BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES (INCLUDING LOSS OF DATA, DEVICE DAMAGE, LOSS OF PROFITS, OR SERVICE INTERRUPTIONS) ARISING OUT OF OR IN CONNECTION WITH YOUR ACCESS TO, USE OF, OR INABILITY TO USE SONZA."
                    )
                }
            }

            // Section 11: Open Source Components
            item {
                TermsSectionContainer(
                    number = "11",
                    title = "Open Source Licensing (GPLv3)",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "Sonza is free and open-source software licensed under the GNU General Public License v3.0 (GPLv3). Under this license:"
                    )

                    TermsBulletPoint(
                        title = "Freedom to Inspect & Modify",
                        description = "You have the freedom to inspect the source code, fork the project, and create derivative works in accordance with the terms of the GNU General Public License v3.0.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "Copyleft Obligations",
                        description = "Any distributed modifications or derivative software based on Sonza must also be released under the GPLv3 license with full source code availability.",
                        accentColor = accentColor
                    )

                    TermsBulletPoint(
                        title = "Third-Party Libraries",
                        description = "Third-party libraries used in Sonza are licensed under their respective open-source licenses (Apache 2.0, MIT, BSD), detailed in the Open Source Licenses screen.",
                        accentColor = accentColor
                    )
                }
            }

            // Section 12: Changes to These Terms
            item {
                TermsSectionContainer(
                    number = "12",
                    title = "Changes to These Terms",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "We reserve the right to amend or update these Terms of Service at our discretion. Any modifications will be posted within the application with an updated effective date at the top of this document. Continued use of Sonza after updates constitutes acceptance of the modified terms."
                    )
                }
            }

            // Section 13: Contact & Inquiries
            item {
                TermsSectionContainer(
                    number = "13",
                    title = "Contact & Inquiries",
                    accentColor = accentColor
                ) {
                    TermsParagraph(
                        text = "For legal inquiries, feedback, or contribution questions regarding these Terms of Service or the Sonza open-source project, please contact us:"
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
                            TermsContactRow(
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
                            TermsContactRow(
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
                            TermsContactRow(
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
private fun TermsSectionContainer(
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
private fun TermsParagraph(
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
private fun TermsBulletPoint(
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
private fun TermsContactRow(
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
