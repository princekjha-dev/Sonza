package com.sonza.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * First-run onboarding experience.
 *
 * Designed as a completely isolated full-screen overlay (not an OS dialog window)
 * to guarantee zero leakage of underlying Home elements, mini-players, or navigation bars.
 *
 * Step 1: Minimal, premium brand-forward welcome with YouTube Music connect & guest exploration.
 * Step 2: "Shape your sound" language preference selection with dynamic accent pill chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeOnboardingDialog(
    onLoginClick: () -> Unit,
    onContinueAsGuest: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }
    val selectedLanguages = remember { mutableStateOf(setOf<String>()) }

    val dynamicAccent = LocalSonzaDynamicColors.current.accent.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary
    val onAccent = LocalSonzaDynamicColors.current.onAccent.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.onPrimary

    // Handle back navigation: Step 2 navigates back to Step 1; Step 1 intercepts back (gate onboarding)
    BackHandler(enabled = true) {
        if (step > 1) {
            step = 1
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = tween(380, easing = FastOutSlowInEasing)) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                            fadeIn(tween(350)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { fullWidth -> -(fullWidth * 0.35f).toInt() } +
                                    fadeOut(tween(250))
                        )
                } else {
                    (slideInHorizontally(animationSpec = tween(380, easing = FastOutSlowInEasing)) { fullWidth -> -(fullWidth * 0.35f).toInt() } +
                            fadeIn(tween(350)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { fullWidth -> (fullWidth * 0.35f).toInt() } +
                                    fadeOut(tween(250))
                        )
                }
            },
            label = "OnboardingStepTransition"
        ) { currentStep ->
            when (currentStep) {
                1 -> WelcomeStep(
                    accentColor = dynamicAccent,
                    onAccentColor = onAccent,
                    onLoginClick = onLoginClick,
                    onContinueAsGuest = { step = 2 }
                )
                2 -> LanguagePreferencesStep(
                    selected = selectedLanguages.value,
                    accentColor = dynamicAccent,
                    onAccentColor = onAccent,
                    onToggle = { lang ->
                        selectedLanguages.value =
                            if (lang in selectedLanguages.value) selectedLanguages.value - lang
                            else selectedLanguages.value + lang
                    },
                    onDone = { onContinueAsGuest(selectedLanguages.value) },
                    onSkip = { onContinueAsGuest(emptySet()) }
                )
            }
        }
    }
}

/**
 * Screen 1 — Welcome: Fresh, original Sonza copy and minimal dark visual identity.
 */
@Composable
private fun WelcomeStep(
    accentColor: Color,
    onAccentColor: Color,
    onLoginClick: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.weight(0.85f))

        // Brand logo with subtle ambient radial glow
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            AppLogo(size = 72.dp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fresh welcome headline
        Text(
            text = "Your music.\nYour way.",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 44.sp,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Supporting description
        Text(
            text = "Bring your favorite music into one simple, uninterrupted listening experience.",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp,
                letterSpacing = (-0.2).sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.weight(1f))

        // Primary CTA: Connect YouTube Music
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = onAccentColor
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = "Connect YouTube Music",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary action: Explore as guest
        TextButton(
            onClick = onContinueAsGuest,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(percent = 50)
        ) {
            Text(
                text = "Explore as guest",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// 15 curated languages for recommendation bootstrapping
private val TASTE_LANGUAGES = listOf(
    "Hindi", "English", "Bengali", "Punjabi", "Marathi", "Gujarati",
    "Tamil", "Telugu", "Kannada", "Malayalam", "Urdu", "Bhojpuri",
    "Korean", "Spanish", "Japanese"
)

/**
 * Screen 2 — Music Preferences: Language selection with dynamic accent chips & isolated layout.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguagePreferencesStep(
    selected: Set<String>,
    accentColor: Color,
    onAccentColor: Color,
    onToggle: (String) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Headline
        Text(
            text = "Shape your sound",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 42.sp,
                letterSpacing = (-0.8).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Supporting text
        Text(
            text = "Choose the languages you listen to most. Sonza will use your choices to make your first recommendations feel more like you.",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 23.sp,
                letterSpacing = (-0.2).sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Scrollable language chips section
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TASTE_LANGUAGES.forEach { lang ->
                    SelectableLanguageChip(
                        language = lang,
                        isSelected = lang in selected,
                        accentColor = accentColor,
                        onAccentColor = onAccentColor,
                        onClick = { onToggle(lang) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary CTA: Continue
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = onAccentColor
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary action: I'll do this later
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(percent = 50)
        ) {
            Text(
                text = "I'll do this later",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Rounded pill-shaped selection chip with dynamic accent color support.
 */
@Composable
private fun SelectableLanguageChip(
    language: String,
    isSelected: Boolean,
    accentColor: Color,
    onAccentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        animationSpec = tween(durationMillis = 200),
        label = "chip_bg"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) onAccentColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
        animationSpec = tween(durationMillis = 200),
        label = "chip_content"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        animationSpec = tween(durationMillis = 200),
        label = "chip_border"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = animatedContainerColor,
        border = if (!isSelected) BorderStroke(1.dp, animatedBorderColor) else null,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(150)) + expandHorizontally(tween(150)),
                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = animatedContentColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            Text(
                text = language,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = animatedContentColor
            )
        }
    }
}
