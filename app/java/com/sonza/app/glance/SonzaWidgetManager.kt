package com.sonza.app.glance

import android.content.Context
import androidx.glance.appwidget.updateAll

object SonzaWidgetManager {
    suspend fun updateAllWidgets(context: Context) {
        try {
            SonzaWidget().updateAll(context)
        } catch (_: Exception) {}

        try {
            SonzaCompactWidget().updateAll(context)
        } catch (_: Exception) {}

        try {
            SonzaStandardWidget().updateAll(context)
        } catch (_: Exception) {}

        try {
            SonzaLargeWidget().updateAll(context)
        } catch (_: Exception) {}

        try {
            SonzaQuickAccessWidget().updateAll(context)
        } catch (_: Exception) {}
    }
}
