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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.components.sonza.SonzaDesignTokens
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
 * Orbit AI Avatar — The visual face of Sonza's assistant inspired by fluid digital characters.
 * Renders an animated glowing orb with expressive pill eyes, orbital rings, and dynamic reactive states.
 */
@Composable
fun OrbitAvatar(
    state: OrbitState,
    modifier: Modifier = Modifier,
    size: OrbitAvatarSize = OrbitAvatarSize.Compact,
    audioLevel: Float = 0f, // 0.0 to 1.0 (live mic amplitude during LISTENING)
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
        initialValue = 0.97f,
        targetValue = 1.03f,
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

    // Dynamic state colors
    val glowColor by animateColorAsState(
        targetValue = when (state) {
            OrbitState.IDLE -> SonzaDesignTokens.Colors.OrbitIdle
            OrbitState.LISTENING -> SonzaDesignTokens.Colors.OrbitListening
            OrbitState.THINKING -> SonzaDesignTokens.Colors.OrbitThinking
            OrbitState.WORKING -> SonzaDesignTokens.Colors.OrbitWorking
            OrbitState.SPEAKING -> SonzaDesignTokens.Colors.OrbitSpeaking
            OrbitState.SUCCESS -> SonzaDesignTokens.Colors.OrbitSuccess
            OrbitState.ERROR -> SonzaDesignTokens.Colors.OrbitError
        },
        animationSpec = tween(350),
        label = "orbitGlowColor"
    )

    val orbBaseGradientStart by animateColorAsState(
        targetValue = when (state) {
            OrbitState.IDLE -> Color(0xFFE3EAFE)
            OrbitState.LISTENING -> Color(0xFFE0F7FA)
            OrbitState.THINKING -> Color(0xFFEDE7F6)
            OrbitState.WORKING -> Color(0xFFFFF3E0)
            OrbitState.SPEAKING -> Color(0xFFE8F5E9)
            OrbitState.SUCCESS -> Color(0xFFE8F5E9)
            OrbitState.ERROR -> Color(0xFFFFEBEE)
        },
        animationSpec = tween(300),
        label = "orbGradientStart"
    )

    val orbBaseGradientEnd by animateColorAsState(
        targetValue = when (state) {
            OrbitState.IDLE -> Color(0xFF7FA2FF)
            OrbitState.LISTENING -> Color(0xFF4DD0E1)
            OrbitState.THINKING -> Color(0xFFBA68C8)
            OrbitState.WORKING -> Color(0xFFFFB74D)
            OrbitState.SPEAKING -> Color(0xFF81C784)
            OrbitState.SUCCESS -> Color(0xFF81C784)
            OrbitState.ERROR -> Color(0xFFE57373)
        },
        animationSpec = tween(300),
        label = "orbGradientEnd"
    )

    val currentScale = when (state) {
        OrbitState.LISTENING -> 1.08f + (audioLevel * 0.12f)
        OrbitState.SUCCESS -> 1.12f
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
            val center = Offset(size.width / 2f, size.height / 2f)
            val orbRadius = (this.size.width - (size.haloPadding.toPx() * 2f)) / 2f

            // 1. Outer Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (state == OrbitState.LISTENING) 0.55f else 0.32f),
                        glowColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbRadius + size.haloPadding.toPx()
                ),
                radius = orbRadius + size.haloPadding.toPx(),
                center = center
            )

            // 2. State-specific outer effects (Audio Waveform ring or Orbital particles)
            when (state) {
                OrbitState.LISTENING -> {
                    // Pulsing audio ring
                    val pulseRadius = orbRadius + 4.dp.toPx() + (audioLevel * 8.dp.toPx())
                    drawCircle(
                        color = glowColor.copy(alpha = 0.45f + (audioLevel * 0.45f)),
                        radius = pulseRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                OrbitState.THINKING -> {
                    // Orbital particle ring
                    rotate(degrees = orbitalRotation, pivot = center) {
                        drawCircle(
                            color = glowColor.copy(alpha = 0.25f),
                            radius = orbRadius + 5.dp.toPx(),
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        // Particle dot
                        val pOffset = Offset(
                            x = center.x + (orbRadius + 5.dp.toPx()),
                            y = center.y
                        )
                        drawCircle(
                            color = glowColor,
                            radius = 3.dp.toPx(),
                            center = pOffset
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
                else -> Unit
            }

            // 3. Main Orb Body (Smooth 3D-lit sphere)
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
                OrbitState.THINKING -> {
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
                OrbitState.LISTENING -> {
                    // Wide, alert eyes
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
                else -> {
                    // IDLE / SPEAKING: Natural blinking vertical pill eyes
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
