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
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.DominantColors
import com.sonza.app.ui.components.glass.LiquidGlassSurface
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens

/**
 * iOS-style Liquid Glass mini player.
 *
 * A floating pill with a frosted glass surface. Tint derives from the current album's
 * dominant color so it feels cohesive with the rest of the UI.
 */
@Composable
fun LiquidGlassMiniPlayer(
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
    blurAmount: Float = 50f,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val effectiveAlpha = 1f - userAlpha

    val artShape = when (artworkShape) {
        "CIRCLE", "VINYL" -> CircleShape
        "SQUARE" -> androidx.compose.ui.graphics.RectangleShape
        else -> RoundedCornerShape(14.dp)
    }

    val highResThumbnail = remember(song.thumbnailUrl) {
        com.sonza.app.util.ImageUtils.getHighResThumbnailUrl(song.thumbnailUrl, size = 544)
    }

    val vinylRotation = rememberMiniPlayerVinylRotation(artworkShape, isPlaying)

    @Composable
    fun GlassButton(
        onClick: () -> Unit,
        size: Dp = 36.dp,
        content: @Composable () -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        // One shared spec for both press animations so scale and background settle in
        // lockstep — using different springs made rapid taps flicker as one finished
        // before the other.
        val pressSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.82f else 1f,
            animationSpec = pressSpec,
            label = "glassBtnScale"
        )
        val bgAlpha by animateFloatAsState(
            targetValue = if (isPressed) 0.18f else 0f,
            animationSpec = pressSpec,
            label = "glassBtnBg"
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
        ) { content() }
    }

    val pillShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceXs)
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(pillShape)
                .clickable(onClick = onTap),
            shape = pillShape,
            blurAmount = blurAmount,
            intensity = effectiveAlpha.coerceIn(0f, 1f),
            tint = dominantColors.primary,
            isDarkTheme = isDarkTheme
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(start = 10.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .graphicsLayer { rotationZ = vinylRotation() }
                            .clip(artShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = song.title,
                            style = SonzaTypography.SongTitle,
                            color = dominantColors.onBackground,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = song.artist,
                            style = SonzaTypography.ArtistSubtitle,
                            color = dominantColors.onBackground.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

                    GlassButton(onClick = onPlayPause, size = 42.dp) {
                        if (isLoading) {
                            com.sonza.app.ui.components.SonzaLoadingLogo(
                                color = dominantColors.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            AnimatedContent(
                                targetState = isPlaying,
                                transitionSpec = {
                                    (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                                        (scaleOut() + fadeOut())
                                },
                                label = "glassPlayPause"
                            ) { playing ->
                                Icon(
                                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playing) "Pause" else "Play",
                                    tint = dominantColors.onBackground,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    GlassButton(onClick = onNext, size = 42.dp) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = dominantColors.onBackground,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Progress bar at the bottom edge
                LinearProgressIndicator(
                    progress = { progressProvider().coerceIn(0f, 1f) },
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
}
