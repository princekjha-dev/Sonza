package com.sonza.app.ai.orbit

/**
 * Centralized configuration for Sonza Orbit Assistant integration with Cloudflare Orbit Proxy.
 *
 * CRITICAL ARCHITECTURAL GUARANTEE:
 * The Android client contains ZERO AI provider API keys or secrets (Groq, OpenAI, Gemini, etc.).
 * The Android app communicates ONLY with the Cloudflare Orbit Proxy.
 */
object OrbitConstants {
    /**
     * Authoritative Cloudflare Orbit Proxy URL.
     * Centralized single source of truth - not scattered across the codebase.
     */
    const val ORBIT_PROXY_URL = "https://orbit-proxy.pkjha2028.workers.dev"

    const val TIMEOUT_CONNECT_SECONDS = 15L
    const val TIMEOUT_READ_SECONDS = 30L
    const val TIMEOUT_WRITE_SECONDS = 15L

    const val MAX_CONVERSATION_TURNS = 20
}
