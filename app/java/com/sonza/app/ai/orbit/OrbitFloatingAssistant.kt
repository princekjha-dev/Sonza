package com.sonza.app.ai.orbit

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings as SettingsIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
 * Reimagined Orbit Immersive Voice Assistant Surface.
 *
 * Interaction Architecture inspired by modern fluid voice surfaces:
 * - Keeps underlying music screen visible behind a smooth translucent dark glass scrim.
 * - Centerpiece is a prominent Hero Orbit Orb with dynamic cyan/blue acoustic reactivity.
 * - Above the orb, a floating frosted glass conversational card displays real-time speech
 *   transcription, intent execution feedback, and quick suggestion chips.
 * - Tap anywhere outside or press the close button to dismiss cleanly and restore audio state.
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
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var showTextInput by remember { mutableStateOf(false) }

    val isPermissionError = state == OrbitState.ERROR &&
            (message.contains("permission", ignoreCase = true) || message.contains("microphone access", ignoreCase = true))

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(220)) + scaleIn(
            initialScale = 0.94f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        ),
        exit = fadeOut(animationSpec = tween(180)) + scaleOut(
            targetScale = 0.96f,
            animationSpec = tween(180)
        ),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x9905070D)) // Smooth dark obsidian scrim
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Ambient Futuristic Cyan/Blue Radial Glow behind the Orb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                when (state) {
                                    OrbitState.ERROR -> Color(0xFFE57373).copy(alpha = 0.22f)
                                    OrbitState.THINKING -> Color(0xFFBA68C8).copy(alpha = 0.24f)
                                    OrbitState.SUCCESS -> Color(0xFF81C784).copy(alpha = 0.24f)
                                    else -> Color(0xFF00E5FF).copy(alpha = 0.22f)
                                },
                                Color(0xFF0052FF).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Main Assistant Content Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume click so card doesn't dismiss when tapped
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Floating Conversational Surface / Speech Card with Liquid Glass
                com.sonza.app.ui.components.glass.LiquidGlassOrbitPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp),
                    shape = RoundedCornerShape(28.dp),
                    tint = when (state) {
                        OrbitState.LISTENING -> Color(0xFF00E5FF).copy(alpha = 0.16f)
                        OrbitState.THINKING, OrbitState.WORKING -> Color(0xFF38BDF8).copy(alpha = 0.14f)
                        OrbitState.SPEAKING -> Color(0xFF818CF8).copy(alpha = 0.14f)
                        OrbitState.ERROR -> Color(0xFFFF5252).copy(alpha = 0.15f)
                        else -> Color(0xFF38BDF8).copy(alpha = 0.10f)
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Row: Status badge + Typing toggle + Close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (state) {
                                                OrbitState.LISTENING -> SonzaDesignTokens.Colors.OrbitListening
                                                OrbitState.THINKING -> SonzaDesignTokens.Colors.OrbitThinking
                                                OrbitState.WORKING -> SonzaDesignTokens.Colors.OrbitWorking
                                                OrbitState.SPEAKING -> SonzaDesignTokens.Colors.OrbitSpeaking
                                                OrbitState.SUCCESS -> SonzaDesignTokens.Colors.OrbitSuccess
                                                OrbitState.ERROR -> SonzaDesignTokens.Colors.OrbitError
                                                else -> SonzaDesignTokens.Colors.OrbitIdle
                                            }
                                        )
                                )
                                val statusLabel = when (state) {
                                    OrbitState.LISTENING -> "Listening…"
                                    OrbitState.THINKING -> "Thinking…"
                                    OrbitState.WORKING -> "Executing…"
                                    OrbitState.SPEAKING -> "Speaking…"
                                    OrbitState.SUCCESS -> "Done"
                                    OrbitState.ERROR -> "Notice"
                                    else -> "Orbit Standby"
                                }
                                Text(
                                    text = statusLabel.uppercase(),
                                    style = SonzaTypography.Kicker.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = SonzaDesignTokens.Colors.TextPrimary
                                    )
                                )
                            }

                            // Action buttons: Keyboard toggle + Close
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SonzaIconButton(
                                    icon = Icons.Rounded.Keyboard,
                                    contentDescription = "Toggle Keyboard",
                                    onClick = { showTextInput = !showTextInput },
                                    size = 32.dp,
                                    iconSize = 16.dp,
                                    backgroundColor = if (showTextInput) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f)
                                )

                                SonzaIconButton(
                                    icon = Icons.Rounded.Close,
                                    contentDescription = "Close Orbit",
                                    onClick = onDismiss,
                                    size = 32.dp,
                                    iconSize = 16.dp,
                                    backgroundColor = Color.White.copy(alpha = 0.06f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Real-time Recognized Speech / Status Message / Spoken Response
                        val displayHeading = if (!actionIndicator.isNullOrBlank()) "$actionIndicator $message" else message
                        Text(
                            text = displayHeading,
                            style = SonzaTypography.BodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 23.sp
                            ),
                            color = if (state == OrbitState.ERROR) Color(0xFFFF8A80) else Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp)
                        )

                        // Permission Action Buttons if permission denied
                        if (isPermissionError) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onAvatarClick() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color(0xFF05070D)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Grant Access", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SettingsIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Settings", fontSize = 13.sp)
                                }
                            }
                        }

                        // Suggestion Pills Row (Quick Voice Triggers)
                        if (!isPermissionError && !showTextInput) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SonzaPill(
                                    text = "Romantic Songs",
                                    onClick = { onSubmitText("Play romantic songs") }
                                )
                                SonzaPill(
                                    text = "Play Liked",
                                    onClick = { onSubmitText("Play my liked songs") }
                                )
                                SonzaPill(
                                    text = "What's Playing?",
                                    onClick = { onSubmitText("What's playing?") }
                                )
                                SonzaPill(
                                    text = "Relaxing Music",
                                    onClick = { onSubmitText("Play something relaxing") }
                                )
                                SonzaPill(
                                    text = "Surprise Me",
                                    onClick = { onSubmitText("Surprise me") }
                                )
                            }
                        }

                        // Optional Manual Text Query Field
                        if (showTextInput) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("Ask Orbit anything...", color = SonzaDesignTokens.Colors.TextTertiary, fontSize = 14.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(22.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (textInput.isNotBlank()) {
                                        val query = textInput
                                        textInput = ""
                                        showTextInput = false
                                        onSubmitText(query)
                                    }
                                }),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (textInput.isNotBlank()) {
                                            val query = textInput
                                            textInput = ""
                                            showTextInput = false
                                            onSubmitText(query)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Send,
                                            contentDescription = "Send",
                                            tint = Color(0xFF00E5FF)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Centerpiece: The Large Hero Orbit Avatar
                // Tapping interrupts Orbit's speech, or starts listening, or cancels listening
                OrbitAvatar(
                    state = state,
                    size = OrbitAvatarSize.Hero,
                    audioLevel = audioLevel,
                    onClick = onAvatarClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Interaction prompt label below orb
                val actionHint = when (state) {
                    OrbitState.SPEAKING -> "Tap orb to interrupt"
                    OrbitState.LISTENING -> "Tap orb to cancel"
                    OrbitState.THINKING, OrbitState.WORKING -> "Processing command…"
                    else -> "Tap orb to speak"
                }
                Text(
                    text = actionHint,
                    style = SonzaTypography.Caption.copy(
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
