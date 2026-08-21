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
    val miniPlayerShape = RoundedCornerShape(16.dp)
    val blurRadius = ElevationTokens.StandardBlurRadius.value
    
    val artShape = when (artworkShape) {
        "CIRCLE", "VINYL" -> androidx.compose.foundation.shape.CircleShape
        "SQUARE" -> androidx.compose.ui.graphics.RectangleShape
        else -> RoundedCornerShape(8.dp)
    }

    val highResThumbnail = androidx.compose.runtime.remember(song.thumbnailUrl) {
        com.sonza.app.util.ImageUtils.getHighResThumbnailUrl(song.thumbnailUrl, size = 544)
    }

    val vinylRotation = rememberMiniPlayerVinylRotation(artworkShape, isPlaying)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = SpacingTokens.SpaceMd, vertical = 0.dp)
            .then(
                if (!isApi31Plus) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = miniPlayerShape,
                        ambientColor = Color.Black.copy(alpha = 0.45f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
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
                color = if (isApi31Plus) SonzaSurface.copy(alpha = 0.85f * effectiveAlpha)
                        else SonzaSurface.copy(alpha = 0.92f * effectiveAlpha)
            )
            // Subtle dynamic accent-muted wash (only when active)
            .then(
                if (!dominantColors.isIdle) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                dominantColors.accentMuted.copy(alpha = 0.18f * effectiveAlpha),
                                dominantColors.primary.copy(alpha = 0.08f * effectiveAlpha)
                            )
                        )
                    )
                } else Modifier
            )
            .border(
                width = 0.75.dp,
                color = SonzaOutline.copy(alpha = 0.35f),
                shape = miniPlayerShape
            )
            .clickable(onClick = onTap)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art thumbnail (Left-aligned)
                Box(
                    modifier = Modifier
                        .size(38.dp)
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
                            modifier = Modifier.size(18.dp),
                            tint = SonzaOnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

                // Song Title & Artist
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        style = SonzaTypography.SongTitle.copy(fontSize = 14.sp),
                        color = SonzaOnBackground,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    if (!song.artist.isNullOrBlank()) {
                        Text(
                            text = song.artist,
                            style = SonzaTypography.ArtistSubtitle.copy(fontSize = 12.sp),
                            color = SonzaOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Play/Pause button (Right edge)
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(38.dp)
                ) {
                    if (isLoading) {
                        com.sonza.app.ui.components.SonzaLoadingLogo(
                            color = SonzaOnBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = SonzaOnBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Next Button (Right edge)
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = SonzaOnBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Progress bar at the bottom edge
            LinearProgressIndicator(
                progress = progressProvider,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                trackColor = Color.Transparent,
                color = if (isPlaying) dominantColors.accent else com.sonza.app.ui.theme.SonzaColors.IdleAccent,
                strokeCap = StrokeCap.Butt
            )
        }
    }
}
