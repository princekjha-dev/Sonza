package com.sonza.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sonza.app.core.model.UpdateChannel
import com.sonza.app.data.SessionManager
import com.sonza.app.updater.UpdateChecker
import com.sonza.app.updater.VersionComparator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PeriodicUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateChecker: UpdateChecker,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("PeriodicUpdateWorker", "Checking for updates in background...")

        return try {
            val updateChannel = sessionManager.getUpdateChannel()
            val isNightly = updateChannel == UpdateChannel.NIGHTLY
            val updateInfo = updateChecker.checkForUpdate(isNightly)

            if (updateInfo != null) {
                val currentVersionCode = getVersionCode().toInt()
                val currentVersionName = getVersionName()

                val isNewer = VersionComparator.isNewer(
                    remoteVersionName = updateInfo.versionName,
                    currentVersionName = currentVersionName,
                    remoteVersionCode = updateInfo.versionCode,
                    currentVersionCode = currentVersionCode
                )

                if (isNewer) {
                    Log.i("PeriodicUpdateWorker", "New update available: ${updateInfo.versionName}")
                    sessionManager.setPendingUpdateInfo(updateInfo.versionCode, updateInfo.versionName)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("PeriodicUpdateWorker", "Update check failed (attempt ${runAttemptCount})", e)
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private fun getVersionCode(): Long {
        return try {
            val pInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun getVersionName(): String {
        return try {
            val pInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            pInfo.versionName ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
