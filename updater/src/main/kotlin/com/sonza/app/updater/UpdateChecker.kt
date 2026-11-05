package com.sonza.app.updater

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request

class UpdateChecker(private val client: OkHttpClient) {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://raw.githubusercontent.com/princekjha-dev/Sonza/main/updater"
    private val githubApiUrl = "https://api.github.com/repos/princekjha-dev/Sonza/releases/latest"

    private suspend fun <T> fetchJson(url: String, serializer: kotlinx.serialization.KSerializer<T>): T? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$url${if (url.contains("?")) "&" else "?"}t=${System.currentTimeMillis()}")
            .header("User-Agent", "Sonza-Updater")
            .header("Accept", "application/json")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                json.decodeFromString(serializer, body)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkForUpdate(isNightly: Boolean = false): UpdateInfo? {
        val fileName = if (isNightly) "nightly.json" else "update.json"
        val directUpdate = fetchJson("$baseUrl/$fileName", UpdateInfo.serializer())
        if (directUpdate != null && directUpdate.downloadUrl.isNotBlank()) {
            return directUpdate
        }

        // Fallback: Check GitHub Releases API for official release assets
        val ghRelease = fetchJson(githubApiUrl, GitHubRelease.serializer())
        if (ghRelease != null && ghRelease.tag_name.isNotBlank()) {
            val apkAsset = ghRelease.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            if (apkAsset != null && apkAsset.browser_download_url.isNotBlank()) {
                val sizeFormatted = if (apkAsset.size > 0) {
                    val mb = apkAsset.size / (1024f * 1024f)
                    String.format(java.util.Locale.US, "%.1f MB", mb)
                } else ""

                return UpdateInfo(
                    versionName = ghRelease.tag_name.removePrefix("v").removePrefix("V"),
                    versionCode = 0,
                    changelog = ghRelease.body ?: "",
                    downloadUrl = apkAsset.browser_download_url,
                    forceUpdate = false,
                    size = sizeFormatted,
                    releaseNotesUrl = "https://github.com/princekjha-dev/Sonza/releases/tag/${ghRelease.tag_name}"
                )
            }
        }

        return null
    }

    suspend fun fetchChangelog(): ChangelogInfo? = fetchJson("$baseUrl/changelog.json", ChangelogInfo.serializer())
}
