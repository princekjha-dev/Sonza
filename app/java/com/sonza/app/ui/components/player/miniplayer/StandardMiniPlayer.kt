package com.sonza.app.ui.components.player.miniplayer

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.asComposeRenderEffect
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
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens

@Composable
fun StandardMiniPlayer(
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
    val isApi31Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val effectiveAlpha = (1f - userAlpha).coerceIn(0f, 1f)
    val miniPlayerShape = RoundedCornerShape(RadiusTokens.Md)
    val blurRadius = ElevationTokens.StandardBlurRadius.value
    
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
            .then(
                if (!isApi31Plus) {
                    Modifier.shadow(
                        elevation = ElevationTokens.Level2,
                        shape = miniPlayerShape,
                        ambientColor = Color.Black.copy(alpha = 0.40f),
                        spotColor = Color.Black.copy(alpha = 0.30f)
                    )
                } else Modifier
            )
            .clip(miniPlayerShape)
            .then(
                if (isApi31Plus && blurRadius > 0.5f) {
                    Modifier.graphicsLayer {
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            blurRadius * 2f,
                            blurRadius * 2f,
                            android.graphics.Shader.TileMode.DECAL
                        ).asComposeRenderEffect()
                    }
                } else Modifier
            )
            .background(
                color = if (isApi31Plus) SonzaSurface.copy(alpha = 0.80f * effectiveAlpha)
                        else SonzaSurface.copy(alpha = 0.92f * effectiveAlpha)
            )
            // Subtle dynamic accent-muted wash per Part 6.4
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
                color = SonzaOutline.copy(alpha = 0.7f),
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
                // Album Art thumbnail
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
                            modifier = Modifier.size(44.dp),
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

                // Song Title & Artist (Manrope typography hierarchy)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        style = SonzaTypography.SongTitle,
                        color = SonzaOnBackground,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        style = SonzaTypography.ArtistSubtitle,
                        color = SonzaOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

                // Play/Pause button with buffering spinner support per Part 6.4 & 6.7
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(36.dp)
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
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = SonzaOnBackground,
                        modifier = Modifier.size(24.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Buffering / Playback Progress Line under the bar per Part 6.4
            LinearProgressIndicator(
                progress = progressProvider,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                trackColor = SonzaOutline.copy(alpha = 0.3f),
                color = dominantColors.accent,
                strokeCap = StrokeCap.Round
            )
        }
    }
}
