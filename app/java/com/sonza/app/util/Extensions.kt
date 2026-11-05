package com.sonza.app.util

import java.net.URLEncoder

fun String.encodeUrl(): String {
    return URLEncoder.encode(this, "UTF-8")
}

fun String.toHighResImage(): String {
    // RemoteAudio images: replace resolution suffix for higher quality
    return this.replace("150x150", "500x500")
        .replace("50x50", "500x500")
}

fun String.decodeHtml(): String {
    return this.replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#039;", "'")
        .replace("&apos;", "'")
}

/**
 * Safely unwrap Context to find an enclosing Activity, traversing any ContextWrappers.
 * Returns null if the context is a Service, Application, or other non-Activity context.
 */
fun android.content.Context.findActivity(): android.app.Activity? {
    var ctx: android.content.Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
