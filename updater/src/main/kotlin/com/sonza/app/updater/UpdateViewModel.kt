package com.sonza.app.updater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
    data class NoUpdate(val info: UpdateInfo) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

data class SystemUpdateUiState(
    val installedVersionName: String = "",
    val installedVersionCode: Int = 0,
    val isChecking: Boolean = false,
    val isLatest: Boolean = false,
    val hasUpdate: Boolean = false,
    val nextUpdate: UpdateInfo? = null,
    val nextReleaseNotes: Release? = null,
    val lastCheckedTimestamp: Long? = null,
    val errorMessage: String? = null,
    val allReleases: List<Release> = emptyList()
)

class UpdateViewModel @Inject constructor(
    private val checker: UpdateChecker,
    private val downloader: UpdateDownloader
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemUpdateUiState())
    val uiState: StateFlow<SystemUpdateUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _changelog = MutableStateFlow<ChangelogInfo?>(null)
    val changelog: StateFlow<ChangelogInfo?> = _changelog.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastUpdated = MutableStateFlow<Long?>(null)
    val lastUpdated: StateFlow<Long?> = _lastUpdated.asStateFlow()

    val downloadState = downloader.downloadState

    init {
        loadChangelog()
    }

    fun loadChangelog() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val info = checker.fetchChangelog()
            _changelog.value = info
            val now = System.currentTimeMillis()
            _lastUpdated.value = now
            _uiState.update { current ->
                current.copy(
                    allReleases = info?.releases ?: emptyList(),
                    lastCheckedTimestamp = current.lastCheckedTimestamp ?: now
                )
            }
            _isRefreshing.value = false
        }
    }

    fun checkForUpdate(
        currentVersionName: String = "",
        currentVersionCode: Int = 0,
        silent: Boolean = false,
        isNightly: Boolean = false
    ) {
        if (!silent && _uiState.value.isChecking) return

        val effectiveInstalled = currentVersionName.ifBlank { _uiState.value.installedVersionName }
        val effectiveCode = if (currentVersionCode > 0) currentVersionCode else _uiState.value.installedVersionCode

        viewModelScope.launch {
            if (!silent) {
                _updateState.value = UpdateState.Checking
                _uiState.update {
                    it.copy(
                        isChecking = true,
                        errorMessage = null,
                        installedVersionName = effectiveInstalled,
                        installedVersionCode = effectiveCode
                    )
                }
            }

            try {
                val updateInfoDeferred = async { checker.checkForUpdate(isNightly) }
                val changelogDeferred = async { checker.fetchChangelog() }

                val updateInfo = updateInfoDeferred.await()
                val changelogInfo = changelogDeferred.await()

                val now = System.currentTimeMillis()
                _lastUpdated.value = now
                _changelog.value = changelogInfo

                val allReleases = changelogInfo?.releases ?: emptyList()

                // Filter out older and equal versions; sort ascending; select the immediate next available version
                val immediateNextRelease = if (effectiveInstalled.isNotBlank()) {
                    VersionComparator.findImmediateNextRelease(effectiveInstalled, allReleases)
                } else null

                if (immediateNextRelease != null) {
                    val vClean = VersionComparator.cleanVersion(immediateNextRelease.versionName)
                    val matchedDownloadUrl = if (updateInfo != null && VersionComparator.cleanVersion(updateInfo.versionName) == vClean && updateInfo.downloadUrl.isNotBlank()) {
                        updateInfo.downloadUrl
                    } else {
                        "https://github.com/princekjha-dev/Sonza/releases/download/v$vClean/Sonza-v$vClean.apk"
                    }
                    val matchedSize = if (updateInfo != null && VersionComparator.cleanVersion(updateInfo.versionName) == vClean) {
                        updateInfo.size
                    } else ""
                    val matchedSha256 = if (updateInfo != null && VersionComparator.cleanVersion(updateInfo.versionName) == vClean) {
                        updateInfo.sha256
                    } else null

                    val resolvedUpdateInfo = UpdateInfo(
                        versionName = immediateNextRelease.versionName,
                        versionCode = immediateNextRelease.versionCode,
                        changelog = immediateNextRelease.description,
                        downloadUrl = matchedDownloadUrl,
                        forceUpdate = false,
                        size = matchedSize,
                        releaseNotesUrl = "https://github.com/princekjha-dev/Sonza/releases/tag/v$vClean",
                        sha256 = matchedSha256
                    )

                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            isLatest = false,
                            hasUpdate = true,
                            nextUpdate = resolvedUpdateInfo,
                            nextReleaseNotes = immediateNextRelease,
                            lastCheckedTimestamp = now,
                            errorMessage = null,
                            allReleases = allReleases,
                            installedVersionName = effectiveInstalled,
                            installedVersionCode = effectiveCode
                        )
                    }
                    _updateState.value = UpdateState.UpdateAvailable(resolvedUpdateInfo)
                } else if (updateInfo != null && VersionComparator.isNewer(updateInfo.versionName, effectiveInstalled, updateInfo.versionCode, effectiveCode)) {
                    val fallbackRelease = Release(
                        versionName = updateInfo.versionName,
                        versionCode = updateInfo.versionCode,
                        description = updateInfo.changelog
                    )
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            isLatest = false,
                            hasUpdate = true,
                            nextUpdate = updateInfo,
                            nextReleaseNotes = fallbackRelease,
                            lastCheckedTimestamp = now,
                            errorMessage = null,
                            allReleases = allReleases,
                            installedVersionName = effectiveInstalled,
                            installedVersionCode = effectiveCode
                        )
                    }
                    _updateState.value = UpdateState.UpdateAvailable(updateInfo)
                } else if (updateInfo != null || changelogInfo != null) {
                    val fallbackInfo = updateInfo ?: UpdateInfo(versionName = effectiveInstalled, downloadUrl = "")
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            isLatest = true,
                            hasUpdate = false,
                            nextUpdate = null,
                            nextReleaseNotes = null,
                            lastCheckedTimestamp = now,
                            errorMessage = null,
                            allReleases = allReleases,
                            installedVersionName = effectiveInstalled,
                            installedVersionCode = effectiveCode
                        )
                    }
                    _updateState.value = UpdateState.NoUpdate(fallbackInfo)
                } else {
                    if (!silent) {
                        _uiState.update {
                            it.copy(
                                isChecking = false,
                                errorMessage = "Couldn't check for updates. Check your internet connection and try again."
                            )
                        }
                        _updateState.value = UpdateState.Error("Could not connect to update server")
                    }
                }
            } catch (e: Exception) {
                if (!silent) {
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            errorMessage = "Couldn't check for updates. Check your internet connection and try again."
                        )
                    }
                    _updateState.value = UpdateState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }

    fun downloadAndInstallUpdate(info: UpdateInfo) {
        downloader.downloadAndInstall(info.downloadUrl, info.versionName, info.sha256)
    }

    fun triggerUpdateAvailable(
        versionCode: Int,
        versionName: String,
        currentVersionName: String = "",
        currentVersionCode: Int = 0
    ) {
        checkForUpdate(
            currentVersionName = currentVersionName,
            currentVersionCode = currentVersionCode,
            silent = true
        )
    }

    fun dismissDialog() {
        _updateState.value = UpdateState.Idle
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
        _uiState.update { it.copy(errorMessage = null) }
        downloader.resetDownloadState()
    }
}

