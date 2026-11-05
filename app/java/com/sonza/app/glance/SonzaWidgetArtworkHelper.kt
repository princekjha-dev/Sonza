package com.sonza.app.glance

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shared memory cache and artwork loader for all Sonza widgets.
 * Prevents RemoteViews bitmap recycling crashes, avoids redundant network queries,
 * and eliminates placeholder flicker on widget updates.
 */
object SonzaWidgetArtworkHelper {
    // Retain up to 25 cached bitmaps in memory for instant widget binding without GC recycling issues
    private val memoryCache = object : LruCache<String, Bitmap>(25) {}

    fun getCachedArtwork(url: String?, targetSize: Int): Bitmap? {
        if (url.isNullOrBlank()) return null
        val cacheKey = "${url}_$targetSize"
        val cached = memoryCache.get(cacheKey)
        return if (cached != null && !cached.isRecycled) cached else null
    }

    suspend fun loadArtwork(context: Context, url: String?, targetSize: Int): Bitmap? {
        if (url.isNullOrBlank()) return null
        val cacheKey = "${url}_$targetSize"

        val existing = memoryCache.get(cacheKey)
        if (existing != null && !existing.isRecycled) {
            return existing
        }

        return withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context.applicationContext)
                    .data(url)
                    .size(targetSize, targetSize)
                    .allowHardware(false)
                    .build()

                val result = context.applicationContext.imageLoader.execute(request)
                if (result is SuccessResult) {
                    val original = result.image.toBitmap()
                    // Create an un-recycled software ARGB_8888 bitmap copy for RemoteViews
                    val bitmapCopy = original.copy(Bitmap.Config.ARGB_8888, false)
                    memoryCache.put(cacheKey, bitmapCopy)
                    bitmapCopy
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
