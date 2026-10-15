package com.sonza.app.glance

import android.content.Context
import androidx.compose.runtime.remember
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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
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

class SonzaQuickAccessWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaQuickAccessWidget()
}

/**
 * Widget 4 — Quick Actions Widget.
 * Provides 5 real quick playback actions:
 * 1. Play Liked Songs
 * 2. Romantic Mix
 * 3. Workout
 * 4. Discover
 * 5. Recently Played
 * Each action directly executes playback in Sonza via deep link routing.
 */
class SonzaQuickAccessWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFF141520)))
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
                                    color = ColorProvider(Color(0xFF38BDF8)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())

                            // User Profile Avatar (Synchronized with App Profile)
                            val avatarId = remember {
                                try {
                                    val prefs = context.getSharedPreferences("sonza_user_avatar", Context.MODE_PRIVATE)
                                    prefs.getString("selected_avatar_id", "avatar_1") ?: "avatar_1"
                                } catch (_: Exception) {
                                    "avatar_1"
                                }
                            }
                            val userAvatar = com.sonza.app.ui.components.avatar.AvatarRepository.getAvatarById(avatarId)

                            Box(
                                modifier = GlanceModifier
                                    .size(24.dp)
                                    .cornerRadius(12.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.12f)))
                                    .clickable(
                                        actionRunCallback<OpenDeepLinkAction>(
                                            actionParametersOf(DeepLinkKey to "sonza://profile")
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(userAvatar.drawableRes),
                                    contentDescription = userAvatar.name,
                                    modifier = GlanceModifier.size(22.dp).cornerRadius(11.dp)
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Row 1: Play Liked Songs + Romantic Mix
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickActionItem(
                                label = "Liked Songs",
                                iconRes = R.drawable.ic_heart,
                                tintColor = Color(0xFFFF5252),
                                actionType = "liked",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            QuickActionItem(
                                label = "Romantic Mix",
                                iconRes = R.drawable.ic_music_note,
                                tintColor = Color(0xFFFF4081),
                                actionType = "romantic",
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        // Row 2: Workout + Discover + Recently Played
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickActionItem(
                                label = "Workout",
                                iconRes = R.drawable.ic_equalizer,
                                tintColor = Color(0xFFFF9100),
                                actionType = "workout",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            QuickActionItem(
                                label = "Discover",
                                iconRes = R.drawable.ic_search,
                                tintColor = Color(0xFF00E5FF),
                                actionType = "discover",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            QuickActionItem(
                                label = "Recent",
                                iconRes = R.drawable.ic_shuffle,
                                tintColor = Color(0xFFB388FF),
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
    private fun QuickActionItem(
        label: String,
        iconRes: Int,
        tintColor: Color,
        actionType: String,
        modifier: GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .cornerRadius(14.dp)
                .background(ColorProvider(Color.White.copy(alpha = 0.08f)))
                .clickable(
                    actionRunCallback<OpenDeepLinkAction>(
                        actionParametersOf(DeepLinkKey to "sonza://quick_action?type=$actionType")
                    )
                )
                .padding(6.dp),
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
                    modifier = GlanceModifier.size(20.dp)
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
