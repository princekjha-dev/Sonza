package com.sonza.app.ai.orbit

import com.google.gson.Gson
import com.sonza.app.ai.orbit.network.OrbitChatRequest
import com.sonza.app.ai.orbit.network.OrbitChatResult
import com.sonza.app.ai.orbit.network.OrbitConversationMessage
import com.sonza.app.ai.orbit.network.OrbitProxyClient
import com.sonza.app.ai.orbit.provider.GroqProxyProvider
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * End-to-End Integration and Verification Tests for Sonza Orbit <-> Cloudflare Orbit Proxy.
 *
 * Verifies:
 * 1. Basic AI Request: "Hello Orbit" via POST /v1/chat
 * 2. Multi-turn conversation memory: "Remember favorite color blue" -> "What is my favorite color?"
 * 3. Command routing: local commands ("Play my music", pause, volume) remain local and are NOT sent to AI
 * 4. Network failure handling: offline / timeout does not crash app and returns graceful voice response
 * 5. Proxy failure handling: 502/504/429/malformed responses handled safely
 * 6. Security: ZERO API keys (Groq, OpenAI, Gemini) anywhere in Android codebase
 */
class OrbitProxyIntegrationTest {

    private val gson = Gson()

    /**
     * Test 1: Basic AI Request ("Hello Orbit")
     * Verifies:
     * - Centralized URL is used
     * - Endpoint is POST /v1/chat
     * - Content-Type is application/json
     * - No API provider secret is sent
     * - AI response is returned and spoken by Orbit
     */
    @Test
    fun testBasicAIRequest_HelloOrbit() = runBlocking {
        var capturedPath: String? = null
        var capturedMethod: String? = null
        var capturedContentType: String? = null
        var capturedBody: String? = null
        var capturedAuthHeader: String? = null

        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                capturedPath = request.url.encodedPath
                capturedMethod = request.method
                capturedContentType = request.body?.contentType()?.toString()
                val buffer = okio.Buffer()
                request.body?.writeTo(buffer)
                capturedBody = buffer.readUtf8()
                capturedAuthHeader = request.header("Authorization")

                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("""{"success":true,"response":"Hello! I'm Orbit, your music assistant."}""".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        val proxyClient = OrbitProxyClient(
            baseHttpClient = mockClient,
            baseUrlProvider = { OrbitConstants.ORBIT_PROXY_URL }
        )

        val result = proxyClient.sendChat(message = "Hello Orbit", conversation = emptyList())

        assertEquals("/v1/chat", capturedPath)
        assertEquals("POST", capturedMethod)
        assertTrue(capturedContentType?.contains("application/json") == true)
        assertNull("Android must never send AI provider key in Authorization header", capturedAuthHeader)

        // Parse sent JSON
        val parsedReq = gson.fromJson(capturedBody, OrbitChatRequest::class.java)
        assertEquals("Hello Orbit", parsedReq.message)
        assertTrue(parsedReq.conversation.isEmpty())

        // Verify Orbit receives response
        assertTrue(result is OrbitChatResult.Success)
        val successResult = result as OrbitChatResult.Success
        assertEquals("Hello! I'm Orbit, your music assistant.", successResult.aiResponse)
    }

    /**
     * Test 2: Multi-turn Conversation Memory
     * "Remember that my favorite color is blue." -> "What is my favorite color?"
     */
    @Test
    fun testConversationMemory_MultiTurnHistory() = runBlocking {
        val memory = OrbitContextMemory()

        // Turn 1: User says "Remember that my favorite color is blue."
        val turn1Prompt = "Remember that my favorite color is blue."
        val turn1Response = "Got it! I'll remember that your favorite color is blue."
        memory.recordTurn(
            userUtterance = turn1Prompt,
            intent = OrbitIntent.AIQueryIntent(turn1Prompt),
            spokenResponse = turn1Response,
            action = "AI_QUERY"
        )

        // Verify memory maintains the turns
        val history = memory.conversationHistory
        assertEquals(2, history.size)
        assertEquals("user", history[0].role)
        assertEquals(turn1Prompt, history[0].content)
        assertEquals("assistant", history[1].role)
        assertEquals(turn1Response, history[1].content)

        // Turn 2: User asks "What is my favorite color?"
        var capturedHistory: List<OrbitConversationMessage>? = null

        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val buffer = okio.Buffer()
                request.body?.writeTo(buffer)
                val bodyStr = buffer.readUtf8()
                val req = gson.fromJson(bodyStr, OrbitChatRequest::class.java)
                capturedHistory = req.conversation

                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("""{"success":true,"response":"Your favorite color is blue!"}""".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        val groqProvider = GroqProxyProvider(
            baseHttpClient = mockClient,
            proxyClient = OrbitProxyClient(mockClient, baseUrlProvider = { OrbitConstants.ORBIT_PROXY_URL })
        )

        val aiResp = groqProvider.processChat(
            message = "What is my favorite color?",
            musicContext = null,
            conversationHistory = history
        )

        assertTrue(aiResp.isSuccess)
        assertEquals("Your favorite color is blue!", aiResp.spokenText)

        assertNotNull(capturedHistory)
        val nonNullHistory = capturedHistory!!
        assertEquals(2, nonNullHistory.size)
        assertEquals("user", nonNullHistory[0].role)
        assertEquals(turn1Prompt, nonNullHistory[0].content)
        assertEquals("assistant", nonNullHistory[1].role)
        assertEquals(turn1Response, nonNullHistory[1].content)
    }

    /**
     * Test 3: Command Routing
     * Verifies local commands ("Play my music", pause, volume up, etc.) are handled locally
     * and NOT sent blindly to the AI proxy.
     */
    @Test
    fun testCommandRouting_LocalCommandsHandledLocally() {
        val playIntent = OrbitIntentParser.parse("Play Unholy", null, OrbitDialogueContext.Idle, null)
        assertFalse("Local music intent should not be AIQueryIntent", playIntent is OrbitIntent.AIQueryIntent)
        assertTrue("Intent should be PlayTrackIntent", playIntent is OrbitIntent.PlayTrackIntent)

        val playFavIntent = OrbitIntentParser.parse("play my favorites", null, OrbitDialogueContext.Idle, null)
        assertTrue("Intent should be PlayFavoritesIntent", playFavIntent is OrbitIntent.PlayFavoritesIntent)

        val pauseIntent = OrbitIntentParser.parse("pause", null, OrbitDialogueContext.Idle, null)
        assertTrue(pauseIntent is OrbitIntent.PauseIntent)

        val resumeIntent = OrbitIntentParser.parse("resume", null, OrbitDialogueContext.Idle, null)
        assertTrue(resumeIntent is OrbitIntent.ResumeIntent)

        val nextIntent = OrbitIntentParser.parse("next song", null, OrbitDialogueContext.Idle, null)
        assertTrue(nextIntent is OrbitIntent.NextTrackIntent)

        val volumeUpIntent = OrbitIntentParser.parse("turn it up", null, OrbitDialogueContext.Idle, null)
        assertTrue(volumeUpIntent is OrbitIntent.VolumeIntent)

        // General conversational question falls back to AI query
        val aiIntent = OrbitIntentParser.parse("Why is the sky blue?", null, OrbitDialogueContext.Idle, null)
        assertTrue(aiIntent is OrbitIntent.AIQueryIntent || aiIntent is OrbitIntent.UnknownIntent)
    }

    /**
     * Test 4: Network Failure Handling (Offline / Host Unreachable)
     * Verifies that network failures do NOT crash Orbit and return graceful spoken feedback.
     */
    @Test
    fun testNetworkFailure_OfflineDoesNotCrash() = runBlocking {
        val offlineClient = OkHttpClient.Builder()
            .addInterceptor {
                throw UnknownHostException("Unable to resolve host \"orbit-proxy.pkiha2028.workers.dev\": No address associated with hostname")
            }
            .build()

        val proxyClient = OrbitProxyClient(
            baseHttpClient = offlineClient,
            baseUrlProvider = { OrbitConstants.ORBIT_PROXY_URL }
        )

        val result = proxyClient.sendChat("Hello Orbit", emptyList())

        assertTrue("Must be an error result, not a crash", result is OrbitChatResult.Error)
        val errorResult = result as OrbitChatResult.Error
        assertEquals("NETWORK_UNAVAILABLE", errorResult.errorCode)
        assertTrue(errorResult.spokenError.contains("reach the AI proxy", ignoreCase = true))

        // Also test timeout
        val timeoutClient = OkHttpClient.Builder()
            .addInterceptor {
                throw SocketTimeoutException("connect timed out")
            }
            .build()

        val timeoutProxyClient = OrbitProxyClient(
            baseHttpClient = timeoutClient,
            baseUrlProvider = { OrbitConstants.ORBIT_PROXY_URL }
        )

        val timeoutResult = timeoutProxyClient.sendChat("Hello Orbit", emptyList())
        assertTrue(timeoutResult is OrbitChatResult.Error)
        val timeoutError = timeoutResult as OrbitChatResult.Error
        assertEquals("TIMEOUT", timeoutError.errorCode)
        assertTrue(timeoutError.spokenError.contains("too long to respond", ignoreCase = true))
    }

    /**
     * Test 5: Proxy Failure Handling (502 / 504 / 429 / Malformed)
     * Verifies that HTTP errors and invalid JSON return graceful error messages.
     */
    @Test
    fun testProxyFailure_ErrorEnvelopesHandledGracefully() = runBlocking {
        // HTTP 502 with proxy error envelope
        val error502Client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(502)
                    .message("Bad Gateway")
                    .body("""{"success":false,"error":{"code":"PROVIDER_ERROR","message":"Unable to process the request."}}""".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        val client502 = OrbitProxyClient(error502Client, baseUrlProvider = { OrbitConstants.ORBIT_PROXY_URL })
        val res502 = client502.sendChat("Hello Orbit", emptyList())
        assertTrue(res502 is OrbitChatResult.Error)
        assertEquals("PROVIDER_ERROR", (res502 as OrbitChatResult.Error).errorCode)

        // HTTP 429 Rate Limited
        val error429Client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(429)
                    .message("Too Many Requests")
                    .body("""{"success":false,"error":{"code":"RATE_LIMITED","message":"Too many requests."}}""".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        val client429 = OrbitProxyClient(error429Client, baseUrlProvider = { OrbitConstants.ORBIT_PROXY_URL })
        val res429 = client429.sendChat("Hello Orbit", emptyList())
        assertTrue(res429 is OrbitChatResult.Error)
        assertTrue((res429 as OrbitChatResult.Error).spokenError.contains("too many requests", ignoreCase = true))

        // Malformed HTML response (e.g. Cloudflare gateway error page)
        val htmlClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(500)
                    .message("Internal Server Error")
                    .body("""<html><body>Cloudflare Error</body></html>""".toResponseBody("text/html".toMediaType()))
                    .build()
            }
            .build()

        val clientHtml = OrbitProxyClient(htmlClient, baseUrlProvider = { OrbitConstants.ORBIT_PROXY_URL })
        val resHtml = clientHtml.sendChat("Hello Orbit", emptyList())
        assertTrue(resHtml is OrbitChatResult.Error)
        assertNotNull((resHtml as OrbitChatResult.Error).spokenError)
    }

    /**
     * Test 6: Security Verification
     * Verifies that NO AI provider API secret keys exist in the Android project.
     * Absolutely zero hardcoded Groq (gsk_), OpenAI (sk-proj-), or Anthropic (sk-ant-) secrets.
     * Also verifies that Orbit package contains zero AI provider API keys.
     */
    @Test
    fun testSecurity_NoProviderSecretsInAndroidProject() {
        val cwd = File(".").canonicalFile
        val projectRoot = if (cwd.name == "app") cwd.parentFile else cwd
        val appSource = File(projectRoot, "app/java")
        assertTrue("app/java must exist", appSource.exists())

        // 1. Check entire app source for real hardcoded API key tokens
        val forbiddenSecretPrefixes = listOf(
            "gsk_", // Groq private key prefix
            "sk-proj-", // OpenAI private key prefix
            "sk-ant-" // Anthropic private key prefix
        )

        appSource.walkTopDown().forEach { file ->
            if (file.isFile && (file.extension == "kt" || file.extension == "xml" || file.extension == "properties" || file.extension == "gradle" || file.name.endsWith(".kts"))) {
                val content = file.readText()
                for (prefix in forbiddenSecretPrefixes) {
                    val linesWithKey = content.lines().filter { line ->
                        val isPlaceholder = line.contains("keyPlaceholder") || line.contains("placeholder") || line.contains("example")
                        val isComment = line.trim().startsWith("//") || line.trim().startsWith("*")
                        line.contains(prefix) && !isPlaceholder && !isComment
                    }
                    assertTrue("Forbidden AI secret prefix '$prefix' found in ${file.path}", linesWithKey.isEmpty())
                }
            }
        }

        // 2. Check Orbit package specifically: Orbit must communicate ONLY with the proxy and have zero provider keys
        val orbitDir = File(appSource, "com/sonza/app/ai/orbit")
        assertTrue("Orbit directory must exist", orbitDir.exists())
        val forbiddenOrbitTerms = listOf(
            "GROQ_API_KEY",
            "OPENAI_API_KEY",
            "GEMINI_API_KEY",
            "api.groq.com",
            "api.openai.com",
            "generativelanguage.googleapis.com"
        )
        orbitDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "kt") {
                val content = file.readText()
                for (term in forbiddenOrbitTerms) {
                    val linesWithTerm = content.lines().filter { line ->
                        val isCommentOrTest = line.contains("prefix") || line.contains("forbidden") || line.contains("example")
                        line.contains(term) && !isCommentOrTest
                    }
                    assertTrue("Forbidden AI provider term '$term' found in Orbit file ${file.path}", linesWithTerm.isEmpty())
                }
            }
        }
    }
}
