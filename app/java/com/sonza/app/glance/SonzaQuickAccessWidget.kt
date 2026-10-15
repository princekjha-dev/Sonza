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
 * Widget 4 — Music Quick Actions.
 * Direct one-tap music access for 5 curated music streams:
 * - Liked Songs
 * - Romantic Mix
 * - Workout
 * - Discover
 * - Recently Played
 * All actions execute directly on the real music player.
 */
class SonzaQuickAccessWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(SonzaWidgetTheme.CardBackground))
                        .cornerRadius(24.dp)
                        .padding(12.dp),
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
                            Text(
                                text = "QUICK ACTIONS",
                                style = TextStyle(
                                    color = ColorProvider(SonzaWidgetTheme.AccentCyan),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Image(
                                provider = ImageProvider(R.drawable.ic_music_note),
                                contentDescription = "Sonza Music",
                                colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.TextMuted)),
                                modifier = GlanceModifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Row 1: Liked Songs + Romantic Mix (Wide Action Cards)
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickCardHero(
                                title = "Liked Songs",
                                subtitle = "Your favorites",
                                iconRes = R.drawable.ic_heart,
                                tint = SonzaWidgetTheme.AccentRed,
                                actionType = "liked",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            QuickCardHero(
                                title = "Romantic Mix",
                                subtitle = "Love & Chill",
                                iconRes = R.drawable.ic_music_note,
                                tint = SonzaWidgetTheme.AccentPink,
                                actionType = "romantic",
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Row 2: Workout + Discover + Recently Played
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickCardCompact(
                                title = "Workout",
                                iconRes = R.drawable.ic_equalizer,
                                tint = SonzaWidgetTheme.AccentOrange,
                                actionType = "workout",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            QuickCardCompact(
                                title = "Discover",
                                iconRes = R.drawable.ic_search,
                                tint = SonzaWidgetTheme.AccentCyan,
                                actionType = "discover",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            QuickCardCompact(
                                title = "Recent",
                                iconRes = R.drawable.ic_shuffle,
                                tint = SonzaWidgetTheme.AccentPurple,
                                actionType = "recent",
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun QuickCardHero(
        title: String,
        subtitle: String,
        iconRes: Int,
        tint: Color,
        actionType: String,
        modifier: GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .cornerRadius(14.dp)
                .background(ColorProvider(SonzaWidgetTheme.ControlPill))
                .clickable(
                    actionRunCallback<QuickActionCallback>(
                        actionParametersOf(QuickActionTypeKey to actionType)
                    )
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .cornerRadius(10.dp)
                        .background(ColorProvider(tint.copy(alpha = 0.20f))),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = title,
                        colorFilter = ColorFilter.tint(ColorProvider(tint)),
                        modifier = GlanceModifier.size(18.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            color = ColorProvider(SonzaWidgetTheme.TextPrimary),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            color = ColorProvider(SonzaWidgetTheme.TextMuted),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun QuickCardCompact(
        title: String,
        iconRes: Int,
        tint: Color,
        actionType: String,
        modifier: GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .cornerRadius(12.dp)
                .background(ColorProvider(SonzaWidgetTheme.ControlPill))
                .clickable(
                    actionRunCallback<QuickActionCallback>(
                        actionParametersOf(QuickActionTypeKey to actionType)
                    )
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(28.dp)
                        .cornerRadius(8.dp)
                        .background(ColorProvider(tint.copy(alpha = 0.18f))),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = title,
                        colorFilter = ColorFilter.tint(ColorProvider(tint)),
                        modifier = GlanceModifier.size(16.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = title,
                    style = TextStyle(
                        color = ColorProvider(SonzaWidgetTheme.TextSecondary),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
