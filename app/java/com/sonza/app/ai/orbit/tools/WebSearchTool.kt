package com.sonza.app.ai.orbit.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Provider-independent Web Search & Knowledge Retrieval Tool.
 *
 * Uses free, public, non-secret endpoints (DuckDuckGo Instant Answers & Wikipedia API)
 * to answer live informational questions without exposing any API keys.
 */
class WebSearchTool(private val baseHttpClient: OkHttpClient) {

    private val httpClient: OkHttpClient by lazy {
        baseHttpClient.newBuilder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    suspend fun search(query: String): String = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext "What would you like me to look up?"

        // 1. Try DuckDuckGo Instant Answer API
        try {
            val encodedQuery = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.toString())
            val ddgUrl = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"

            val request = Request.Builder()
                .url(ddgUrl)
                .header("User-Agent", "Sonza-Orbit-Assistant/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                if (body.isNotBlank()) {
                    val json = JSONObject(body)
                    val abstractText = json.optString("AbstractText", "")
                    if (abstractText.isNotBlank()) {
                        return@withContext abstractText
                    }
                    val heading = json.optString("Heading", "")
                    val answer = json.optString("Answer", "")
                    if (answer.isNotBlank()) {
                        return@withContext if (heading.isNotBlank()) "$heading: $answer" else answer
                    }
                }
            }
        } catch (_: Exception) {
            // Fall through to Wikipedia
        }

        // 2. Try Wikipedia Summary API
        try {
            val encodedQuery = URLEncoder.encode(trimmed.replace(" ", "_"), StandardCharsets.UTF_8.toString())
            val wikiUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/$encodedQuery"

            val request = Request.Builder()
                .url(wikiUrl)
                .header("User-Agent", "Sonza-Orbit-Assistant/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                if (body.isNotBlank()) {
                    val json = JSONObject(body)
                    val extract = json.optString("extract", "")
                    if (extract.isNotBlank()) {
                        // Take the first 2 sentences for natural speech
                        val sentences = extract.split(Regex("(?<=[.!?])\\s+"))
                        return@withContext sentences.take(2).joinToString(" ")
                    }
                }
            }
        } catch (_: Exception) {
            // Handled below
        }

        "I couldn't find live information for \"$trimmed\" right now."
    }
}
