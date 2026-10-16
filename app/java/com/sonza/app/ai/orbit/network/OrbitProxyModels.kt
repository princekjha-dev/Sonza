package com.sonza.app.ai.orbit.network

import com.google.gson.annotations.SerializedName

/**
 * Request payload sent from Sonza Orbit Android app to Cloudflare Orbit Proxy.
 * Never includes any AI provider API key.
 */
data class OrbitChatRequest(
    @SerializedName("message")
    val message: String,

    @SerializedName("conversation")
    val conversation: List<OrbitConversationMessage> = emptyList(),

    @SerializedName("model")
    val model: String? = null
)

/**
 * Single turn in conversational history sent to the proxy.
 * role must be "user", "assistant", or "system".
 */
data class OrbitConversationMessage(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)

/**
 * Response payload returned by Cloudflare Orbit Proxy.
 */
data class OrbitChatResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("response")
    val response: String? = null,

    @SerializedName("error")
    val error: OrbitChatError? = null
)

/**
 * Structured error details returned by Cloudflare Orbit Proxy.
 */
data class OrbitChatError(
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null
)

/**
 * Health check diagnostics response from GET /health.
 */
data class OrbitHealthResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("service")
    val service: String? = null
)
