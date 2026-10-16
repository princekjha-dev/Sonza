package com.sonza.app.ai.orbit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.theme.SonzaBrandAccent
import kotlin.math.*

private inline fun lerpFloat(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

/**
 * Size variants for Orbit Avatar.
 * [haloPadding] provides generous clearance for outer glow, headphones,
 * and music-reactive side equalizer waveforms.
 */
enum class OrbitAvatarSize(val orbSize: Dp, val haloPadding: Dp) {
    Mini(orbSize = 28.dp, haloPadding = 8.dp),
    Compact(orbSize = 36.dp, haloPadding = 12.dp),
    Floating(orbSize = 54.dp, haloPadding = 16.dp),
    Medium(orbSize = 64.dp, haloPadding = 20.dp),
    Hero(orbSize = 104.dp, haloPadding = 34.dp)
}

/**
 * Orbit Avatar — The animated assistant character of Sonza.
 *
 * CORE SPECIFICATION:
 * - MIC ≠ ORBIT: The microphone button triggers Orbit; Orbit itself ALWAYS remains the character.
 * - IDLE STATE (No song playing): Gently moves along a small, smooth circular orbital floating path.
 * - MUSIC PLAYING: Smoothly puts on large headphones around the avatar and enters ORBIT MUSIC MODE.
 * - MUSIC REACTIVE: Reacts in real-time to music amplitude, RMS, frequency bands (bass pulse, mid grooving,
 *   high glow), and beat onset bounce. Headphones remain physically attached to Orbit.
 * - MUSIC-REACTIVE WAVEFORM: Subtle 8-bar equalizer around Orbit's sides dancing to the music.
 * - DYNAMIC COLORS: Derives live accent palette from the currently playing album artwork (yellow, blue, purple, red, etc.).
 * - MUSIC PAUSED: Headphones and waveform smoothly disappear; returns to gentle circular floating motion.
 * - VOICE INTERACTION: Layers on top (HEY ORBIT → LISTENING → PROCESSING → SPEAKING) and returns to
 *   Music Orbit if music was playing, or Idle Orbit otherwise.
 */
@Composable
fun OrbitAvatar(
    state: OrbitState = OrbitState.IDLE,
    modifier: Modifier = Modifier,
    size: OrbitAvatarSize = OrbitAvatarSize.Compact,
    isPlaying: Boolean = false,
    audioLevel: Float = 0f, // 0.0 to 1.0 (live mic amplitude during LISTENING or TTS waveform)
    musicAudioData: OrbitMusicAudioData? = null,
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val totalSize = size.orbSize + (size.haloPadding * 2)

    // Fallback dynamic music synthesis if external analyzer not supplied
    val fallbackMusicData = rememberFallbackMusicAudioData(isPlaying = isPlaying || state.isMusicMode)
    val effectiveMusicData = musicAudioData ?: fallbackMusicData

    val isMusicMode = isPlaying || state.isMusicMode

    // Smooth transition into/out of Music Mode headphones & waveforms (0f -> idle, 1f -> music)
    val targetHpProgress = if (isMusicMode) 1f else 0f
    val headphonesProgress by animateFloatAsState(
        targetValue = targetHpProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "headphonesProgress"
    )

    // Infinite transitions for idle floating and breathing
    val infiniteTransition = rememberInfiniteTransition(label = "orbitMotion")

    // 1. Idle Circular Floating / Orbiting Motion (Section 2)
    // Small circular path: x = R * cos(t), y = R * sin(t). Smooth, continuous, calm.
    val idleOrbitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idleOrbitPhase"
    )

    // Grooving rhythmic phase for music mode
    val musicGroovePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "musicGroovePhase"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    val orbitalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalRotation"
    )

    // Natural blinking cycle: short blink every ~3.5 seconds
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                1f at 0
                1f at 3300
                0.12f at 3400 // blink shut
                1f at 3500   // open again
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blinkProgress"
    )

    // Dynamic Live Color Extraction from active player artwork
    val dynamicColors = LocalSonzaDynamicColors.current
    val baseAccent = accentColor?.takeIf { it != Color.Unspecified }
        ?: dynamicColors.accent.takeIf { it != Color.Unspecified }
        ?: SonzaBrandAccent

    // State-reactive Glow Color derived from active music artwork accent
    val targetGlowColor = when (state) {
        OrbitState.IDLE, OrbitState.ORBITING -> baseAccent.copy(alpha = 0.65f)
        OrbitState.MUSIC_PLAYING, OrbitState.HEADPHONES_ON, OrbitState.MUSIC_REACTIVE -> baseAccent
        OrbitState.MUSIC_PAUSED, OrbitState.HEADPHONES_OFF -> baseAccent.copy(alpha = 0.50f)
        OrbitState.WAKE_DETECTED, OrbitState.ACTIVATING -> Color.White.copy(alpha = 0.95f)
        OrbitState.LISTENING, OrbitState.TRANSCRIBING, OrbitState.WAITING_FOR_FOLLOWUP -> baseAccent
        OrbitState.PROCESSING, OrbitState.THINKING, OrbitState.WORKING, OrbitState.EXECUTING -> baseAccent
        OrbitState.SPEAKING -> baseAccent
        OrbitState.ENDING -> baseAccent.copy(alpha = 0.35f)
        OrbitState.SUCCESS -> Color(0xFF00E676)
        OrbitState.ERROR -> Color(0xFFFF5252)
    }

    // 3D Sphere base gradients derived live from current artwork accent
    val targetGradientStart = when (state) {
        OrbitState.ERROR -> Color(0xFFFFEBEE)
        OrbitState.SUCCESS -> Color(0xFFE8F5E9)
        OrbitState.WAKE_DETECTED, OrbitState.ACTIVATING -> Color.White
        else -> lerp(Color.White, baseAccent, 0.28f)
    }
    val targetGradientEnd = when (state) {
        OrbitState.ERROR -> Color(0xFFE57373)
        OrbitState.SUCCESS -> Color(0xFF81C784)
        else -> lerp(baseAccent, Color.Black, 0.20f)
    }

    val glowColor by animateColorAsState(
        targetValue = targetGlowColor,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "orbitGlowColor"
    )

    val orbBaseGradientStart by animateColorAsState(
        targetValue = targetGradientStart,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "orbGradientStart"
    )

    val orbBaseGradientEnd by animateColorAsState(
        targetValue = targetGradientEnd,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "orbGradientEnd"
    )

    // Calculate motion offsets:
    // When idle: orbital circular path (alive, futuristic, continuous floating)
    // When music playing: audio-reactive grooving & beat bounce
    val density = androidx.compose.ui.platform.LocalDensity.current
    val orbRadiusPx = (size.orbSize.value * 1.5f) // Reference pixel scaling
    val circularRadius = with(density) { 8.dp.toPx() } // Smooth gentle circular floating excursion
    val idleX = circularRadius * cos(idleOrbitPhase)
    val idleY = (circularRadius * 0.78f) * sin(idleOrbitPhase)

    // Music mode audio-reactive movement (bass scale, mid grooving, beat bounce)
    val bass = effectiveMusicData.bass
    val mid = effectiveMusicData.mid
    val beat = effectiveMusicData.beatPulse

    val musicGrooveX = sin(musicGroovePhase) * (mid * orbRadiusPx * 0.08f)
    val musicGrooveY = -abs(cos(musicGroovePhase * 2f)) * (bass * orbRadiusPx * 0.06f + beat * orbRadiusPx * 0.10f)
    val musicRotation = sin(musicGroovePhase) * (mid * 3.5f)

    // Cross-fade idle circular orbit with music grooving
    val combinedOffsetX = lerpFloat(idleX, musicGrooveX, headphonesProgress)
    val combinedOffsetY = lerpFloat(idleY, musicGrooveY, headphonesProgress)
    val combinedRotation = lerpFloat(0f, musicRotation, headphonesProgress)

    // Base state scale transitions
    val voiceScale = when (state) {
        OrbitState.WAKE_DETECTED, OrbitState.ACTIVATING -> 1.10f
        OrbitState.LISTENING -> 1.06f + (audioLevel * 0.12f)
        OrbitState.SPEAKING -> 1.04f + (audioLevel * 0.08f)
        OrbitState.SUCCESS -> 1.12f
        OrbitState.ENDING -> 0.96f
        else -> breathingScale
    }
    val musicScale = 1.0f + (bass * 0.045f) + (beat * 0.055f)
    val finalScale = if (state.isVoiceActive) voiceScale else lerpFloat(voiceScale, musicScale, headphonesProgress)

    Box(
        modifier = modifier
            .size(totalSize)
            .offset {
                val x = if (combinedOffsetX.isNaN() || combinedOffsetX.isInfinite()) 0 else combinedOffsetX.roundToInt()
                val y = if (combinedOffsetY.isNaN() || combinedOffsetY.isInfinite()) 0 else combinedOffsetY.roundToInt()
                IntOffset(x, y)
            }
            .scale(finalScale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(totalSize)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val orbRadius = size.orbSize.toPx() / 2f

            rotate(degrees = combinedRotation, pivot = center) {
                // 1. Outer Ambient Glow (Soft lighting reacting to music high frequencies)
                val glowRadius = orbRadius + size.haloPadding.toPx() * (0.85f + effectiveMusicData.high * 0.35f)
                val glowAlpha = if (state.isListening) 0.58f else (0.28f + effectiveMusicData.high * 0.22f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = glowAlpha),
                            glowColor.copy(alpha = glowAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )

                // 2. Music-Reactive Waveform Equalizer (Section 7)
                // Subtle 8-bar equalizer around Orbit's sides dancing to audio frequencies
                if (headphonesProgress > 0.005f) {
                    val maxBarH = orbRadius * 0.58f
                    val barWidth = orbRadius * 0.065f
                    val barSpacing = orbRadius * 0.085f
                    val leftBaseX = center.x - (orbRadius * 1.16f)
                    val rightBaseX = center.x + (orbRadius * 1.16f)

                    val bars = effectiveMusicData.waveformBars
                    // Left 4 bars (0=high outer, 1=mid-high, 2=mid, 3=bass inner)
                    for (i in 0..3) {
                        val barEnergy = bars.getOrElse(i) { 0.2f }
                        val barH = (orbRadius * 0.12f + barEnergy * maxBarH) * headphonesProgress
                        val barX = leftBaseX - (3 - i) * (barWidth + barSpacing)
                        val barY = center.y - barH / 2f
                        val barAlpha = headphonesProgress * (0.35f + barEnergy * 0.65f)

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = barAlpha * 0.95f),
                                    glowColor.copy(alpha = barAlpha * 0.85f),
                                    glowColor.copy(alpha = barAlpha * 0.35f)
                                ),
                                startY = barY,
                                endY = barY + barH
                            ),
                            topLeft = Offset(barX, barY),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }

                    // Right 4 bars (4=bass inner, 5=mid, 6=mid-high, 7=high outer)
                    for (i in 4..7) {
                        val barEnergy = bars.getOrElse(i) { 0.2f }
                        val barH = (orbRadius * 0.12f + barEnergy * maxBarH) * headphonesProgress
                        val barX = rightBaseX + (i - 4) * (barWidth + barSpacing)
                        val barY = center.y - barH / 2f
                        val barAlpha = headphonesProgress * (0.35f + barEnergy * 0.65f)

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = barAlpha * 0.95f),
                                    glowColor.copy(alpha = barAlpha * 0.85f),
                                    glowColor.copy(alpha = barAlpha * 0.35f)
                                ),
                                startY = barY,
                                endY = barY + barH
                            ),
                            topLeft = Offset(barX, barY),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }

                // 3. Voice State-specific outer effects (Audio Waveform rings, Orbital particles)
                when (state) {
                    OrbitState.WAKE_DETECTED, OrbitState.ACTIVATING -> {
                        // Expanding wake bloom halo
                        drawCircle(
                            color = Color.White.copy(alpha = 0.65f),
                            radius = orbRadius + 6.dp.toPx(),
                            center = center,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    OrbitState.LISTENING -> {
                        // Primary pulsing voice ring
                        val pulseRadius = orbRadius + 4.dp.toPx() + (audioLevel * 10.dp.toPx())
                        drawCircle(
                            color = glowColor.copy(alpha = 0.50f + (audioLevel * 0.45f)),
                            radius = pulseRadius,
                            center = center,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Secondary outer echo soundwave ring
                        val secondaryRadius = orbRadius + 10.dp.toPx() + (audioLevel * 16.dp.toPx())
                        drawCircle(
                            color = glowColor.copy(alpha = (audioLevel * 0.35f)),
                            radius = secondaryRadius,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    OrbitState.PROCESSING, OrbitState.THINKING -> {
                        // Orbital particle ring
                        rotate(degrees = orbitalRotation, pivot = center) {
                            drawCircle(
                                color = glowColor.copy(alpha = 0.28f),
                                radius = orbRadius + 6.dp.toPx(),
                                center = center,
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                            val pOffset = Offset(
                                x = center.x + (orbRadius + 6.dp.toPx()),
                                y = center.y
                            )
                            drawCircle(
                                color = glowColor,
                                radius = 3.5.dp.toPx(),
                                center = pOffset
                            )
                        }
                        // Counter-rotating secondary arc
                        rotate(degrees = -orbitalRotation * 0.8f, pivot = center) {
                            drawArc(
                                color = glowColor.copy(alpha = 0.5f),
                                startAngle = 180f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(center.x - orbRadius - 10.dp.toPx(), center.y - orbRadius - 10.dp.toPx()),
                                size = Size((orbRadius + 10.dp.toPx()) * 2, (orbRadius + 10.dp.toPx()) * 2),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                    OrbitState.WORKING -> {
                        rotate(degrees = orbitalRotation * 1.5f, pivot = center) {
                            drawArc(
                                color = glowColor,
                                startAngle = 0f,
                                sweepAngle = 100f,
                                useCenter = false,
                                topLeft = Offset(center.x - orbRadius - 4.dp.toPx(), center.y - orbRadius - 4.dp.toPx()),
                                size = Size((orbRadius + 4.dp.toPx()) * 2, (orbRadius + 4.dp.toPx()) * 2),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                    OrbitState.SPEAKING -> {
                        // Dynamic speech ripples
                        val speakRadius = orbRadius + 4.dp.toPx() + (audioLevel * 8.dp.toPx())
                        drawCircle(
                            color = glowColor.copy(alpha = 0.40f + (audioLevel * 0.45f)),
                            radius = speakRadius,
                            center = center,
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        val outerSpeakRadius = orbRadius + 11.dp.toPx() + (audioLevel * 14.dp.toPx())
                        drawCircle(
                            color = glowColor.copy(alpha = (audioLevel * 0.28f)),
                            radius = outerSpeakRadius,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    else -> Unit
                }

                // 4. Main Orb Body (Smooth 3D-lit sphere with artwork dynamic tint)
                val orbGradient = Brush.radialGradient(
                    colors = listOf(
                        orbBaseGradientStart,
                        orbBaseGradientEnd
                    ),
                    center = Offset(center.x - orbRadius * 0.35f, center.y - orbRadius * 0.35f),
                    radius = orbRadius * 1.35f
                )

                drawCircle(
                    brush = orbGradient,
                    radius = orbRadius,
                    center = center
                )

                // Specular top highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = orbRadius * 0.28f,
                    center = Offset(center.x - orbRadius * 0.35f, center.y - orbRadius * 0.4f)
                )

                // 5. Expressive Digital Pill Eyes
                val eyeWidth = orbRadius * 0.18f
                val baseEyeHeight = orbRadius * 0.40f
                val eyeSpacing = orbRadius * 0.24f

                val leftEyeCenter = Offset(center.x - eyeSpacing, center.y - orbRadius * 0.05f)
                val rightEyeCenter = Offset(center.x + eyeSpacing, center.y - orbRadius * 0.05f)
                val eyeColor = Color(0xFF13151D)

                when (state) {
                    OrbitState.SUCCESS -> {
                        // Joyful smiling arcs
                        val arcWidth = eyeWidth * 1.8f
                        val arcHeight = baseEyeHeight * 0.65f
                        val stroke = Stroke(width = eyeWidth * 0.8f, cap = StrokeCap.Round)

                        val leftPath = Path().apply {
                            moveTo(leftEyeCenter.x - arcWidth / 2f, leftEyeCenter.y)
                            quadraticTo(leftEyeCenter.x, leftEyeCenter.y - arcHeight, leftEyeCenter.x + arcWidth / 2f, leftEyeCenter.y)
                        }
                        val rightPath = Path().apply {
                            moveTo(rightEyeCenter.x - arcWidth / 2f, rightEyeCenter.y)
                            quadraticTo(rightEyeCenter.x, rightEyeCenter.y - arcHeight, rightEyeCenter.x + arcWidth / 2f, rightEyeCenter.y)
                        }
                        drawPath(leftPath, color = eyeColor, style = stroke)
                        drawPath(rightPath, color = eyeColor, style = stroke)
                    }
                    OrbitState.ERROR -> {
                        val eyeH = baseEyeHeight * 0.5f
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeH / 2f + 2.dp.toPx()),
                            size = Size(eyeWidth, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeH / 2f + 2.dp.toPx()),
                            size = Size(eyeWidth, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                    }
                    OrbitState.PROCESSING, OrbitState.THINKING -> {
                        val eyeH = baseEyeHeight * 0.8f
                        val lookUpY = -3.dp.toPx()
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeH / 2f + lookUpY),
                            size = Size(eyeWidth, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeH / 2f + lookUpY),
                            size = Size(eyeWidth, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                    }
                    OrbitState.WAKE_DETECTED, OrbitState.ACTIVATING -> {
                        val eyeH = baseEyeHeight * 1.1f
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeH / 2f),
                            size = Size(eyeWidth * 1.1f, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeH / 2f),
                            size = Size(eyeWidth * 1.1f, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                    }
                    OrbitState.LISTENING -> {
                        val eyeH = (baseEyeHeight * 1.15f + (audioLevel * baseEyeHeight * 0.25f)).coerceAtMost(orbRadius * 0.65f)
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeH / 2f),
                            size = Size(eyeWidth * 1.15f, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeH / 2f),
                            size = Size(eyeWidth * 1.15f, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                    }
                    OrbitState.SPEAKING -> {
                        val eyeH = baseEyeHeight * 0.95f
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeH / 2f),
                            size = Size(eyeWidth, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeH / 2f),
                            size = Size(eyeWidth, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )

                        // Animated mouth reacting to Orbit's speech level
                        val mouthY = center.y + orbRadius * 0.32f
                        val mouthWidth = orbRadius * (0.28f + (audioLevel * 0.16f))
                        val mouthHeight = (orbRadius * 0.08f + (audioLevel * orbRadius * 0.18f)).coerceAtLeast(2.dp.toPx())
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(center.x - mouthWidth / 2f, mouthY - mouthHeight / 2f),
                            size = Size(mouthWidth, mouthHeight),
                            cornerRadius = CornerRadius(mouthHeight / 2f, mouthHeight / 2f)
                        )
                    }
                    else -> {
                        // IDLE / ORBITING / MUSIC MODE: Natural blinking pill eyes
                        val beatSquint = if (isMusicMode) (beat * 0.15f) else 0f
                        val eyeH = ((baseEyeHeight * (blinkProgress - beatSquint))).coerceAtLeast(eyeWidth * 0.28f)
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeH / 2f),
                            size = Size(eyeWidth, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                        drawRoundRect(
                            color = eyeColor,
                            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeH / 2f),
                            size = Size(eyeWidth, eyeH),
                            cornerRadius = CornerRadius(eyeWidth / 2f, eyeWidth / 2f)
                        )
                    }
                }

                // 6. Headphones in Orbit Music Mode (Sections 3, 4, 6)
                // Headband arched over top + plush earcups on sides, scaling & sliding smoothly into place
                if (headphonesProgress > 0.002f) {
                    val hpAlpha = headphonesProgress.coerceIn(0f, 1f)
                    val hpScale = lerpFloat(0.72f, 1.0f, headphonesProgress)
                    val hpSlideY = lerpFloat(-orbRadius * 0.35f, 0f, headphonesProgress)

                    // Draw Headband Arch
                    val headbandRadius = orbRadius * 1.06f * hpScale
                    val headbandStrokeW = orbRadius * 0.15f * hpScale
                    val headbandCenterY = center.y - orbRadius * 0.05f + hpSlideY

                    // Outer chassis arch
                    drawArc(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lerp(Color(0xFF282C3A), baseAccent, 0.45f).copy(alpha = hpAlpha),
                                Color(0xFF161820).copy(alpha = hpAlpha)
                            ),
                            startY = headbandCenterY - headbandRadius,
                            endY = headbandCenterY
                        ),
                        startAngle = 196f,
                        sweepAngle = 148f,
                        useCenter = false,
                        topLeft = Offset(center.x - headbandRadius, headbandCenterY - headbandRadius),
                        size = Size(headbandRadius * 2f, headbandRadius * 2f),
                        style = Stroke(width = headbandStrokeW, cap = StrokeCap.Round)
                    )

                    // Headband accent crest stripe (dynamic glowing trim along the top arch)
                    drawArc(
                        color = baseAccent.copy(alpha = hpAlpha * 0.85f),
                        startAngle = 215f,
                        sweepAngle = 110f,
                        useCenter = false,
                        topLeft = Offset(center.x - headbandRadius, headbandCenterY - headbandRadius),
                        size = Size(headbandRadius * 2f, headbandRadius * 2f),
                        style = Stroke(width = headbandStrokeW * 0.35f, cap = StrokeCap.Round)
                    )

                    // Headband inner padding cushion
                    val padRadius = headbandRadius - headbandStrokeW * 0.35f
                    if (padRadius > 0f) {
                        drawArc(
                            color = Color(0xFF12141A).copy(alpha = hpAlpha * 0.90f),
                            startAngle = 202f,
                            sweepAngle = 136f,
                            useCenter = false,
                            topLeft = Offset(center.x - padRadius, headbandCenterY - padRadius),
                            size = Size(padRadius * 2f, padRadius * 2f),
                            style = Stroke(width = headbandStrokeW * 0.45f, cap = StrokeCap.Round)
                        )
                    }

                    // Draw Ear Cups (Left & Right)
                    val cupW = orbRadius * 0.38f * hpScale
                    val cupH = orbRadius * 0.82f * hpScale
                    val cupCorner = CornerRadius(cupW * 0.46f, cupW * 0.46f)
                    val cupDistX = orbRadius * 0.96f

                    // Left Ear Cup
                    val leftCupCenter = Offset(center.x - cupDistX, center.y + hpSlideY)
                    val leftCupTopLeft = Offset(leftCupCenter.x - cupW / 2f, leftCupCenter.y - cupH / 2f)

                    // 1. Inner soft cushion (resting against orb)
                    drawRoundRect(
                        color = Color(0xFF111319).copy(alpha = hpAlpha),
                        topLeft = Offset(leftCupTopLeft.x + cupW * 0.15f, leftCupTopLeft.y + cupH * 0.05f),
                        size = Size(cupW * 0.85f, cupH * 0.90f),
                        cornerRadius = cupCorner
                    )

                    // 2. Main outer cup shell
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                lerp(Color(0xFF2C303E), baseAccent, 0.40f).copy(alpha = hpAlpha),
                                Color(0xFF171922).copy(alpha = hpAlpha)
                            ),
                            startX = leftCupTopLeft.x,
                            endX = leftCupTopLeft.x + cupW
                        ),
                        topLeft = leftCupTopLeft,
                        size = Size(cupW, cupH),
                        cornerRadius = cupCorner
                    )

                    // 3. Outer acoustic glowing circle
                    val leftAcousticCenter = Offset(leftCupTopLeft.x + cupW * 0.38f, leftCupCenter.y)
                    drawCircle(
                        color = baseAccent.copy(alpha = hpAlpha * 0.85f),
                        radius = cupW * 0.32f,
                        center = leftAcousticCenter
                    )
                    drawCircle(
                        color = Color(0xFF13151D).copy(alpha = hpAlpha),
                        radius = cupW * 0.20f,
                        center = leftAcousticCenter
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = hpAlpha * 0.75f),
                        radius = cupW * 0.08f,
                        center = leftAcousticCenter
                    )

                    // Right Ear Cup (Mirrored)
                    val rightCupCenter = Offset(center.x + cupDistX, center.y + hpSlideY)
                    val rightCupTopLeft = Offset(rightCupCenter.x - cupW / 2f, rightCupCenter.y - cupH / 2f)

                    // 1. Inner soft cushion
                    drawRoundRect(
                        color = Color(0xFF111319).copy(alpha = hpAlpha),
                        topLeft = Offset(rightCupTopLeft.x, rightCupTopLeft.y + cupH * 0.05f),
                        size = Size(cupW * 0.85f, cupH * 0.90f),
                        cornerRadius = cupCorner
                    )

                    // 2. Main outer cup shell
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF171922).copy(alpha = hpAlpha),
                                lerp(Color(0xFF2C303E), baseAccent, 0.40f).copy(alpha = hpAlpha)
                            ),
                            startX = rightCupTopLeft.x,
                            endX = rightCupTopLeft.x + cupW
                        ),
                        topLeft = rightCupTopLeft,
                        size = Size(cupW, cupH),
                        cornerRadius = cupCorner
                    )

                    // 3. Outer acoustic glowing circle
                    val rightAcousticCenter = Offset(rightCupTopLeft.x + cupW * 0.62f, rightCupCenter.y)
                    drawCircle(
                        color = baseAccent.copy(alpha = hpAlpha * 0.85f),
                        radius = cupW * 0.32f,
                        center = rightAcousticCenter
                    )
                    drawCircle(
                        color = Color(0xFF13151D).copy(alpha = hpAlpha),
                        radius = cupW * 0.20f,
                        center = rightAcousticCenter
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = hpAlpha * 0.75f),
                        radius = cupW * 0.08f,
                        center = rightAcousticCenter
                    )
                }
            }
        }
    }
}

/**
 * Synthetic fallback music analyzer that ensures smooth, organic audio responsiveness
 * whenever external analyzer stream is unavailable.
 */
@Composable
private fun rememberFallbackMusicAudioData(isPlaying: Boolean): OrbitMusicAudioData {
    if (!isPlaying) return OrbitMusicAudioData()

    val infiniteTransition = rememberInfiniteTransition(label = "fallbackAudio")

    val bassProgress by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fallbackBass"
    )

    val midProgress by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(620, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fallbackMid"
    )

    val highProgress by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fallbackHigh"
    )

    val beatPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fallbackBeat"
    )

    val bars = remember(bassProgress, midProgress, highProgress, beatPulse) {
        floatArrayOf(
            highProgress * 0.85f,
            midProgress * 0.90f,
            midProgress * 0.75f + bassProgress * 0.25f,
            bassProgress * 1.05f + beatPulse * 0.20f,
            bassProgress * 1.02f + beatPulse * 0.20f,
            midProgress * 0.80f + bassProgress * 0.20f,
            midProgress * 0.88f,
            highProgress * 0.82f
        )
    }

    return OrbitMusicAudioData(
        amplitude = (bassProgress + midProgress + highProgress) / 3f,
        bass = bassProgress,
        mid = midProgress,
        high = highProgress,
        beatPulse = beatPulse,
        waveformBars = bars
    )
}
