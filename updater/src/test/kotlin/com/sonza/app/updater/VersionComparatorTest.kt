package com.sonza.app.updater

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun testSemanticPatchVersionIncrement() {
        // v2.6.5.1 > v2.6.5.0
        assertTrue(VersionComparator.isNewer("v2.6.5.1", "v2.6.5.0"))
        assertTrue(VersionComparator.isNewer("2.6.5.1", "2.6.5.0"))
        assertTrue(VersionComparator.isNewer("v2.6.5.2", "v2.6.5.1"))
    }

    @Test
    fun testSemanticMinorVersionIncrement() {
        // v2.7.0 > v2.6.5.1
        assertTrue(VersionComparator.isNewer("v2.7.0", "v2.6.5.1"))
        assertTrue(VersionComparator.isNewer("2.7.0", "2.6.5.1"))
        assertTrue(VersionComparator.isNewer("v2.7.0.0", "v2.6.5.1"))
    }

    @Test
    fun testSemanticMajorVersionIncrement() {
        // v3.0.0 > v2.7.0
        assertTrue(VersionComparator.isNewer("v3.0.0", "v2.7.0"))
        assertTrue(VersionComparator.isNewer("3.0.0.0", "2.7.0.5"))
    }

    @Test
    fun testEqualOrOlderVersionsDoNotTriggerUpdate() {
        // Same version
        assertFalse(VersionComparator.isNewer("v2.6.5.0", "v2.6.5.0"))
        assertFalse(VersionComparator.isNewer("2.6.5.1", "v2.6.5.1"))
        assertFalse(VersionComparator.isNewer("v2.7.0", "2.7.0"))

        // Older remote version
        assertFalse(VersionComparator.isNewer("v2.6.5.0", "v2.6.5.1"))
        assertFalse(VersionComparator.isNewer("v2.6.5", "v2.7.0"))
        assertFalse(VersionComparator.isNewer("v1.9.9", "v2.0.0"))
    }

    @Test
    fun testVersionCodeFallbackWhenNamesMatch() {
        // Same name, higher remote code
        assertTrue(VersionComparator.isNewer("v2.6.5.0", "v2.6.5.0", remoteVersionCode = 2651, currentVersionCode = 2650))
        // Same name, equal or lower remote code
        assertFalse(VersionComparator.isNewer("v2.6.5.0", "v2.6.5.0", remoteVersionCode = 2650, currentVersionCode = 2650))
        assertFalse(VersionComparator.isNewer("v2.6.5.0", "v2.6.5.0", remoteVersionCode = 2649, currentVersionCode = 2650))
    }

    @Test
    fun testPreReleaseSuffixes() {
        assertTrue(VersionComparator.isNewer("v2.6.5.1-beta", "v2.6.5.0"))
        assertFalse(VersionComparator.isNewer("v2.6.5.0-rc1", "v2.6.5.0"))
    }

    @Test
    fun testMultiDigitSemanticComparison() {
        // 2.6.9.0 < 2.6.10.0 must be true (not lexicographical)
        assertTrue(VersionComparator.compare("2.6.10.0", "2.6.9.0") > 0)
        assertTrue(VersionComparator.compare("2.6.9.0", "2.6.10.0") < 0)
        assertTrue(VersionComparator.isNewer("2.6.10.0", "2.6.9.0"))
        assertFalse(VersionComparator.isNewer("2.6.9.0", "2.6.10.0"))
    }

    @Test
    fun testFindImmediateNextRelease() {
        val releases = listOf(
            Release(versionName = "2.6.2.0"),
            Release(versionName = "2.6.3.0"),
            Release(versionName = "2.6.6.0"),
            Release(versionName = "2.6.4.0"),
            Release(versionName = "2.7.0.0"),
            Release(versionName = "2.6.5.0")
        )

        // When installed is 2.6.3.0, immediate next is 2.6.4.0
        val nextFrom263 = VersionComparator.findImmediateNextRelease("2.6.3.0", releases)
        org.junit.Assert.assertEquals("2.6.4.0", nextFrom263?.versionName)

        // When installed is 2.6.4.0, immediate next is 2.6.5.0
        val nextFrom264 = VersionComparator.findImmediateNextRelease("2.6.4.0", releases)
        org.junit.Assert.assertEquals("2.6.5.0", nextFrom264?.versionName)

        // When installed is 2.7.0.0 (highest), no newer release exists
        val nextFrom270 = VersionComparator.findImmediateNextRelease("2.7.0.0", releases)
        org.junit.Assert.assertNull(nextFrom270)
    }
}
