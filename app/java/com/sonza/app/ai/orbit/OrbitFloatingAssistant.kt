package com.sonza.app.ai.orbit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ai.orbit.theme.OrbitColorEngine
import com.sonza.app.ai.orbit.theme.OrbitColorPalette
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.theme.SonzaTypography
import kotlin.math.sin

/**
 * Orbit Overlay — Next-Generation Voice Assistant Interaction Surface.
 *
 * Core UX Principles (Siri-like Assistant Architecture):
 * 1. The assistant interaction is strictly TEMPORARY: appears only during active voice interaction,
 *    and smoothly fades away when done.
 * 2. NEVER creates a giant floating overlay or permanent character covering the Home screen songs,
 *    artwork, Quick Picks, or navigation.
 * 3. The permanent Orbit identity lives in the top-right header of Sonza ([HomeTopHeader]).
 * 4. When voice interaction activates ("Hey Orbit" or top-right tap), this compact, translucent
 *    glass interaction surface floats seamlessly near the bottom above the navigation.
 * 5. Uses dynamic music-reactive colors derived from the currently playing album artwork
 *    ([OrbitColorEngine]) with liquid, smooth color morphing across song changes.
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
    // Only display the temporary interaction surface during an active voice session
    val isSurfaceVisible = isVisualUiVisible && state.isVoiceActive && !isPlayerExpanded

    // Dynamically calculate bottom padding so Orbit floats safely above bottom nav & mini-player
    val targetBottomPadding = when {
        hasSong || isPlaying -> 152.dp
        else -> 88.dp
    }
    val animatedBottomPadding by animateDpAsState(
        targetValue = targetBottomPadding,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "orbit_bottom_padding"
    )

    // Dynamic music-reactive palette derived live from artwork
    val dynamicColors = LocalSonzaDynamicColors.current
    val palette = OrbitColorEngine.rememberAnimatedOrbitPalette(
        currentSong = null,
        dynamicColors = dynamicColors
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = isSurfaceVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(220)),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(180)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = animatedBottomPadding)
        ) {
            OrbitSiriInteractionSurface(
                state = state,
                audioLevel = audioLevel,
                musicAudioData = musicAudioData,
                isPlaying = isPlaying,
                message = message,
                actionIndicator = actionIndicator,
                palette = palette,
                onAvatarClick = onOrbitClick,
                onDismiss = onDismissVisual
            )
        }
    }
}

/**
 * Compact Siri-like Voice Interaction Surface.
 *
 * - Dark translucent glass with luminous border.
 * - Dynamic waveform / thinking dots responding live to audio & speech.
 * - Embedded compact Orbit avatar.
 * - Humanized natural dialogue.
 */
@Composable
fun OrbitSiriInteractionSurface(
    state: OrbitState,
    audioLevel: Float,
    musicAudioData: OrbitMusicAudioData?,
    isPlaying: Boolean,
    message: String,
    actionIndicator: String?,
    palette: OrbitColorPalette,
    onAvatarClick: () -> Unit,
    onDismiss: (() -> Unit)?
) {
    val cleanMessage = remember(message, state) {
        when {
            state.isListening && (message.isBlank() || message == "Tap Orbit to speak…") -> "Listening…"
            state == OrbitState.THINKING || state == OrbitState.PROCESSING || state == OrbitState.EXECUTING -> "Thinking…"
            else -> OrbitDialogueSanitizer.sanitize(message)
        }
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = palette.surfaceBackground,
        border = BorderStroke(1.dp, palette.borderStroke),
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
    ) {
        Row(
            modifier = Modifier
                .background(palette.surfaceGradient)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Compact Orbit Avatar
            OrbitAvatar(
                state = state,
                size = OrbitAvatarSize.Compact,
                isPlaying = isPlaying,
                audioLevel = audioLevel,
                musicAudioData = musicAudioData,
                accentColor = palette.accent,
                onClick = onAvatarClick
            )

            Spacer(Modifier.width(12.dp))

            // 2. Center Column: Speech text + dynamic waveform/dots
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!actionIndicator.isNullOrBlank()) {
                        Text(
                            text = actionIndicator,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = cleanMessage,
                        style = SonzaTypography.BodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        ),
                        color = palette.onSurfaceText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(5.dp))

                // Waveform or Thinking Dots
                when {
                    state == OrbitState.THINKING || state == OrbitState.PROCESSING || state == OrbitState.EXECUTING -> {
                        OrbitThinkingDots(palette = palette)
                    }
                    state.isListening || state == OrbitState.SPEAKING || isPlaying -> {
                        OrbitCompactWaveform(
                            audioLevel = audioLevel,
                            isSpeaking = state == OrbitState.SPEAKING,
                            palette = palette
                        )
                    }
                    else -> {
                        OrbitCompactWaveform(
                            audioLevel = 0.15f,
                            isSpeaking = false,
                            palette = palette
                        )
                    }
                }
            }

            // 3. Subtle close button
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Hide visual HUD",
                        tint = palette.onSurfaceSubtext,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Animated dynamic waveform reacting to mic amplitude, TTS speech, and music mood.
 */
@Composable
fun OrbitCompactWaveform(
    audioLevel: Float,
    isSpeaking: Boolean,
    palette: OrbitColorPalette,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_motion"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val totalBars = 9
        val barWidth = 3.dp.toPx()
        val spacing = 3.dp.toPx()
        val startX = 0f

        val effectiveAmp = audioLevel.coerceIn(0.12f, 1f)

        for (i in 0 until totalBars) {
            val waveOffset = sin(phase + (i * 0.7f))
            val dynamicHeightFraction = (0.25f + (0.75f * effectiveAmp * (0.6f + 0.4f * waveOffset))).coerceIn(0.15f, 1f)
            val barHeight = size.height * dynamicHeightFraction

            val x = startX + i * (barWidth + spacing)
            val y = (size.height - barHeight) / 2f

            drawRoundRect(
                brush = palette.waveformGradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

/**
 * Animated 3-dot thinking indicator with dynamic song colors.
 */
@Composable
fun OrbitThinkingDots(
    palette: OrbitColorPalette,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_transition")

    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(palette.primary.copy(alpha = dot1Alpha))
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(palette.secondary.copy(alpha = dot2Alpha))
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(palette.accent.copy(alpha = dot3Alpha))
        )
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
