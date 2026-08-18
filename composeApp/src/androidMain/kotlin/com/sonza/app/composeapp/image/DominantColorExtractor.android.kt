package com.sonza.app.composeapp.image

import android.graphics.BitmapFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual class PlatformDominantColorExtractor actual constructor() {

    private val httpClient = HttpClient(CIO)

    actual suspend fun extract(imageUrl: String, isDarkTheme: Boolean): DominantColors? =
        withContext(Dispatchers.IO) {
            try {
                val bytes = httpClient.get(imageUrl).bodyAsBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
                val width = bitmap.width
                val height = bitmap.height
                val step = maxOf(1, minOf(width, height) / 10)
                var totalR = 0L
                var totalG = 0L
                var totalB = 0L
                var count = 0L
                for (x in 0 until width step step) {
                    for (y in 0 until height step step) {
                        val pixel = bitmap.getPixel(x, y)
                        totalR += (pixel shr 16) and 0xFF
                        totalG += (pixel shr 8) and 0xFF
                        totalB += pixel and 0xFF
                        count++
                    }
                }
                if (count == 0L) return@withContext null
                buildDominantColors((totalR / count).toInt(), (totalG / count).toInt(), (totalB / count).toInt(), isDarkTheme)
            } catch (t: Throwable) {
                null
            }
        }
}
