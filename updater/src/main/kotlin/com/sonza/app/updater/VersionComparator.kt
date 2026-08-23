package com.sonza.app.updater

/**
 * Future-ready semantic version comparator.
 *
 * Compares version strings numerically component by component:
 * - "v2.6.5.1" > "v2.6.5.0" -> true
 * - "v2.7.0" > "v2.6.5.1" -> true
 * - "v3.0.0" > "v2.7.0" -> true
 * - "2.6.9.0" < "2.6.10.0" -> true
 * - "v2.6.5.0" == "v2.6.5.0" -> true
 */
object VersionComparator {

    /**
     * Compares two version strings numerically component by component.
     * Returns:
     * - positive integer if v1 > v2
     * - negative integer if v1 < v2
     * - 0 if v1 == v2
     */
    fun compare(v1: String?, v2: String?): Int {
        if (v1.isNullOrBlank() && v2.isNullOrBlank()) return 0
        if (v1.isNullOrBlank()) return -1
        if (v2.isNullOrBlank()) return 1

        val clean1 = cleanVersion(v1)
        val clean2 = cleanVersion(v2)

        val parts1 = parseVersionParts(clean1)
        val parts2 = parseVersionParts(clean2)

        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 > p2) return 1
            if (p1 < p2) return -1
        }
        return 0
    }

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
        val cmp = compare(remoteVersionName, currentVersionName)
        if (cmp > 0) return true
        if (cmp < 0) return false

        // If semantic version names evaluate to equal, fallback to versionCode comparison if provided
        if (remoteVersionCode > 0 && currentVersionCode > 0) {
            return remoteVersionCode > currentVersionCode
        }

        return false
    }

    /**
     * Finds the immediate next available release strictly newer than [installedVersion].
     * Filters out older and equal versions, sorts in ascending semantic order, and takes the first.
     */
    fun findImmediateNextRelease(installedVersion: String, releases: List<Release>): Release? {
        return releases
            .filter { compare(it.versionName, installedVersion) > 0 }
            .sortedWith { a, b -> compare(a.versionName, b.versionName) }
            .firstOrNull()
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

