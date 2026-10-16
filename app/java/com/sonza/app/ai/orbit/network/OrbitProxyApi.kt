package com.sonza.app.ai.orbit.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Authoritative Retrofit API interface for Cloudflare Orbit Proxy.
 * Connects exclusively to the deployed proxy (default: https://orbit-proxy.pkjha2028.workers.dev).
 * Zero AI provider API keys are handled or transmitted by this interface.
 */
interface OrbitProxyApi {

    /**
     * Main Orbit voice chat endpoint.
     * POST /v1/chat
     */
    @POST("v1/chat")
    suspend fun chat(
        @Body request: OrbitChatRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-Orbit-Client-Token") clientToken: String? = null
    ): Response<OrbitChatResponse>

    /**
     * Diagnostic health check endpoint.
     * GET /health
     */
    @GET("health")
    suspend fun health(): Response<OrbitHealthResponse>
}
