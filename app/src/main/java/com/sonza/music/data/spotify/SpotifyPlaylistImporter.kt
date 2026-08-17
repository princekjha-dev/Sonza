package com.sonza.music.data.spotify

import com.sonza.music.core.model.Track
import kotlinx.serialization.Serializable
import kotlin.math.min

@Serializable
data class SpotifyImportTrack(
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0,
    val spotifyUri: String = ""
)

data class ImportMatchResult(
    val spotifyTrack: SpotifyImportTrack,
    val matchedTrack: Track?,
    val matchScore: Float, // 0.0 to 1.0
    val isResolved: Boolean
)

data class SpotifyImportReport(
    val playlistName: String,
    val totalTracksCount: Int,
    val matchedCount: Int,
    val unresolvedCount: Int,
    val results: List<ImportMatchResult>
)

object SpotifyPlaylistImporter {

    fun matchImportedTracks(
        imported: List<SpotifyImportTrack>,
        availableTracks: List<Track>,
        playlistName: String = "Imported Spotify Playlist",
        matchThreshold: Float = 0.65f
    ): SpotifyImportReport {
        val results = mutableListOf<ImportMatchResult>()

        for (spotifyTrack in imported) {
            var bestMatch: Track? = null
            var bestScore = 0.0f

            for (candidate in availableTracks) {
                val titleSimilarity = calculateSimilarity(spotifyTrack.title, candidate.title)
                val artistSimilarity = calculateSimilarity(spotifyTrack.artist, candidate.artist)
                val combinedScore = (titleSimilarity * 0.6f) + (artistSimilarity * 0.4f)

                if (combinedScore > bestScore) {
                    bestScore = combinedScore
                    bestMatch = candidate
                }
            }

            val isResolved = bestScore >= matchThreshold && bestMatch != null
            results.add(
                ImportMatchResult(
                    spotifyTrack = spotifyTrack,
                    matchedTrack = if (isResolved) bestMatch else null,
                    matchScore = bestScore,
                    isResolved = isResolved
                )
            )
        }

        val matchedCount = results.count { it.isResolved }
        val unresolvedCount = results.size - matchedCount

        return SpotifyImportReport(
            playlistName = playlistName,
            totalTracksCount = imported.size,
            matchedCount = matchedCount,
            unresolvedCount = unresolvedCount,
            results = results
        )
    }

    /**
     * Normalized Levenshtein similarity with parenthetical noise stripping (e.g. "Remastered", "Original Mix")
     */
    fun calculateSimilarity(s1: String, s2: String): Float {
        val clean1 = sanitizeString(s1)
        val clean2 = sanitizeString(s2)

        if (clean1 == clean2) return 1.0f
        if (clean1.isEmpty() || clean2.isEmpty()) return 0.0f

        // Check if one is a substring of another
        if (clean1.contains(clean2) || clean2.contains(clean1)) {
            val minLen = Math.min(clean1.length, clean2.length).toFloat()
            val maxLen = Math.max(clean1.length, clean2.length).toFloat()
            return (minLen / maxLen).coerceAtLeast(0.80f)
        }

        val dist = levenshteinDistance(clean1, clean2)
        val maxLen = Math.max(clean1.length, clean2.length)
        return (1.0f - (dist.toFloat() / maxLen.toFloat())).coerceIn(0.0f, 1.0f)
    }

    private fun sanitizeString(s: String): String {
        return s.lowercase()
            .replace(Regex("\\(.*?\\)|\\[.*?\\]"), "") // Remove (Original Mix), [2026 Remaster], etc.
            .replace(Regex("[^a-z0-9 ]"), "")
            .trim()
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }
}
