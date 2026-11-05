package com.sonza.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.core.model.Song
import com.sonza.app.data.repository.FeedbackRepository
import com.sonza.app.data.repository.FeedbackResult
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.computeOnAccent
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * UI State for Send Feedback Screen
 */
sealed interface FeedbackUiState {
    data object Idle : FeedbackUiState
    data object Sending : FeedbackUiState
    data object Success : FeedbackUiState
    data class Error(val message: String) : FeedbackUiState
}

/**
 * Rebuilt Send Feedback Screen.
 * - Dynamic color system support (no hardcoded orange)
 * - Safe networking via FeedbackRepository with zero raw technical exception leakage
 * - Clear state transitions: Idle → Sending → Success / Error
 * - Mini-player aware bottom padding with responsive keyboard ime handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit,
    currentSong: Song? = null,
    feedbackRepository: FeedbackRepository = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
    val onAccentColor = dynamicColors.onAccent.takeIf { it != Color.Unspecified } ?: computeOnAccent(accentColor)

    var message by remember { mutableStateOf("") }
    var uiState by remember { mutableStateOf<FeedbackUiState>(FeedbackUiState.Idle) }
    val scrollState = rememberScrollState()

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val hasMiniPlayer = currentSong != null
    val bottomSystemHeight = if (hasMiniPlayer) ExpressiveBottomNavTokens.TotalBottomBarHeight else 0.dp
    val bottomInset = navBarPadding + bottomSystemHeight + SpacingTokens.Space2Xl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Back Arrow + Title
            TopAppBar(
                title = {
                    Text(
                        text = "Send Feedback",
                        fontWeight = FontWeight.Bold,
                        style = SonzaTypography.Headline,
                        color = SonzaOnBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SonzaOnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = SonzaOnBackground,
                    navigationIconContentColor = SonzaOnBackground
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(horizontal = SpacingTokens.SpaceLg)
                    .padding(bottom = bottomInset),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Centered Feedback Icon in Squircle Container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(SquircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Feedback,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Send Feedback",
                    style = SonzaTypography.Headline,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = "Help us improve Sonza by sharing your thoughts.",
                    style = SonzaTypography.BodyMedium,
                    color = SonzaOnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                if (uiState is FeedbackUiState.Success) {
                    // Success Confirmation State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp)
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Feedback sent successfully",
                            style = SonzaTypography.TitleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SonzaOnBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Thanks for helping us improve Sonza.",
                            style = SonzaTypography.BodyMedium,
                            color = SonzaOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        TextButton(
                            onClick = {
                                message = ""
                                uiState = FeedbackUiState.Idle
                            },
                            shape = RoundedCornerShape(RadiusTokens.Pill)
                        ) {
                            Text(
                                text = "Send another feedback",
                                color = accentColor,
                                style = SonzaTypography.BodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    // Main Form: Large rounded multiline input
                    val isSending = uiState is FeedbackUiState.Sending
                    val maxChars = 2000

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp)
                    ) {
                        OutlinedTextField(
                            value = message,
                            onValueChange = {
                                if (it.length <= maxChars) {
                                    message = it
                                    if (uiState is FeedbackUiState.Error) {
                                        uiState = FeedbackUiState.Idle
                                    }
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Tell us what you think…",
                                    color = SonzaOnSurfaceVariant.copy(alpha = 0.6f),
                                    style = SonzaTypography.BodyLarge
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SonzaSurfaceVariant,
                                unfocusedContainerColor = SonzaSurfaceVariant,
                                disabledContainerColor = SonzaSurfaceVariant.copy(alpha = 0.6f),
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = SonzaOutline.copy(alpha = 0.35f),
                                cursorColor = accentColor,
                                focusedTextColor = SonzaOnBackground,
                                unfocusedTextColor = SonzaOnBackground,
                                disabledTextColor = SonzaOnBackground.copy(alpha = 0.5f)
                            ),
                            textStyle = SonzaTypography.BodyLarge.copy(lineHeight = 22.sp),
                            singleLine = false,
                            enabled = !isSending
                        )

                        // Character counter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${message.length} / $maxChars",
                                style = SonzaTypography.LabelSmall,
                                color = SonzaOnSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }

                        // Inline Error State (if any)
                        if (uiState is FeedbackUiState.Error) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = (uiState as FeedbackUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = SonzaTypography.BodySmall.copy(fontWeight = FontWeight.Medium),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Full-Width Dynamic Primary Action Button
                        Button(
                            onClick = {
                                if (message.trim().isNotBlank() && !isSending) {
                                    uiState = FeedbackUiState.Sending
                                    scope.launch {
                                        val result = feedbackRepository.submitFeedback(
                                            message = message,
                                            context = context
                                        )
                                        when (result) {
                                            is FeedbackResult.Success -> {
                                                message = ""
                                                uiState = FeedbackUiState.Success
                                            }
                                            is FeedbackResult.Error -> {
                                                uiState = FeedbackUiState.Error(result.userFacingMessage)
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = message.trim().isNotBlank() && !isSending,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = onAccentColor,
                                disabledContainerColor = accentColor.copy(alpha = 0.25f),
                                disabledContentColor = onAccentColor.copy(alpha = 0.4f)
                            )
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = onAccentColor,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Send Feedback",
                                    fontWeight = FontWeight.Bold,
                                    style = SonzaTypography.TitleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
