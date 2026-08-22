package com.sonza.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SettingsCard
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutDeveloperScreen(
    onBackClick: () -> Unit
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
    val uriHandler = LocalUriHandler.current

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
                        text = "About the Developer",
                        style = SonzaTypography.Headline,
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnBackground
                    )
                }
            }

            // Developer Profile Card
            item {
                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.SpaceLg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(2.5.dp, accentColor.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = "https://avatars.githubusercontent.com/u/201319388?v=4",
                                contentDescription = "Prince Kumar Jha",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                        Text(
                            text = "Prince Kumar Jha",
                            style = SonzaTypography.TitleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = SonzaOnBackground
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Creator & Lead Developer of Sonza",
                            style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = accentColor
                        )

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                        Text(
                            text = "A passionate Kotlin & Android developer focused on building clean, privacy-conscious, and beautifully crafted open-source audio streaming applications.",
                            style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                            color = SonzaOnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // Mission / Project Highlights Card
            item {
                Text(
                    text = "Project Vision",
                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.SpaceLg)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = SquircleShape,
                                color = accentColor.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
                            Text(
                                text = "Crafting Sonza",
                                style = SonzaTypography.BodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = SonzaOnBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))

                        Text(
                            text = "Sonza is designed from the ground up to bring an expressive, elegant, and ad-free YouTube Music experience into a native Jetpack Compose architecture with fluid animations and dynamic album-driven theming.",
                            style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                            color = SonzaOnSurfaceVariant
                        )
                    }
                }
            }

            // Connect & Links Section
            item {
                Text(
                    text = "Connect & Links",
                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    DeveloperLinkRow(
                        icon = Icons.Default.Code,
                        title = "GitHub Profile",
                        subtitle = "github.com/princekjha-dev",
                        accentColor = accentColor,
                        onClick = { uriHandler.openUri("https://github.com/princekjha-dev") }
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    DeveloperLinkRow(
                        icon = Icons.Default.Star,
                        title = "Sonza Repository",
                        subtitle = "Star and contribute on GitHub",
                        accentColor = accentColor,
                        onClick = { uriHandler.openUri("https://github.com/princekjha-dev/Sonza") }
                    )
                    HorizontalDivider(
                        color = SonzaOutline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    )
                    DeveloperLinkRow(
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
                    DeveloperLinkRow(
                        icon = Icons.Default.Email,
                        title = "Contact Email",
                        subtitle = "princekjha.dev@gmail.com",
                        accentColor = accentColor,
                        onClick = { uriHandler.openUri("mailto:princekjha.dev@gmail.com") }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeveloperLinkRow(
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
