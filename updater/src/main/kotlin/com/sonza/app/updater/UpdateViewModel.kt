package com.sonza.app.updater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
    data class NoUpdate(val info: UpdateInfo) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdateViewModel @Inject constructor(
    private val checker: UpdateChecker,
    private val downloader: UpdateDownloader
) : ViewModel() {

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
            _lastUpdated.value = System.currentTimeMillis()
            _isRefreshing.value = false
        }
    }

    fun checkForUpdate(
        currentVersionName: String = "",
        currentVersionCode: Int = 0,
        silent: Boolean = false,
        isNightly: Boolean = false
    ) {
        viewModelScope.launch {
            if (!silent) _updateState.value = UpdateState.Checking

            val updateJob = launch {
                val updateInfo = checker.checkForUpdate(isNightly)
                if (updateInfo != null) {
                    _lastUpdated.value = System.currentTimeMillis()
                    val isNewer = VersionComparator.isNewer(
                        remoteVersionName = updateInfo.versionName,
                        currentVersionName = currentVersionName,
                        remoteVersionCode = updateInfo.versionCode,
                        currentVersionCode = currentVersionCode
                    )
                    if (isNewer) {
                        _updateState.value = UpdateState.UpdateAvailable(updateInfo)
                    } else {
                        _updateState.value = UpdateState.NoUpdate(updateInfo)
                    }
                } else {
                    if (!silent) _updateState.value = UpdateState.Error("Could not connect to update server")
                }
            }

            val changelogJob = launch {
                loadChangelog()
            }

            updateJob.join()
            changelogJob.join()
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
        downloader.resetDownloadState()
    }
}
