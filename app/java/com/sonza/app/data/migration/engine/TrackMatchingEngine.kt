package com.sonza.app.data.migration.engine

import com.sonza.app.core.model.Song
import com.sonza.app.data.migration.model.MatchConfidence
import com.sonza.app.data.migration.model.SourceTrack
import com.sonza.app.data.migration.model.TrackMatchResult
import com.sonza.app.data.repository.YouTubeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class TrackMatchingEngine @Inject constructor(
    private val youTubeRepository: YouTubeRepository
) {

    /**
     * Match a list of source tracks with concurrent batching and progress reporting.
     */
    suspend fun matchTracks(
        tracks: List<SourceTrack>,
        onProgress: (completed: Int, total: Int, lastResult: TrackMatchResult) -> Unit = { _, _, _ -> }
    ): List<TrackMatchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<TrackMatchResult>()
        val total = tracks.size

        tracks.forEachIndexed { index, track ->
            val result = matchSingleTrack(track)
            results.add(result)
            onProgress(index + 1, total, result)
        }

        results
    }

    /**
     * Match a single track using multi-tier matching logic.
     */
    suspend fun matchSingleTrack(track: SourceTrack): TrackMatchResult = withContext(Dispatchers.IO) {
        // If sourceId is already a native YouTube video/song id, verify and fetch directly
        if (!track.sourceId.isNullOrBlank()) {
            try {
                val candidate = youTubeRepository.search(track.sourceId).firstOrNull { it.id == track.sourceId }
                if (candidate != null) {
                    return@withContext TrackMatchResult(
                        sourceTrack = track,
                        matchedSong = candidate,
                        confidence = MatchConfidence.PERFECT_MATCH,
                        possibleCandidates = listOf(candidate)
                    )
                }
            } catch (_: Exception) {}
        }

        val cleanTitle = normalizeText(track.title)
        val cleanArtist = normalizeText(track.artist)
        val searchQuery = "${track.title} ${track.artist}".trim()

        try {
            val candidates = youTubeRepository.search(searchQuery)
            if (candidates.isEmpty()) {
                // Secondary fallback search with title only if query had noise
                val fallbackCandidates = if (cleanTitle.isNotBlank()) youTubeRepository.search(track.title) else emptyList()
                return@withContext evaluateCandidates(track, fallbackCandidates)
            }
            return@withContext evaluateCandidates(track, candidates)
        } catch (_: Exception) {
            TrackMatchResult(
                sourceTrack = track,
                matchedSong = null,
                confidence = MatchConfidence.UNAVAILABLE
            )
        }
    }

    /**
     * Evaluates candidates with strict scoring and artist overlap gating.
     */
    fun evaluateCandidates(source: SourceTrack, candidates: List<Song>): TrackMatchResult {
        if (candidates.isEmpty()) {
            return TrackMatchResult(sourceTrack = source, confidence = MatchConfidence.UNAVAILABLE)
        }

        val normSourceTitle = normalizeText(source.title)
        val normSourceArtist = normalizeText(source.artist)
        val sourceArtistTokens = tokenize(normSourceArtist)

        var bestSong: Song? = null
        var bestScore = 0.0
        val scoredCandidates = mutableListOf<Pair<Song, Double>>()

        for (candidate in candidates) {
            val normCandTitle = normalizeText(candidate.title)
            val normCandArtist = normalizeText(candidate.artist)
            val candArtistTokens = tokenize(normCandArtist)

            // Strict artist overlap calculation
            val artistOverlap = if (sourceArtistTokens.isEmpty() || candArtistTokens.isEmpty()) {
                0.5 // Neutral if unknown
            } else {
                val common = sourceArtistTokens.intersect(candArtistTokens)
                common.size.toDouble() / sourceArtistTokens.size.toDouble()
            }

            // Gating: If both artists are known and have 0 token overlap, heavily penalize or reject
            if (sourceArtistTokens.isNotEmpty() && candArtistTokens.isNotEmpty() && artistOverlap == 0.0) {
                // Check if artist name appears inside title (e.g. "Song (feat. Artist)")
                val candTitleTokens = tokenize(normCandTitle)
                val artistInTitle = sourceArtistTokens.any { it in candTitleTokens }
                if (!artistInTitle) {
                    continue // Skip completely wrong artist
                }
            }

            // Title similarity
            val titleSimilarity = calculateSimilarity(normSourceTitle, normCandTitle)

            // Duration bonus
            val durationBonus = if (source.durationMs > 0 && candidate.duration > 0) {
                val diffSec = abs((source.durationMs / 1000) - (candidate.duration / 1000))
                if (diffSec <= 15) 0.15 else if (diffSec <= 45) 0.05 else -0.10
            } else 0.0

            val totalScore = (titleSimilarity * 0.55) + (artistOverlap * 0.35) + durationBonus
            scoredCandidates.add(candidate to totalScore)

            if (totalScore > bestScore) {
                bestScore = totalScore
                bestSong = candidate
            }
        }

        val sortedCandidates = scoredCandidates.sortedByDescending { it.second }.map { it.first }

        return when {
            bestSong != null && bestScore >= 0.75 -> TrackMatchResult(
                sourceTrack = source,
                matchedSong = bestSong,
                confidence = MatchConfidence.PERFECT_MATCH,
                possibleCandidates = sortedCandidates.take(5)
            )
            bestSong != null && bestScore >= 0.45 -> TrackMatchResult(
                sourceTrack = source,
                matchedSong = bestSong,
                confidence = MatchConfidence.POSSIBLE_MATCH,
                possibleCandidates = sortedCandidates.take(5)
            )
            else -> TrackMatchResult(
                sourceTrack = source,
                matchedSong = null,
                confidence = MatchConfidence.UNAVAILABLE,
                possibleCandidates = sortedCandidates.take(5)
            )
        }
    }

    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace(Regex("""\((official\s*(video|audio|music\s*video|lyric\s*video|visualizer)|remastered|live|audio|video|lyrics)\)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\[(official\s*(video|audio|music\s*video|lyric\s*video|visualizer)|remastered|live|audio|video|hd|4k)\]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\b(feat|ft|featuring|with|prod|prod\sby)\b.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""[^\p{L}\p{Nd}\s]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun tokenize(text: String): Set<String> {
        val stopWords = setOf("the", "a", "an", "and", "or", "of", "in", "by", "for", "to", "official", "music", "video")
        return text.split(" ")
            .filter { it.length > 1 && it !in stopWords }
            .toSet()
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val t1 = tokenize(s1)
        val t2 = tokenize(s2)
        if (t1.isEmpty() || t2.isEmpty()) return 0.0

        val intersection = t1.intersect(t2).size
        val union = t1.union(t2).size
        return intersection.toDouble() / union.toDouble()
    }
}
