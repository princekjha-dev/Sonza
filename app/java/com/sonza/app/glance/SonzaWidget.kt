package com.sonza.app.glance

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sonza.app.R

/**
 * Widget 3 — Dedicated Orbit AI Assistant Widget.
 * Clean, independent AI assistant widget.
 * Features the signature glowing Orbit emblem, "Hey Orbit" prompt, and tap-to-activate voice interaction.
 * Strictly separates assistant interactions from standard music widgets.
 */
class SonzaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFF0D0E16)))
                        .cornerRadius(24.dp)
                        .clickable(
                            actionRunCallback<OpenDeepLinkAction>(
                                actionParametersOf(DeepLinkKey to "sonza://orbit_listen")
                            )
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Header: Glowing indicator dot + "ORBIT" branding
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(7.dp)
                                    .cornerRadius(3.5.dp)
                                    .background(ColorProvider(SonzaWidgetTheme.AccentCyan))
                            ) {}
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = "ORBIT AI",
                                style = TextStyle(
                                    color = ColorProvider(SonzaWidgetTheme.AccentCyan),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Glowing Orbit Avatar Emblem (layered concentric rings)
                        Box(
                            modifier = GlanceModifier
                                .size(62.dp)
                                .cornerRadius(31.dp)
                                .background(ColorProvider(SonzaWidgetTheme.AccentCyan.copy(alpha = 0.14f))),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(48.dp)
                                    .cornerRadius(24.dp)
                                    .background(ColorProvider(SonzaWidgetTheme.AccentCyan.copy(alpha = 0.30f))),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(36.dp)
                                        .cornerRadius(18.dp)
                                        .background(ColorProvider(SonzaWidgetTheme.AccentCyan.copy(alpha = 0.55f))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_mic),
                                        contentDescription = "Activate Orbit Assistant",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                        modifier = GlanceModifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Wake Phrase Prompt
                        Text(
                            text = "“Hey Orbit”",
                            style = TextStyle(
                                color = ColorProvider(SonzaWidgetTheme.TextPrimary),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        // Subtitle
                        Text(
                            text = "Tap to speak",
                            style = TextStyle(
                                color = ColorProvider(SonzaWidgetTheme.TextSecondary),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}
