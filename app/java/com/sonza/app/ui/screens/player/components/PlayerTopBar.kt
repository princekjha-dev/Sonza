package com.sonza.app.ui.screens.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Drag Indicator Handle
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(dominantColors.onBackground.copy(alpha = 0.25f))
                .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onBack)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Back / Minimize Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(dominantColors.onBackground.copy(alpha = 0.08f))
                    .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse player",
                    tint = dominantColors.onBackground,
                    modifier = Modifier.size(24.dp)
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
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isVideoMode) dominantColors.accent.copy(alpha = 0.22f) else dominantColors.onBackground.copy(alpha = 0.08f))
                            .clickable { onVideoToggle() }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OndemandVideo,
                                contentDescription = "Toggle video mode",
                                tint = if (isVideoMode) dominantColors.accent else dominantColors.onBackground.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isVideoMode) "Video" else "Audio",
                                style = SonzaTypography.LabelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = if (isVideoMode) dominantColors.accent else dominantColors.onBackground.copy(alpha = 0.85f)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Now Playing",
                        style = SonzaTypography.TitleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp,
                            fontSize = 14.sp
                        ),
                        color = dominantColors.onBackground.copy(alpha = 0.75f),
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(dominantColors.onBackground.copy(alpha = 0.08f))
                        .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onCastClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Audio output devices",
                        tint = dominantColors.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (audioArEnabled) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(dominantColors.onBackground.copy(alpha = 0.08f))
                            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onRecenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recenter Audio",
                            tint = dominantColors.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(dominantColors.onBackground.copy(alpha = 0.08f))
                        .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onMoreClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = dominantColors.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

