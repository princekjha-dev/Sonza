package com.sonza.app.ai.orbit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.components.LocalSonzaDynamicColors

/**
 * Ambient Orbit Assistant Floating Layer.
 *
 * Implements the non-modal, ambient voice assistant design:
 * - NO giant modal listening card or full-screen translucent scrim.
 * - NO "LISTENING..." text labels, suggestion chips, or repeated icons.
 * - ONLY the single reactive Orbit Avatar with dynamic live artwork tinting.
 * - ONE small, clean X cross button to cancel / dismiss.
 * - Music UI remains 100% visible and usable underneath.
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

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(220)) + scaleIn(
            initialScale = 0.82f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut(animationSpec = tween(180)) + scaleOut(
            targetScale = 0.82f,
            animationSpec = tween(180)
        ),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 120.dp) // Float gracefully above bottom navigation & mini-player
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Container holding Single Avatar + small close control
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                // 1. SINGLE ORBIT AVATAR (Hero size, audio reactive, dynamic artwork color, headphones in music mode)
                OrbitAvatar(
                    state = state,
                    size = OrbitAvatarSize.Hero,
                    isPlaying = isPlaying,
                    audioLevel = audioLevel,
                    musicAudioData = musicAudioData,
                    accentColor = accentColor,
                    onClick = onAvatarClick
                )

                // 2. SINGLE DISCREET X / CANCEL BUTTON
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-6).dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cancel Orbit",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
