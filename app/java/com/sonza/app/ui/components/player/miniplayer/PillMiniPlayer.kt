package com.sonza.app.ui.components.player.miniplayer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.crossfade
import com.sonza.app.R
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.DominantColors
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens

@Composable
fun PillMiniPlayer(
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val effectiveAlpha = 1f - userAlpha
    
    val artShape = when (artworkShape) {
        "CIRCLE", "VINYL" -> CircleShape
        "SQUARE" -> androidx.compose.ui.graphics.RectangleShape
        else -> RoundedCornerShape(32.dp)
    }

    val highResThumbnail = remember(song.thumbnailUrl) {
        com.sonza.app.util.ImageUtils.getHighResThumbnailUrl(song.thumbnailUrl, size = 544)
    }

    val vinylRotation = rememberMiniPlayerVinylRotation(artworkShape, isPlaying)

    @Composable
    fun MiniPlayerButton(
        onClick: () -> Unit,
        size: Dp = 36.dp,
        content: @Composable () -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.78f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "miniPlayerBtnScale"
        )

        val bgAlpha by animateFloatAsState(
            targetValue = if (isPressed) 0.15f else 0f,
            animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
            label = "miniPlayerBtnBg"
        )

        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .clip(CircleShape)
                .background(dominantColors.onBackground.copy(alpha = bgAlpha))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = SpacingTokens.SpaceMd, vertical = 0.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onTap),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            dominantColors.primary.copy(alpha = effectiveAlpha),
                            dominantColors.secondary.copy(alpha = effectiveAlpha)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left-aligned Artwork with circular progress track
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val activeColor = if (isPlaying) dominantColors.accent else com.sonza.app.ui.theme.SonzaColors.IdleAccent
                    val trackColor = dominantColors.onBackground.copy(alpha = 0.2f)
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 2.dp.toPx()
                        val dashSize = 2.dp.toPx()
                        val gapSize = 3.dp.toPx()
                        
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashSize, gapSize), 0f)
                            )
                        )
                        
                        drawArc(
                            color = activeColor,
                            startAngle = -90f,
                            sweepAngle = progressProvider() * 360f,
                            useCenter = false,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashSize, gapSize), 0f)
                            )
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
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
                                tint = dominantColors.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

                // Title & Artist
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        style = SonzaTypography.SongTitle.copy(fontSize = 14.sp),
                        color = dominantColors.onBackground,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    if (!song.artist.isNullOrBlank()) {
                        Text(
                            text = song.artist,
                            style = SonzaTypography.ArtistSubtitle.copy(fontSize = 12.sp),
                            color = dominantColors.onBackground.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Play/Pause button with single loader
                MiniPlayerButton(onClick = onPlayPause, size = 38.dp) {
                    if (isLoading) {
                        AsyncImage(
                            model = coil3.request.ImageRequest.Builder(context)
                                .data(R.raw.loding)
                                .crossfade(false)
                                .build(),
                            contentDescription = "Loading",
                            modifier = Modifier.size(26.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                                (scaleOut() + fadeOut())
                            },
                            label = "miniPlayPause"
                        ) { playing ->
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = dominantColors.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Next button (Right edge)
                MiniPlayerButton(onClick = onNext, size = 38.dp) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = dominantColors.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}
