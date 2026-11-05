package com.sonza.app.ai

import com.sonza.app.core.model.ChatProxyModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap

/**
 * Production-ready AI provider and model management service.
 * Handles dynamic model catalogs, API key validation, and resilient error mapping.
 */
object AIProviderManager {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // In-memory cache for dynamic model catalogs
    private val modelCache = ConcurrentHashMap<String, List<String>>()

    // Default Curated Fallback Catalogs
    val DEFAULT_GEMINI_MODELS = listOf(
        "gemini-2.0-flash",
        "gemini-1.5-pro",
        "gemini-1.5-flash",
        "gemini-2.0-pro-exp-02-05",
        "gemini-2.0-flash-lite-preview-02-05"
    )

    val DEFAULT_OPENAI_MODELS = listOf(
        "gpt-4o",
        "gpt-4o-mini",
        "o3-mini",
        "o1",
        "o1-mini",
        "gpt-4-turbo",
        "gpt-3.5-turbo"
    )

    val DEFAULT_ANTHROPIC_MODELS = listOf(
        "claude-3-5-sonnet-20241022",
        "claude-3-5-haiku-20241022",
        "claude-3-opus-20240229",
        "claude-3-5-sonnet-20240620",
        "claude-3-haiku-20240307"
    )

    /**
     * Fetches available models for Chat Proxy dynamically from worker endpoint.
     */
    suspend fun fetchChatProxyModels(forceRefresh: Boolean = false): Result<List<String>> = withContext(Dispatchers.IO) {
        val cacheKey = "CHAT_PROXY"
        if (!forceRefresh && modelCache.containsKey(cacheKey)) {
            val cached = modelCache[cacheKey]
            if (!cached.isNullOrEmpty()) return@withContext Result.success(cached)
        }

        try {
            val request = Request.Builder()
                .url("https://chatbot.codexapi.workers.dev/v1/models")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; Sonza-App)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val fallback = listOf(ChatProxyModels.RANDOM) + ChatProxyModels.ALL
                    modelCache[cacheKey] = fallback
                    return@withContext Result.success(fallback)
                }

                val body = response.body?.string()
                if (body.isNullOrBlank()) {
                    val fallback = listOf(ChatProxyModels.RANDOM) + ChatProxyModels.ALL
                    modelCache[cacheKey] = fallback
                    return@withContext Result.success(fallback)
                }

                val json = JSONObject(body)
                val dataArray = json.optJSONArray("data")
                if (dataArray != null && dataArray.length() > 0) {
                    val parsed = mutableListOf<String>()
                    parsed.add(ChatProxyModels.RANDOM)
                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)
                        val id = item.optString("id")
                        if (id.isNotBlank() && id != ChatProxyModels.RANDOM) {
                            parsed.add(id)
                        }
                    }
                    val distinctModels = parsed.distinct()
                    modelCache[cacheKey] = distinctModels
                    Result.success(distinctModels)
                } else {
                    val fallback = listOf(ChatProxyModels.RANDOM) + ChatProxyModels.ALL
                    modelCache[cacheKey] = fallback
                    Result.success(fallback)
                }
            }
        } catch (e: Exception) {
            val fallback = listOf(ChatProxyModels.RANDOM) + ChatProxyModels.ALL
            modelCache[cacheKey] = fallback
            Result.success(fallback)
        }
    }

    /**
     * Fetches models dynamically for Gemini if API key is provided.
     */
    suspend fun fetchGeminiModels(apiKey: String, forceRefresh: Boolean = false): Result<List<String>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.success(DEFAULT_GEMINI_MODELS)
        }

        val cacheKey = "GEMINI_${apiKey.hashCode()}"
        if (!forceRefresh && modelCache.containsKey(cacheKey)) {
            val cached = modelCache[cacheKey]
            if (!cached.isNullOrEmpty()) return@withContext Result.success(cached)
        }

        try {
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models")
                .header("x-goog-api-key", apiKey)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(mapHttpError(response.code, "Gemini"))
                }
                val body = response.body?.string() ?: return@withContext Result.success(DEFAULT_GEMINI_MODELS)
                val json = JSONObject(body)
                val modelsArray = json.optJSONArray("models")
                if (modelsArray != null && modelsArray.length() > 0) {
                    val list = mutableListOf<String>()
                    for (i in 0 until modelsArray.length()) {
                        val obj = modelsArray.getJSONObject(i)
                        val name = obj.optString("name").removePrefix("models/")
                        val methods = obj.optJSONArray("supportedGenerationMethods")
                        val supportsGenerate = if (methods != null) {
                            (0 until methods.length()).any { methods.getString(it) == "generateContent" }
                        } else true

                        if (supportsGenerate && name.isNotBlank() && !name.contains("embedding") && !name.contains("aqa")) {
                            list.add(name)
                        }
                    }
                    val result = if (list.isNotEmpty()) list.distinct() else DEFAULT_GEMINI_MODELS
                    modelCache[cacheKey] = result
                    Result.success(result)
                } else {
                    Result.success(DEFAULT_GEMINI_MODELS)
                }
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkError(e))
        }
    }

    /**
     * Fetches models dynamically for OpenAI if API key is provided.
     */
    suspend fun fetchOpenAiModels(apiKey: String, forceRefresh: Boolean = false): Result<List<String>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.success(DEFAULT_OPENAI_MODELS)
        }

        val cacheKey = "OPENAI_${apiKey.hashCode()}"
        if (!forceRefresh && modelCache.containsKey(cacheKey)) {
            val cached = modelCache[cacheKey]
            if (!cached.isNullOrEmpty()) return@withContext Result.success(cached)
        }

        try {
            val request = Request.Builder()
                .url("https://api.openai.com/v1/models")
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(mapHttpError(response.code, "OpenAI"))
                }
                val body = response.body?.string() ?: return@withContext Result.success(DEFAULT_OPENAI_MODELS)
                val json = JSONObject(body)
                val dataArray = json.optJSONArray("data")
                if (dataArray != null && dataArray.length() > 0) {
                    val list = mutableListOf<String>()
                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        val id = obj.optString("id")
                        if (id.startsWith("gpt-") || id.startsWith("o1") || id.startsWith("o3") || id.startsWith("chatgpt")) {
                            if (!id.contains("realtime") && !id.contains("audio") && !id.contains("transcribe")) {
                                list.add(id)
                            }
                        }
                    }
                    val sorted = list.sortedDescending().distinct()
                    val result = if (sorted.isNotEmpty()) sorted else DEFAULT_OPENAI_MODELS
                    modelCache[cacheKey] = result
                    Result.success(result)
                } else {
                    Result.success(DEFAULT_OPENAI_MODELS)
                }
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkError(e))
        }
    }

    /**
     * Fetches models dynamically for Anthropic if API key is provided.
     */
    suspend fun fetchAnthropicModels(apiKey: String, forceRefresh: Boolean = false): Result<List<String>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.success(DEFAULT_ANTHROPIC_MODELS)
        }

        val cacheKey = "ANTHROPIC_${apiKey.hashCode()}"
        if (!forceRefresh && modelCache.containsKey(cacheKey)) {
            val cached = modelCache[cacheKey]
            if (!cached.isNullOrEmpty()) return@withContext Result.success(cached)
        }

        try {
            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/models")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val dataArray = json.optJSONArray("data")
                        if (dataArray != null && dataArray.length() > 0) {
                            val list = mutableListOf<String>()
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                val id = obj.optString("id")
                                if (id.isNotBlank()) list.add(id)
                            }
                            if (list.isNotEmpty()) {
                                val result = list.distinct()
                                modelCache[cacheKey] = result
                                return@withContext Result.success(result)
                            }
                        }
                    }
                } else if (response.code == 401 || response.code == 403) {
                    return@withContext Result.failure(mapHttpError(response.code, "Anthropic"))
                }
                // If models endpoint is not enabled for tier, use curated list
                modelCache[cacheKey] = DEFAULT_ANTHROPIC_MODELS
                Result.success(DEFAULT_ANTHROPIC_MODELS)
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkError(e))
        }
    }

    /**
     * Validates an API key against the specified provider.
     */
    suspend fun validateApiKey(provider: String, apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API key cannot be blank."))
        }

        when (provider.uppercase()) {
            "GEMINI" -> {
                try {
                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models?pageSize=1")
                        .header("x-goog-api-key", apiKey)
                        .get()
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            Result.success(true)
                        } else {
                            Result.failure(mapHttpError(response.code, "Gemini"))
                        }
                    }
                } catch (e: Exception) {
                    Result.failure(mapNetworkError(e))
                }
            }
            "OPENAI" -> {
                try {
                    val request = Request.Builder()
                        .url("https://api.openai.com/v1/models")
                        .header("Authorization", "Bearer $apiKey")
                        .get()
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            Result.success(true)
                        } else {
                            Result.failure(mapHttpError(response.code, "OpenAI"))
                        }
                    }
                } catch (e: Exception) {
                    Result.failure(mapNetworkError(e))
                }
            }
            "ANTHROPIC" -> {
                try {
                    // Probe with lightweight model or models list
                    val request = Request.Builder()
                        .url("https://api.anthropic.com/v1/models")
                        .header("x-api-key", apiKey)
                        .header("anthropic-version", "2023-06-01")
                        .get()
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            Result.success(true)
                        } else if (response.code == 401 || response.code == 403) {
                            Result.failure(mapHttpError(response.code, "Anthropic"))
                        } else {
                            // Any non-auth response (e.g. 400/404 on endpoint) means credentials passed gateway
                            Result.success(true)
                        }
                    }
                } catch (e: Exception) {
                    Result.failure(mapNetworkError(e))
                }
            }
            "CHAT_PROXY" -> Result.success(true)
            else -> Result.failure(IllegalArgumentException("Unknown provider: $provider"))
        }
    }

    private fun mapHttpError(code: Int, provider: String): Exception {
        val message = when (code) {
            401 -> "Invalid $provider API key. Please check your credentials."
            403 -> "Access forbidden. Ensure your $provider API key has appropriate permissions."
            404 -> "$provider model or endpoint not found."
            429 -> "Rate limit reached or quota exceeded for $provider."
            500, 502, 503, 504 -> "$provider servers are temporarily unavailable ($code). Try again later."
            else -> "$provider returned HTTP error $code."
        }
        return Exception(message)
    }

    private fun mapNetworkError(e: Exception): Exception {
        val message = when (e) {
            is SocketTimeoutException -> "Connection timed out. Please check your internet connection."
            is IOException -> "Network error: Unable to reach AI server."
            else -> e.message ?: "An unexpected error occurred."
        }
        return Exception(message)
    }
}
