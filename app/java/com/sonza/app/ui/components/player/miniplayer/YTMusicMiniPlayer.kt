package com.sonza.app.ui.components.player.miniplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.DominantColors
import com.sonza.app.ui.theme.ElevationTokens
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens

/**
 * Compact Floating Mini Player per Part Q (Q34–Q58).
 *
 * Sits above the Bottom Navigation with rounded corners, subtle elevation,
 * clear typography hierarchy, responsive playback controls, and live progress line.
 */
@Composable
fun YTMusicMiniPlayer(
    song: Song,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    dominantColors: DominantColors,
    progressProvider: () -> Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    onTap: () -> Unit,
    userAlpha: Float = 0f,
    artworkShape: String = "ROUNDED_SQUARE",
    modifier: Modifier = Modifier
) {
    val effectiveAlpha = (1f - userAlpha).coerceIn(0f, 1f)
    val miniPlayerShape = RoundedCornerShape(RadiusTokens.Md)

    val artShape = when (artworkShape) {
        "CIRCLE", "VINYL" -> androidx.compose.foundation.shape.CircleShape
        "SQUARE" -> androidx.compose.ui.graphics.RectangleShape
        else -> RoundedCornerShape(RadiusTokens.Sm)
    }

    val highResThumbnail = androidx.compose.runtime.remember(song.thumbnailUrl) {
        com.sonza.app.util.ImageUtils.getHighResThumbnailUrl(song.thumbnailUrl, size = 544)
    }

    val vinylRotation = rememberMiniPlayerVinylRotation(artworkShape, isPlaying)

    Box(
        modifier = modifier
            .padding(horizontal = SpacingTokens.SpaceSm, vertical = SpacingTokens.SpaceXs)
            .shadow(
                elevation = ElevationTokens.Level2,
                shape = miniPlayerShape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(miniPlayerShape)
            .background(SonzaSurface.copy(alpha = 0.95f * effectiveAlpha))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        dominantColors.accentMuted.copy(alpha = 0.20f * effectiveAlpha),
                        dominantColors.primary.copy(alpha = 0.08f * effectiveAlpha)
                    )
                )
            )
            .border(
                width = 0.75.dp,
                color = SonzaOutline.copy(alpha = 0.6f),
                shape = miniPlayerShape
            )
            .clickable(onClick = onTap)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = SpacingTokens.SpaceMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art - square with rounded corners
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer { rotationZ = vinylRotation() }
                        .clip(artShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (highResThumbnail != null) {
                        AsyncImage(
                            model = highResThumbnail,
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = SonzaOnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

                // Song Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        style = SonzaTypography.TitleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = SonzaOnBackground,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = song.artist,
                        style = SonzaTypography.BodyMedium.copy(
                            fontSize = 12.sp
                        ),
                        color = SonzaOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

                // Play/Pause Button
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(38.dp)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = dominantColors.accent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = dominantColors.accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Next Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = SonzaOnBackground,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (!isPlaying) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SonzaOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Progress bar at the bottom edge
            LinearProgressIndicator(
                progress = progressProvider,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                trackColor = Color.Transparent,
                color = dominantColors.accent,
                strokeCap = StrokeCap.Butt
            )
        }
    }
}

