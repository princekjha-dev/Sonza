package com.sonza.app.ui.screens.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.components.DominantColors
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens

@Composable
fun PlayerTopBar(
    onBack: () -> Unit,
    dominantColors: DominantColors,
    isVideoMode: Boolean = false,
    isYouTubeSong: Boolean = false,
    onVideoToggle: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onCastClick: () -> Unit = {},
    audioArEnabled: Boolean = false,
    onRecenter: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Back Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(dominantColors.onBackground.copy(alpha = 0.08f))
                .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Back",
                tint = dominantColors.onBackground,
                modifier = Modifier.size(26.dp)
            )
        }

        // Center: Switcher or Title
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isYouTubeSong) {
                // Icon-only pill switcher: Audio / Video
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(CircleShape)
                        .background(dominantColors.onBackground.copy(alpha = 0.08f))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Audio Mode Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (!isVideoMode) dominantColors.onBackground.copy(alpha = 0.16f) else Color.Transparent)
                            .bounceClick(scaleDown = MotionTokens.CardTapScale) { if (isVideoMode) onVideoToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Audio Mode",
                            tint = if (!isVideoMode) dominantColors.onBackground else dominantColors.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Video Mode Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isVideoMode) dominantColors.onBackground.copy(alpha = 0.16f) else Color.Transparent)
                            .bounceClick(scaleDown = MotionTokens.CardTapScale) { if (!isVideoMode) onVideoToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = "Video Mode",
                            tint = if (isVideoMode) dominantColors.onBackground else dominantColors.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Now Playing",
                    style = SonzaTypography.TitleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = dominantColors.onBackground.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Right side: Cast, Audio AR and More Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(dominantColors.onBackground.copy(alpha = 0.08f))
                    .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onCastClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = "Cast",
                    tint = dominantColors.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (audioArEnabled) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(dominantColors.onBackground.copy(alpha = 0.08f))
                        .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onRecenter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recenter Audio",
                        tint = dominantColors.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(dominantColors.onBackground.copy(alpha = 0.08f))
                    .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onMoreClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = dominantColors.onBackground,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

