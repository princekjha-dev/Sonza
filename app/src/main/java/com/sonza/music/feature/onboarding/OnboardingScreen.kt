package com.sonza.music.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.music.core.model.OutputGearPreference
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaSurface
import com.sonza.music.core.theme.SonzaSurfaceElevated
import com.sonza.music.core.theme.SonzaTextPrimary
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun completeOnboarding(
        genres: List<String>,
        gear: OutputGearPreference,
        quality: String
    ) {
        viewModelScope.launch {
            settingsRepository.setSelectedGenres(genres)
            settingsRepository.setOutputGear(gear)
            settingsRepository.setStreamingQuality(quality)
            settingsRepository.setOnboarded(true)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    onCompleteOnboarding: (List<String>, OutputGearPreference, String) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val selectedGenres = remember { mutableStateListOf("Electronic", "Jazz", "Classical", "Rock") }
    var selectedGear by remember { mutableStateOf(OutputGearPreference.EXTERNAL_HI_RES_DAC) }
    var selectedQuality by remember { mutableStateOf("Lossless (24-bit/96kHz FLAC)") }

    val allGenres = listOf(
        "Pop", "Hip-Hop", "Rock", "Electronic", "Classical", "Lo-Fi", "Jazz", "Metal", "Indie", "Bollywood", "Acoustic", "Ambient"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SonzaDarkBackground, Color(0xFF0F121E), SonzaDarkBackground)
                )
            )
            .padding(24.dp)
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                )
            },
            label = "onboarding_steps"
        ) { currentStep ->
            when (currentStep) {
                0 -> {
                    // Screen 1: Brand Identity
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(SonzaSurfaceElevated)
                                .border(2.dp, SonzaCyanAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "SONZA Logo",
                                tint = SonzaCyanAccent,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = "SONZA",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SonzaTextPrimary,
                            letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Hear Music Differently.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SonzaCyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "High-fidelity lossless playback, dynamic acoustic visuals, parametric equalizer & synchronized listening rooms.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SonzaTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                1 -> {
                    // Screen 2: Genre Preferences
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "What do you listen to?",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SonzaTextPrimary
                        )
                        Text(
                            text = "Select your preferred acoustic genres for personalized curation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonzaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            allGenres.forEach { genre ->
                                val isSelected = selectedGenres.contains(genre)
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) SonzaCyanAccent.copy(alpha = 0.2f) else SonzaSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) SonzaCyanAccent else Color(0x33FFFFFF)
                                    ),
                                    modifier = Modifier.clickable {
                                        if (isSelected) selectedGenres.remove(genre) else selectedGenres.add(genre)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertAlignment
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SonzaCyanAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = genre,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isSelected) SonzaCyanAccent else SonzaTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Screen 3: Output Gear
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "How do you listen?",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SonzaTextPrimary
                        )
                        Text(
                            text = "We tune the default DSP profile and spatializer to your primary gear.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonzaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val gearOptions = listOf(
                            OutputGearPreference.EXTERNAL_HI_RES_DAC to "External Hi-Res DAC / AMP",
                            OutputGearPreference.WIRED_HEADPHONES to "Audiophile Wired Headphones",
                            OutputGearPreference.BLUETOOTH_LDAC_APT_X to "Bluetooth (LDAC / aptX HD)",
                            OutputGearPreference.CAR_AUDIO to "Car Audio System",
                            OutputGearPreference.PHONE_SPEAKERS to "Built-in Phone Speakers"
                        )

                        gearOptions.forEach { (gear, label) ->
                            val isSelected = selectedGear == gear
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) SonzaCyanAccent.copy(alpha = 0.15f) else SonzaSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SonzaCyanAccent else Color(0x22FFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { selectedGear = gear }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertAlignment
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = if (isSelected) SonzaCyanAccent else SonzaTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) SonzaCyanAccent else SonzaTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Screen 4: Streaming Quality
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Preferred Audio Quality",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SonzaTextPrimary
                        )
                        Text(
                            text = "Bit-perfect reproduction. Never artificially upsampled or distorted.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonzaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val qualities = listOf(
                            "Lossless (24-bit/96kHz FLAC)" to "Studio master bit-perfect quality for critical listening.",
                            "High Quality (320kbps Opus/AAC)" to "Clean, dynamic sound with lower data usage.",
                            "Automatic Adaptive" to "Dynamically matches connection bandwidth."
                        )

                        qualities.forEach { (title, desc) ->
                            val isSelected = selectedQuality == title
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) SonzaCyanAccent.copy(alpha = 0.15f) else SonzaSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SonzaCyanAccent else Color(0x22FFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { selectedQuality = title }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertAlignment) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = null,
                                            tint = if (isSelected) SonzaCyanAccent else SonzaTextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) SonzaCyanAccent else SonzaTextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SonzaTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // Screen 5: Permissions & Ready
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SonzaCyanAccent,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Ready for Audiophile Audio",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SonzaTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SONZA only accesses audio files to index your local lossless library and requires notification permissions for background MediaSession controls.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SonzaTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertAlignment
        ) {
            // Page Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(5) { dotIndex ->
                    Box(
                        modifier = Modifier
                            .size(if (dotIndex == step) 18.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (dotIndex == step) SonzaCyanAccent else Color(0x44FFFFFF))
                    )
                }
            }

            Button(
                onClick = {
                    if (step < 4) {
                        step++
                    } else {
                        onCompleteOnboarding(selectedGenres, selectedGear, selectedQuality)
                        onFinished()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SonzaCyanAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (step == 4) "Start Listening" else "Continue",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
