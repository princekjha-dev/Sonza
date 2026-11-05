package com.sonza.app.data.repository.youtube.account

import com.sonza.app.data.SessionManager
import com.sonza.app.data.SessionManager.StoredAccount
import com.sonza.app.data.repository.youtube.internal.YouTubeApiClient
import com.sonza.app.data.repository.youtube.internal.YouTubeJsonParser
import com.sonza.app.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** Reads the signed-in account(s) out of the YouTube Music account menu. */
@Singleton
class YouTubeAccountService @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiClient: YouTubeApiClient,
    private val jsonParser: YouTubeJsonParser,
    private val networkMonitor: NetworkMonitor
) {

    suspend fun fetchAccountInfo(): StoredAccount? = withContext(Dispatchers.IO) {
        if (!networkMonitor.isCurrentlyConnected()) return@withContext null
        try {
            if (!sessionManager.isLoggedIn()) return@withContext null
            val cookies = sessionManager.getCookies() ?: return@withContext null

            val response = apiClient.fetchInternalApi("account/account_menu")
            if (response.isBlank()) return@withContext null

            // The header's position in the response varies, so search for it rather than
            // walking a fixed path.
            val accountHeader = findActiveAccountHeader(JSONObject(response)) ?: return@withContext null

            val thumbnails = accountHeader.optJSONObject("accountPhoto")?.optJSONArray("thumbnails")
            val parsedName = jsonParser.getRunText(accountHeader.optJSONObject("accountName"))
                ?: accountHeader.optJSONObject("accountName")?.optString("simpleText")
                ?: accountHeader.optString("accountName").takeIf { it.isNotBlank() }
                ?: jsonParser.getRunText(accountHeader.optJSONObject("channelHandle"))
                ?: "User"

            val parsedEmail = jsonParser.getRunText(accountHeader.optJSONObject("email"))
                ?: accountHeader.optJSONObject("email")?.optString("simpleText")
                ?: accountHeader.optString("email")

            StoredAccount(
                name = parsedName,
                email = parsedEmail,
                avatarUrl = thumbnails?.let { it.optJSONObject(it.length() - 1)?.optString("url") } ?: "",
                cookies = cookies,
                authUserIndex = sessionManager.getAuthUserIndex()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAvailableAccounts(): List<StoredAccount> = withContext(Dispatchers.IO) {
        if (!networkMonitor.isCurrentlyConnected()) return@withContext emptyList()
        try {
            if (!sessionManager.isLoggedIn()) return@withContext emptyList()
            val cookies = sessionManager.getCookies() ?: return@withContext emptyList()

            val response = apiClient.fetchInternalApi("account/account_menu")
            if (response.isBlank()) return@withContext emptyList()

            jsonParser.parseAccountMenu(JSONObject(response)).map { info ->
                StoredAccount(
                    name = info.name,
                    email = info.email,
                    avatarUrl = info.avatarUrl,
                    cookies = cookies, // All channels on the session share one cookie jar
                    authUserIndex = info.authUserIndex
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun switchAccount(account: StoredAccount) {
        sessionManager.switchAccount(account)
        // Recently-played is per-account; keeping it would attribute one user's history to
        // another.
        sessionManager.clearRecentlyPlayed()
    }

    private fun findActiveAccountHeader(node: JSONObject): JSONObject? {
        val keys = node.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = node.opt(key)

            if (key == "activeAccountHeaderRenderer" && value is JSONObject) return value

            if (value is JSONObject) {
                findActiveAccountHeader(value)?.let { return it }
            } else if (value is JSONArray) {
                for (i in 0 until value.length()) {
                    val item = value.optJSONObject(i) ?: continue
                    findActiveAccountHeader(item)?.let { return it }
                }
            }
        }
        return null
    }
}
