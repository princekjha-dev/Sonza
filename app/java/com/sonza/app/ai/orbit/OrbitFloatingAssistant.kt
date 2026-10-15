package com.sonza.app.ai.orbit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.components.sonza.SonzaDesignTokens
import com.sonza.app.ui.components.sonza.SonzaGlassSurface
import com.sonza.app.ui.components.sonza.SonzaIconButton
import com.sonza.app.ui.components.sonza.SonzaPill
import com.sonza.app.ui.theme.SonzaTypography

/**
 * Compact Floating Orbit Mini Assistant.
 * Pops up gracefully without blocking the music interface.
 */
@Composable
fun OrbitFloatingAssistant(
    isVisible: Boolean,
    state: OrbitState,
    message: String,
    audioLevel: Float,
    actionIndicator: String? = null,
    onAvatarClick: () -> Unit,
    onDismiss: () -> Unit,
    onSubmitText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var showTextInput by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SonzaGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(26.dp),
                backgroundColor = Color(0xFF161720).copy(alpha = 0.94f),
                borderColor = Color.White.copy(alpha = 0.14f),
                elevation = SonzaDesignTokens.Elevation.GlassFloat
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row: Status badge + Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (state) {
                                            OrbitState.LISTENING -> SonzaDesignTokens.Colors.OrbitListening
                                            OrbitState.THINKING -> SonzaDesignTokens.Colors.OrbitThinking
                                            OrbitState.SUCCESS -> SonzaDesignTokens.Colors.OrbitSuccess
                                            OrbitState.ERROR -> SonzaDesignTokens.Colors.OrbitError
                                            else -> SonzaDesignTokens.Colors.OrbitIdle
                                        }
                                    )
                            )
                            Text(
                                text = "ORBIT",
                                style = SonzaTypography.Kicker.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    color = SonzaDesignTokens.Colors.TextSecondary
                                )
                            )
                        }

                        SonzaIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "Close Orbit",
                            onClick = onDismiss,
                            size = 32.dp,
                            iconSize = 16.dp,
                            backgroundColor = Color.White.copy(alpha = 0.06f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Centerpiece: Orbit Avatar (tap to toggle listen/cancel)
                    OrbitAvatar(
                        state = state,
                        size = OrbitAvatarSize.Medium,
                        audioLevel = audioLevel,
                        onClick = onAvatarClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speech transcription / Status response
                    val displayText = if (!actionIndicator.isNullOrBlank()) "$actionIndicator $message" else message
                    Text(
                        text = displayText,
                        style = SonzaTypography.BodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = SonzaDesignTokens.Colors.TextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Suggestions Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SonzaPill(
                            text = "Play Liked Songs",
                            onClick = { onSubmitText("Play my liked songs") }
                        )
                        SonzaPill(
                            text = "Romantic Mix",
                            onClick = { onSubmitText("Play romantic songs") }
                        )
                        SonzaPill(
                            text = "Sleep Timer 30m",
                            onClick = { onSubmitText("Set sleep timer for 30 minutes") }
                        )
                        SonzaPill(
                            text = "Surprise Me",
                            onClick = { onSubmitText("Surprise me") }
                        )
                    }

                    if (showTextInput) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Ask Orbit anything...", color = SonzaDesignTokens.Colors.TextTertiary) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (textInput.isNotBlank()) {
                                    onSubmitText(textInput)
                                    textInput = ""
                                }
                            }),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (textInput.isNotBlank()) {
                                        onSubmitText(textInput)
                                        textInput = ""
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Send,
                                        contentDescription = "Send",
                                        tint = SonzaDesignTokens.Colors.BrandAccent
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SonzaDesignTokens.Colors.BrandAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
