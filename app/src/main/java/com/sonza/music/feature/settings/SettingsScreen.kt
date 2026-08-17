package com.sonza.music.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.music.core.model.HapticIntensity
import com.sonza.music.core.model.ThemeModePreference
import com.sonza.music.core.model.UserPreferences
import com.sonza.music.core.model.VolumeNormalizationMode
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaSurface
import com.sonza.music.core.theme.SonzaSurfaceElevated
import com.sonza.music.core.theme.SonzaTextPrimary
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.core.theme.SonzaTextTertiary
import com.sonza.music.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = settingsRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setHapticIntensity(intensity: HapticIntensity) {
        viewModelScope.launch { settingsRepository.setHapticIntensity(intensity) }
    }

    fun setVolumeNormalization(mode: VolumeNormalizationMode) {
        viewModelScope.launch { settingsRepository.setVolumeNormalization(mode) }
    }

    fun setCrossfadeDuration(seconds: Int) {
        viewModelScope.launch { settingsRepository.setCrossfadeDuration(seconds) }
    }

    fun setGapless(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setGaplessEnabled(enabled) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setReduceMotion(enabled) }
    }

    fun setThemeMode(mode: ThemeModePreference) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }
}

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onHapticChange: (HapticIntensity) -> Unit,
    onVolumeNormChange: (VolumeNormalizationMode) -> Unit,
    onCrossfadeChange: (Int) -> Unit,
    onGaplessChange: (Boolean) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeModePreference) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaDarkBackground)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SonzaTextPrimary)
                }

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SonzaTextPrimary
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                // Section: Playback & DSP
                item {
                    SettingsSection(title = "Playback & Audio Engine", icon = Icons.Default.PlayCircle) {
                        // Gapless Playback Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Gapless Playback", style = MaterialTheme.typography.titleMedium, color = SonzaTextPrimary)
                                Text(text = "Seamless transitions on live & classical albums", style = MaterialTheme.typography.bodyMedium, color = SonzaTextSecondary)
                            }
                            Switch(
                                checked = preferences.gaplessEnabled,
                                onCheckedChange = onGaplessChange,
                                colors = SwitchDefaults.colors(checkedThumbColor = SonzaCyanAccent, checkedTrackColor = SonzaCyanAccent.copy(alpha = 0.4f))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Crossfade Duration Slider
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Crossfade Duration", style = MaterialTheme.typography.titleMedium, color = SonzaTextPrimary)
                                Text(text = "${preferences.crossfadeDurationSeconds}s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SonzaCyanAccent)
                            }
                            Slider(
                                value = preferences.crossfadeDurationSeconds.toFloat(),
                                onValueChange = { onCrossfadeChange(it.toInt()) },
                                valueRange = 0f..12f,
                                steps = 5,
                                colors = SliderDefaults.colors(thumbColor = SonzaCyanAccent, activeTrackColor = SonzaCyanAccent)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Volume Normalization Mode
                        Text(text = "Volume Normalization (ReplayGain)", style = MaterialTheme.typography.titleMedium, color = SonzaTextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VolumeNormalizationMode.values().forEach { mode ->
                                val isSelected = preferences.volumeNormalization == mode
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SonzaCyanAccent.copy(alpha = 0.2f) else SonzaSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SonzaCyanAccent else Color(0x22FFFFFF)),
                                    modifier = Modifier.clickable { onVolumeNormChange(mode) }
                                ) {
                                    Text(
                                        text = mode.name.replace("_", " "),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) SonzaCyanAccent else SonzaTextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Beat Haptics & Visuals
                item {
                    SettingsSection(title = "Haptics & Aesthetics", icon = Icons.Default.Vibration) {
                        Text(text = "Beat-Synchronized Musical Haptics", style = MaterialTheme.typography.titleMedium, color = SonzaTextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HapticIntensity.values().forEach { intensity ->
                                val isSelected = preferences.hapticIntensity == intensity
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SonzaCyanAccent.copy(alpha = 0.2f) else SonzaSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SonzaCyanAccent else Color(0x22FFFFFF)),
                                    modifier = Modifier.clickable { onHapticChange(intensity) }
                                ) {
                                    Text(
                                        text = intensity.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) SonzaCyanAccent else SonzaTextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Reduce Motion", style = MaterialTheme.typography.titleMedium, color = SonzaTextPrimary)
                                Text(text = "Disables heavy spring transitions and visualizers", style = MaterialTheme.typography.bodyMedium, color = SonzaTextSecondary)
                            }
                            Switch(
                                checked = preferences.reduceMotion,
                                onCheckedChange = onReduceMotionChange,
                                colors = SwitchDefaults.colors(checkedThumbColor = SonzaCyanAccent, checkedTrackColor = SonzaCyanAccent.copy(alpha = 0.4f))
                            )
                        }
                    }
                }

                // Section: Privacy & Storage
                item {
                    SettingsSection(title = "Privacy & Data", icon = Icons.Default.Lock) {
                        Text(
                            text = "SONZA is privacy-first. No listening habits or telemetry are sold or uploaded to third-party ad networks.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonzaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SonzaSurface,
                            modifier = Modifier.fillMaxWidth().clickable { }
                        ) {
                            Text(
                                text = "Export Listening History (JSON)",
                                style = MaterialTheme.typography.titleMedium,
                                color = SonzaCyanAccent,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // App Version Footer
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SONZA v1.0.0 (Audiophile Release)",
                            style = MaterialTheme.typography.labelMedium,
                            color = SonzaTextTertiary
                        )
                        Text(
                            text = "Hear Music Differently.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonzaCyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SonzaSurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = SonzaCyanAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SonzaTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
