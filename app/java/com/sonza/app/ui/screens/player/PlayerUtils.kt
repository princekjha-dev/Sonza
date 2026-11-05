package com.sonza.app.ui.screens.player

import com.sonza.app.util.ImageUtils

fun getHighResThumbnail(url: String?): String? {
    return ImageUtils.getHighResThumbnailUrl(url)
}

fun formatDuration(millis: Long): String = com.sonza.app.util.TimeUtil.formatPosition(millis)

