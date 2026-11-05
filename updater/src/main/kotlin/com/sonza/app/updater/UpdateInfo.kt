package com.sonza.app.updater

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    val versionName: String,
    val versionCode: Int = 0,
    val changelog: String = "",
    val downloadUrl: String,
    val forceUpdate: Boolean = false,
    val size: String = "",
    val releaseNotesUrl: String = "",
    /** Optional hex SHA-256 of the APK. When present, UpdateDownloader aborts
     *  install on mismatch — defends against MITM swap of the downloaded file. */
    val sha256: String? = null
)

@Serializable
data class ChangelogInfo(
    val releases: List<Release> = emptyList()
)

@Serializable
data class Release(
    val versionName: String,
    val versionCode: Int = 0,
    val date: String = "",
    val description: String = "",
    val isMajorUpdate: Boolean = false
)

@Serializable
internal data class GitHubRelease(
    val tag_name: String = "",
    val name: String? = null,
    val body: String? = null,
    val prerelease: Boolean = false,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
internal data class GitHubAsset(
    val name: String = "",
    val size: Long = 0L,
    val browser_download_url: String = ""
)
