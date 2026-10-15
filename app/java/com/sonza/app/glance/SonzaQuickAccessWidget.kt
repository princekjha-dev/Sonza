package com.sonza.app.glance

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sonza.app.R

class SonzaQuickAccessWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaQuickAccessWidget()
}

/**
 * Widget D — AI Quick Access Widget
 * Directly deep-links to existing Sonza features: Orbit Voice AI, Equalizer, Lyrics, and Search.
 */
class SonzaQuickAccessWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val backgroundColor = Color(0xFF14151F).copy(alpha = 0.96f)

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(backgroundColor))
                        .cornerRadius(24.dp)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Header
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(32.dp)
                                    .cornerRadius(16.dp)
                                    .background(ColorProvider(Color(0xFF4F86F7).copy(alpha = 0.3f))),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_mic),
                                    contentDescription = "Orbit AI",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF4F86F7))),
                                    modifier = GlanceModifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(10.dp))

                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = "SONZA AI",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF4F86F7)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "What would you like to do?",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(12.dp))

                        // 4 Quick Action Tiles: [Orbit] [EQ] [Lyrics] [Search]
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Orbit Voice AI Tile
                            QuickActionTile(
                                label = "Orbit AI",
                                iconRes = R.drawable.ic_mic,
                                tintColor = Color(0xFF4F86F7),
                                deepLinkUri = "sonza://orbit_listen",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            // 2. Equalizer Tile
                            QuickActionTile(
                                label = "EQ / DSP",
                                iconRes = R.drawable.ic_equalizer,
                                tintColor = Color(0xFF00E5FF),
                                deepLinkUri = "sonza://eq",
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3. Lyrics Tile
                            QuickActionTile(
                                label = "Lyrics",
                                iconRes = R.drawable.ic_lyrics,
                                tintColor = Color(0xFFBA68C8),
                                deepLinkUri = "sonza://lyrics",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            // 4. Search Tile
                            QuickActionTile(
                                label = "Search",
                                iconRes = R.drawable.ic_search,
                                tintColor = Color(0xFF38D39F),
                                deepLinkUri = "sonza://search",
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun QuickActionTile(
        label: String,
        iconRes: Int,
        tintColor: Color,
        deepLinkUri: String,
        modifier: GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .cornerRadius(16.dp)
                .background(ColorProvider(Color.White.copy(alpha = 0.08f)))
                .clickable(
                    actionRunCallback<OpenDeepLinkAction>(
                        actionParametersOf(DeepLinkKey to deepLinkUri)
                    )
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = label,
                    colorFilter = ColorFilter.tint(ColorProvider(tintColor)),
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = label,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
