package com.sonza.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.BuildConfig
import com.sonza.app.core.model.LogoVariant
import com.sonza.app.data.SessionManager
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SettingsCard
import com.sonza.app.ui.components.drawableRes
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onDeveloperClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsOfServiceClick: () -> Unit = {},
    onLicensesClick: () -> Unit = {},
    onHowItWorksClick: () -> Unit = {}
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
    val uriHandler = LocalUriHandler.current
    val sessionManager: SessionManager = koinInject()
    val logoVariant by sessionManager.logoVariantFlow.collectAsState(initial = LogoVariant.DEFAULT)

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
                        onClick = onBack,
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
                        text = "About Sonza",
                        style = SonzaTypography.Headline,
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnBackground
                    )
                }
            }

            // Hero Brand Card
            item {
                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.SpaceLg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = SquircleShape,
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = logoVariant.drawableRes()),
                                contentDescription = "Sonza Logo",
                                tint = accentColor,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                        Text(
                            text = "Sonza",
                            style = SonzaTypography.PageTitle.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = SonzaOnBackground
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(RadiusTokens.Pill),
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Version v${BuildConfig.VERSION_NAME}",
                                style = SonzaTypography.BodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                        Text(
                            text = "A modern, privacy-respecting music streaming application designed for rich audio experiences with dynamic theming, synchronized lyrics, and seamless YouTube Music integration.",
                            style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                            color = SonzaOnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // Highlights Section
            item {
                Text(
                    text = "Key Highlights",
                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.SpaceLg),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HighlightChip("Ad-Free Streaming", accentColor)
                        HighlightChip("Synchronized Lyrics", accentColor)
                        HighlightChip("Offline Caching", accentColor)
                        HighlightChip("Dynamic Album Theming", accentColor)
                        HighlightChip("Privacy Focused", accentColor)
                        HighlightChip("Native Jetpack Compose", accentColor)
                    }
                }
            }

            // Navigation Links Section
            item {
                Text(
                    text = "Information & Legal",
                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    AboutLinkRow(
                        icon = Icons.Default.Person,
                        title = "About the Developer",
                        subtitle = "Meet Prince Kumar Jha",
                        accentColor = accentColor,
                        onClick = onDeveloperClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    AboutLinkRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        subtitle = "How Sonza handles your data",
                        accentColor = accentColor,
                        onClick = onPrivacyPolicyClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    AboutLinkRow(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Service",
                        subtitle = "Usage terms and licensing conditions",
                        accentColor = accentColor,
                        onClick = onTermsOfServiceClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    AboutLinkRow(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "Open Source Licenses",
                        subtitle = "Third-party libraries and tools",
                        accentColor = accentColor,
                        onClick = onLicensesClick
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    AboutLinkRow(
                        icon = Icons.Default.Language,
                        title = "Official Website",
                        subtitle = "princekjha-dev.github.io/Sonza-Website",
                        accentColor = accentColor,
                        onClick = { uriHandler.openUri("https://princekjha-dev.github.io/Sonza-Website/") }
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    AboutLinkRow(
                        icon = Icons.Default.Star,
                        title = "Source Code on GitHub",
                        subtitle = "github.com/princekjha-dev/Sonza",
                        accentColor = accentColor,
                        onClick = { uriHandler.openUri("https://github.com/princekjha-dev/Sonza") }
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightChip(text: String, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(RadiusTokens.Pill),
        color = accentColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(0.75.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            style = SonzaTypography.BodySmall.copy(fontWeight = FontWeight.Medium),
            color = SonzaOnBackground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun AboutLinkRow(
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
