package com.sonza.app.glance

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sonza.app.R

/**
 * Widget 3 — Dedicated Orbit AI Assistant Widget.
 * Shows ORBIT, "Hey Orbit", with Orbit avatar.
 * Tap activates Orbit assistant directly in Sonza (no Google voice UI).
 */
class SonzaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFF141520)))
                        .cornerRadius(24.dp)
                        .clickable(
                            actionRunCallback<OpenDeepLinkAction>(
                                actionParametersOf(DeepLinkKey to "sonza://orbit_listen")
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Header: Status dot + ORBIT
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(7.dp)
                                    .cornerRadius(3.5.dp)
                                    .background(ColorProvider(Color(0xFF38BDF8)))
                            ) {}
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = "ORBIT",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF38BDF8)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Orbit Avatar Glowing Emblem
                        Box(
                            modifier = GlanceModifier
                                .size(56.dp)
                                .cornerRadius(28.dp)
                                .background(ColorProvider(Color(0xFF38BDF8).copy(alpha = 0.18f))),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(42.dp)
                                    .cornerRadius(21.dp)
                                    .background(ColorProvider(Color(0xFF38BDF8).copy(alpha = 0.35f))),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_mic),
                                    contentDescription = "Orbit Avatar",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // "Hey Orbit" Activation Tag
                        Text(
                            text = "“Hey Orbit”",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        Text(
                            text = "Tap to speak",
                            style = TextStyle(
                                color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}
