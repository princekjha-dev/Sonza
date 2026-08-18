package com.sonza.app.newpipe

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response

/**
 * OkHttp-backed implementation of NewPipe's [Downloader] for Android.
 */
class NewPipeDownloaderImpl(
    private val client: OkHttpClient,
    private val cookieProvider: () -> String = { "" }
) : Downloader() {

    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = okhttp3.Request.Builder().url(url)

        var hasUserAgent = false
        for ((headerName, headerValueList) in headers) {
            if (headerName.equals("User-Agent", ignoreCase = true)) {
                hasUserAgent = true
            }
            for (value in headerValueList) {
                requestBuilder.addHeader(headerName, value)
            }
        }

        if (!hasUserAgent) {
            requestBuilder.header("User-Agent", DEFAULT_USER_AGENT)
        }

        val cookies = cookieProvider()
        if (cookies.isNotBlank() && headers["Cookie"] == null) {
            requestBuilder.addHeader("Cookie", cookies)
        }

        val requestBody = dataToSend?.toRequestBody(null)
        requestBuilder.method(httpMethod, requestBody)

        val okHttpResponse = client.newCall(requestBuilder.build()).execute()
        val responseBody = okHttpResponse.body?.string().orEmpty()
        val responseHeaders = okHttpResponse.headers.toMultimap()

        return Response(
            okHttpResponse.code,
            okHttpResponse.message,
            responseHeaders,
            responseBody,
            okHttpResponse.request.url.toString()
        )
    }

    companion object {
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
}
