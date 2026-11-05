package com.sonza.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.gson.JsonObject
import com.sonza.app.BuildConfig
import com.sonza.app.data.FeedbackConfig
import com.sonza.app.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

sealed interface FeedbackResult {
    data object Success : FeedbackResult
    data class Error(val userFacingMessage: String) : FeedbackResult
}

@Singleton
class FeedbackRepository @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "FeedbackRepository"
        const val GENERIC_ERROR_MESSAGE = "Unable to send feedback right now. Please try again."
    }

    suspend fun submitFeedback(
        message: String,
        context: Context
    ): FeedbackResult = withContext(Dispatchers.IO) {
        val trimmedMessage = message.trim()
        if (trimmedMessage.isBlank()) {
            return@withContext FeedbackResult.Error("Please enter your feedback before sending.")
        }

        val endpointUrl = FeedbackConfig.getFeedbackUrl()
        if (!FeedbackConfig.isValidEndpoint(endpointUrl)) {
            AppLog.e(TAG, "Configured feedback endpoint is invalid: $endpointUrl")
            return@withContext FeedbackResult.Error(GENERIC_ERROR_MESSAGE)
        }

        try {
            val appName = "Sonza"
            val appPackage = context.packageName
            val appVersion = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrDefault(BuildConfig.VERSION_NAME)

            val deviceBrand = Build.BRAND.orEmpty()
            val deviceModel = Build.MODEL.orEmpty()
            val osVersion = "Android ${Build.VERSION.RELEASE}"
            val sdkVersion = Build.VERSION.SDK_INT.toString()
            val screenResolution = runCatching {
                val dm = context.resources.displayMetrics
                "${dm.widthPixels}x${dm.heightPixels}"
            }.getOrDefault("unknown")
            val locale = Locale.getDefault().toString()
            val timezone = TimeZone.getDefault().id
            val networkType = getNetworkType(context)

            val json = JsonObject().apply {
                addProperty("appName", appName)
                addProperty("appVersion", appVersion)
                addProperty("appPackage", appPackage)
                addProperty("rating", 5)
                addProperty("category", "feedback")
                addProperty("message", trimmedMessage)
                addProperty("deviceBrand", deviceBrand)
                addProperty("deviceModel", deviceModel)
                addProperty("osVersion", osVersion)
                addProperty("sdkVersion", sdkVersion)
                addProperty("screenResolution", screenResolution)
                addProperty("locale", locale)
                addProperty("timezone", timezone)
                addProperty("networkType", networkType)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(endpointUrl)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.use { resp ->
                if (resp.isSuccessful) {
                    FeedbackResult.Success
                } else {
                    AppLog.e(TAG, "Server responded with HTTP error code: ${resp.code}")
                    FeedbackResult.Error(GENERIC_ERROR_MESSAGE)
                }
            }
        } catch (e: Throwable) {
            // Log securely for diagnostic purposes without leaking technical details to the UI
            AppLog.e(TAG, "Failed to submit feedback", e)
            FeedbackResult.Error(GENERIC_ERROR_MESSAGE)
        }
    }

    private fun getNetworkType(context: Context): String {
        return runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return "unknown"
            val activeNetwork = cm.activeNetwork ?: return "none"
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return "unknown"
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                else -> "other"
            }
        }.getOrDefault("unknown")
    }
}
