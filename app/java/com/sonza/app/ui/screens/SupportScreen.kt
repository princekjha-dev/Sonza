package com.sonza.app.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.compose.koinInject

/**
 * Android host for the dedicated Send Feedback screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val okHttpClient: OkHttpClient = koinInject()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Send Feedback",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5F5F7)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFF5F5F7)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color(0xFFF5F5F7),
                    navigationIconContentColor = Color(0xFFF5F5F7)
                ),
            )
        },
    ) { paddingValues ->
        com.sonza.app.composeapp.ui.settings.SupportScreen(
            onSubmitFeedback = { message, onSuccess, onError ->
                scope.launch {
                    try {
                        val appName = "Sonza"
                        val appPackage = context.packageName
                        val appVersion = runCatching {
                            context.packageManager.getPackageInfo(context.packageName, 0).versionName
                        }.getOrDefault(com.sonza.app.BuildConfig.VERSION_NAME)

                        val deviceBrand = android.os.Build.BRAND
                        val deviceModel = android.os.Build.MODEL
                        val osVersion = "Android ${android.os.Build.VERSION.RELEASE}"
                        val sdkVersion = android.os.Build.VERSION.SDK_INT.toString()
                        val screenResolution = "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}"
                        val locale = java.util.Locale.getDefault().toString()
                        val timezone = java.util.TimeZone.getDefault().id
                        val networkType = getNetworkType(context)

                        val json = JsonObject().apply {
                            addProperty("appName", appName)
                            addProperty("appVersion", appVersion)
                            addProperty("appPackage", appPackage)
                            addProperty("rating", 5)
                            addProperty("category", "feedback")
                            addProperty("message", message)
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
                            .url("https://feedback.narutosengupta.in/api/feedback")
                            .post(requestBody)
                            .build()

                        val response = withContext(Dispatchers.IO) {
                            okHttpClient.newCall(request).execute()
                        }
                        if (response.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("Server returned ${response.code}. Please try again.")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SupportScreen", "Error submitting feedback", e)
                        onError(e.message ?: "Failed to connect to feedback server.")
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
            ),
        )
    }
}

private fun getNetworkType(context: Context): String {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return "unknown"
    val activeNetwork = cm.activeNetwork ?: return "none"
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return "unknown"
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
        else -> "other"
    }
}
