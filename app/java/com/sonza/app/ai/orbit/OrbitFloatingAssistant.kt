package com.sonza.app.ai.orbit

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import kotlin.math.roundToInt

/**
 * Orbit Overlay — Persistent ambient assistant character layer.
 *
 * Architecture:
 * SonzaRoot
 * │
 * ├── Header (MicrophoneButton + UserAvatar)
 * ├── HomeContent
 * ├── MusicPlayer
 * └── OrbitOverlay
 *     └── OrbitAvatar (ONLY ONE in the entire application)
 *
 * Orbit is a persistent living character inside Sonza:
 * - App opened / No music: IDLE mode with smooth circular floating animation.
 * - Song playing: MUSIC mode with headphones and music-reactive grooving & equalizer waveforms.
 * - Hey Orbit / Mic tapped: LISTENING mode with pulsing rings, breathing, and audio-reactive glow.
 * - Song paused / Cancel: Transitions back smoothly without disappearing.
 * - Dynamic Artwork Palette: Live theme inheritance from the currently playing album art.
 * - X Button: Only dismisses the visual speech HUD without stopping Orbit or cancelling wake word.
 */
@Composable
fun OrbitOverlay(
    state: OrbitState,
    isPlaying: Boolean,
    audioLevel: Float,
    musicAudioData: OrbitMusicAudioData?,
    accentColor: Color,
    onOrbitClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasSong: Boolean = false,
    isPlayerExpanded: Boolean = false,
    message: String = "",
    actionIndicator: String? = null,
    isVisualUiVisible: Boolean = false,
    onDismissVisual: (() -> Unit)? = null
) {
    // Draggable offset so the user can freely reposition Orbit anywhere on screen if desired
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val targetBottomPadding = when {
        isPlayerExpanded -> 120.dp
        hasSong || isPlaying -> 156.dp
        else -> 96.dp
    }
    val animatedBottomPadding by animateDpAsState(
        targetValue = targetBottomPadding,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "orbitBottomPadding"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = animatedBottomPadding)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) },
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ambient spoken message bubble (visible during active voice session)
            AnimatedVisibility(
                visible = isVisualUiVisible && state.isVoiceActive && message.isNotBlank(),
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Surface(
                    color = Color(0xFF14171F).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!actionIndicator.isNullOrBlank()) {
                            Text(
                                text = actionIndicator,
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = message,
                            style = SonzaTypography.BodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = SonzaOnBackground,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Requirement 21: X button only dismisses the visual HUD
                        if (onDismissVisual != null) {
                            IconButton(
                                onClick = onDismissVisual,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide visual HUD",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Draggable container for OrbitAvatar
            Box(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
            ) {
                // THE EXACTLY ONE PERSISTENT ORBIT AVATAR
                OrbitAvatar(
                    state = state,
                    size = OrbitAvatarSize.Floating,
                    isPlaying = isPlaying,
                    audioLevel = audioLevel,
                    musicAudioData = musicAudioData,
                    accentColor = accentColor,
                    onClick = onOrbitClick
                )
            }
        }
    }
}

/**
 * Backward-compatibility wrapper for any legacy invocations.
 */
@Composable
fun OrbitFloatingAssistant(
    isVisible: Boolean,
    state: OrbitState,
    audioLevel: Float,
    onAvatarClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    musicAudioData: OrbitMusicAudioData? = null,
    message: String = "",
    actionIndicator: String? = null,
    onSubmitText: ((String) -> Unit)? = null
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified }
        ?: com.sonza.app.ui.theme.SonzaBrandAccent

    OrbitOverlay(
        state = state,
        isPlaying = isPlaying,
        audioLevel = audioLevel,
        musicAudioData = musicAudioData,
        accentColor = accentColor,
        onOrbitClick = onAvatarClick,
        message = message,
        actionIndicator = actionIndicator,
        isVisualUiVisible = isVisible,
        onDismissVisual = onDismiss,
        modifier = modifier
    )
}
