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
import kotlin.math.roundToInt

/**
 * Size variants for Orbit Avatar.
 */
enum class OrbitAvatarSize(val orbSize: Dp, val haloPadding: Dp) {
    Mini(orbSize = 28.dp, haloPadding = 4.dp),
    Compact(orbSize = 36.dp, haloPadding = 6.dp),
    Medium(orbSize = 64.dp, haloPadding = 12.dp),
    Hero(orbSize = 104.dp, haloPadding = 20.dp)
}

/**
 * Orbit Avatar — The single visual face of Sonza's assistant.
 *
 * Implements:
 * - Dynamic live color palette derived from active song artwork (yellow/orange, blue, purple, red, green, etc.).
 * - Subtle state-based animation:
 *   * IDLE: very subtle breathing/floating pulse.
 *   * WAKE_DETECTED / ACTIVATING: quick soft glow expansion.
 *   * LISTENING: slow animated glow/ring reacting to live mic audio amplitude.
 *   * PROCESSING / THINKING: fluid orbital rotation.
 *   * SPEAKING: gentle responsive pulse synchronized with speech waveform.
 *   * ENDING: soft settling fade.
 */
@Composable
fun OrbitAvatar(
    state: OrbitState,
    modifier: Modifier = Modifier,
    size: OrbitAvatarSize = OrbitAvatarSize.Compact,
    audioLevel: Float = 0f, // 0.0 to 1.0 (live mic amplitude during LISTENING or TTS waveform)
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val totalSize = size.orbSize + (size.haloPadding * 2)

    // Breathing & idle floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "orbitMotion")

    val idleFloatY by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleFloat"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
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
        OrbitState.IDLE -> baseAccent.copy(alpha = 0.70f)
        OrbitState.WAKE_DETECTED, OrbitState.ACTIVATING -> Color.White.copy(alpha = 0.95f)
        OrbitState.LISTENING -> baseAccent
        OrbitState.PROCESSING, OrbitState.THINKING, OrbitState.WORKING -> baseAccent
        OrbitState.SPEAKING -> baseAccent
        OrbitState.ENDING -> baseAccent.copy(alpha = 0.40f)
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
        else -> lerp(baseAccent, Color.Black, 0.18f)
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

    // State scale transitions
    val currentScale = when (state) {
        OrbitState.WAKE_DETECTED, OrbitState.ACTIVATING -> 1.10f
        OrbitState.LISTENING -> 1.06f + (audioLevel * 0.12f)
        OrbitState.SPEAKING -> 1.04f + (audioLevel * 0.08f)
        OrbitState.SUCCESS -> 1.12f
        OrbitState.ENDING -> 0.96f
        else -> breathingScale
    }

    Box(
        modifier = modifier
            .size(totalSize)
            .offset { IntOffset(0, if (state == OrbitState.IDLE) idleFloatY.roundToInt() else 0) }
            .scale(currentScale)
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
            val orbRadius = (this.size.width - (size.haloPadding.toPx() * 2f)) / 2f

            // 1. Outer Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (state.isListening) 0.58f else 0.32f),
                        glowColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbRadius + size.haloPadding.toPx()
                ),
                radius = orbRadius + size.haloPadding.toPx(),
                center = center
            )

            // 2. State-specific outer effects (Audio Waveform rings, Orbital particles, Ripple waves)
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
                    // Primary pulsing audio ring
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
                        // Particle dot
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
                    // Progress arc spinning
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
                    // Dynamic speech soundwave ripples
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

            // 3. Main Orb Body (Smooth 3D-lit sphere with artwork dynamic tint)
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

            // 4. Expressive Digital Pill Eyes
            val eyeWidth = orbRadius * 0.18f
            val baseEyeHeight = orbRadius * 0.40f
            val eyeSpacing = orbRadius * 0.24f

            // Eye positions
            val leftEyeCenter = Offset(center.x - eyeSpacing, center.y - orbRadius * 0.05f)
            val rightEyeCenter = Offset(center.x + eyeSpacing, center.y - orbRadius * 0.05f)
            val eyeColor = Color(0xFF13151D)

            when (state) {
                OrbitState.SUCCESS -> {
                    // Joyful squinting arcs (smiling eyes ^_^)
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
                    // Gentle curved / empathetic eyes
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
                    // Glance slightly upwards
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
                    // Alert wide open eyes
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
                    // Wide, alert eyes with audio reactivity
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
                    // Alert eyes
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
                    // IDLE & ENDING: Natural blinking vertical pill eyes
                    val eyeH = (baseEyeHeight * blinkProgress).coerceAtLeast(eyeWidth * 0.3f)
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
        }
    }
}
