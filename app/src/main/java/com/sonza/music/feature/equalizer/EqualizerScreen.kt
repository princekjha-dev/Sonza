package com.sonza.music.feature.equalizer

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SurroundSound
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.sonza.music.audio.analyzer.AudioAnalyzer
import com.sonza.music.audio.equalizer.SonzaEqualizer
import com.sonza.music.audio.spatial.SpatialAudioProcessor
import com.sonza.music.core.model.EqualizerPreset
import com.sonza.music.core.model.SpatialConfig
import com.sonza.music.core.model.SpatialMode
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaSurface
import com.sonza.music.core.theme.SonzaSurfaceElevated
import com.sonza.music.core.theme.SonzaSurfaceVariant
import com.sonza.music.core.theme.SonzaTextPrimary
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.core.theme.SonzaTextTertiary
import com.sonza.music.feature.visualizer.AudioVisualizerView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizer: SonzaEqualizer,
    private val spatialProcessor: SpatialAudioProcessor,
    val analyzer: AudioAnalyzer
) : ViewModel() {

    val gainsFlow: StateFlow<List<Float>> = equalizer.gainsFlow
    val presetFlow: StateFlow<EqualizerPreset> = equalizer.presetFlow
    val spatialConfigFlow: StateFlow<SpatialConfig> = spatialProcessor.config

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        equalizer.setBandGain(bandIndex, gainDb)
    }

    fun setPreampGain(gainDb: Float) {
        equalizer.setPreampGain(gainDb)
    }

    fun applyPreset(preset: EqualizerPreset) {
        equalizer.applyPreset(preset)
    }

    fun resetEq() {
        equalizer.applyPreset(EqualizerPreset.FLAT)
    }

    fun updateSpatial(config: SpatialConfig) {
        spatialProcessor.updateConfig(config)
    }
}

@Composable
fun EqualizerScreen(
    gains: List<Float>,
    activePreset: EqualizerPreset,
    spatialConfig: SpatialConfig,
    onBandChange: (Int, Float) -> Unit,
    onPresetChange: (EqualizerPreset) -> Unit,
    onReset: () -> Unit,
    onSpatialChange: (SpatialConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaDarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SonzaTextPrimary)
                }

                Text(
                    text = "Parametric EQ & DSP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SonzaTextPrimary
                )

                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset EQ", tint = SonzaCyanAccent)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smooth Frequency Curve Visualizer Canvas
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SonzaSurfaceElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    FrequencyCurveCanvas(gains = gains)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Preset Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(EqualizerPreset.FACTORY_PRESETS) { preset ->
                    val isSelected = activePreset.id == preset.id
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) SonzaCyanAccent.copy(alpha = 0.2f) else SonzaSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) SonzaCyanAccent else Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.clickable { onPresetChange(preset) }
                    ) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) SonzaCyanAccent else SonzaTextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 10-Band Sliders
            Text(
                text = "10-Band Studio Equalizer (-12dB to +12dB)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SonzaTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            EqualizerPreset.FREQUENCY_BANDS.forEachIndexed { index, band ->
                val currentGain = gains.getOrElse(index) { 0.0f }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Alignment.CenterVertically
                ) {
                    Text(
                        text = band.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SonzaTextSecondary,
                        modifier = Modifier.width(64.dp)
                    )

                    Slider(
                        value = currentGain,
                        onValueChange = { onBandChange(index, it) },
                        valueRange = -12.0f..12.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = SonzaCyanAccent,
                            activeTrackColor = SonzaCyanAccent,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = String.format("%+.1f dB", currentGain),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentGain != 0.0f) SonzaCyanAccent else SonzaTextTertiary,
                        modifier = Modifier.width(68.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Spatial Audio Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SonzaSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Alignment.CenterVertically) {
                            Icon(Icons.Default.SurroundSound, contentDescription = null, tint = SonzaCyanAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Binaural Spatial Audio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SonzaTextPrimary
                            )
                        }

                        Switch(
                            checked = spatialConfig.enabled,
                            onCheckedChange = { onSpatialChange(spatialConfig.copy(enabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SonzaCyanAccent, checkedTrackColor = SonzaCyanAccent.copy(alpha = 0.4f))
                        )
                    }

                    if (spatialConfig.enabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "Spatial Modes", style = MaterialTheme.typography.bodyMedium, color = SonzaTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(SpatialMode.values().filter { it != SpatialMode.OFF }) { mode ->
                                val isSelected = spatialConfig.mode == mode
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) SonzaCyanAccent.copy(alpha = 0.2f) else SonzaSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SonzaCyanAccent else Color(0x22FFFFFF)),
                                    modifier = Modifier.clickable { onSpatialChange(spatialConfig.copy(mode = mode)) }
                                ) {
                                    Text(
                                        text = mode.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) SonzaCyanAccent else SonzaTextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun FrequencyCurveCanvas(gains: List<Float>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Draw 0dB Reference Line
        drawLine(
            color = Color(0x33FFFFFF),
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1.dp.toPx()
        )

        if (gains.isEmpty()) return@Canvas

        val stepX = width / (gains.size - 1)
        val path = Path()

        val points = gains.mapIndexed { idx, db ->
            val x = idx * stepX
            // Map -12dB to height, +12dB to 0
            val y = midY - (db / 12f) * (midY * 0.85f)
            Offset(x, y)
        }

        path.moveTo(points.first().x, points.first().y)
        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val controlPointX = (p0.x + p1.x) / 2f
            path.cubicTo(controlPointX, p0.y, controlPointX, p1.y, p1.x, p1.y)
        }

        // Draw glowing gradient curve
        drawPath(
            path = path,
            color = SonzaCyanAccent,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
