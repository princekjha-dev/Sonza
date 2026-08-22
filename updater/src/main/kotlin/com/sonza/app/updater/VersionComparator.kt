package com.sonza.app.updater

/**
 * Future-ready semantic version comparator.
 *
 * Compares version strings numerically component by component:
 * - "v2.6.5.1" > "v2.6.5.0" -> true
 * - "v2.7.0" > "v2.6.5.1" -> true
 * - "v3.0.0" > "v2.7.0" -> true
 * - "v2.6.5.0" > "v2.6.5.0" -> false
 * - "v2.6.5.0" > "v2.6.5.1" -> false
 */
object VersionComparator {

    /**
     * Determines whether the remote release is strictly newer than the currently installed version.
     *
     * @param remoteVersionName Version name of the available remote update (e.g. "v2.6.5.1", "2.7.0")
     * @param currentVersionName Version name of the currently installed app (e.g. "v2.6.5.0", "2.6.5.0")
     * @param remoteVersionCode Optional remote version code
     * @param currentVersionCode Optional current version code
     * @return true if remote is newer, false otherwise
     */
    fun isNewer(
        remoteVersionName: String?,
        currentVersionName: String?,
        remoteVersionCode: Int = 0,
        currentVersionCode: Int = 0
    ): Boolean {
        if (remoteVersionName.isNullOrBlank()) {
            return remoteVersionCode > 0 && currentVersionCode > 0 && remoteVersionCode > currentVersionCode
        }
        if (currentVersionName.isNullOrBlank()) {
            return true
        }

        val remoteClean = cleanVersion(remoteVersionName)
        val currentClean = cleanVersion(currentVersionName)

        val remoteParts = parseVersionParts(remoteClean)
        val currentParts = parseVersionParts(currentClean)

        if (remoteParts.isNotEmpty() && currentParts.isNotEmpty()) {
            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        }

        // If semantic version names evaluate to equal, fallback to versionCode comparison if provided
        if (remoteVersionCode > 0 && currentVersionCode > 0) {
            return remoteVersionCode > currentVersionCode
        }

        return false
    }

    /**
     * Strips leading "v"/"V", whitespace, and suffixes like "-beta", "-release" for numeric comparison.
     */
    fun cleanVersion(version: String): String {
        return version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .substringBefore("+")
            .trim()
    }

    /**
     * Parses numeric parts separated by dots, e.g. "2.6.5.1" -> [2, 6, 5, 1].
     */
    private fun parseVersionParts(version: String): List<Int> {
        return version.split(".")
            .mapNotNull { it.trim().toIntOrNull() }
    }
}
