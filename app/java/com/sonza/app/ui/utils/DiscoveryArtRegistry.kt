package com.sonza.app.ui.utils

import androidx.compose.ui.graphics.Color
import com.sonza.app.core.model.BrowseCategory

/**
 * Metadata for a discovery category tile, containing curated artwork URL and theme tint color.
 */
data class DiscoveryArtMeta(
    val title: String,
    val imageUrl: String,
    val tintColor: Long,
    val subtitle: String? = null,
    val browseQuery: String? = null
)

/**
 * Registry of curated high-resolution music and artist artwork with paired vibrant color tints
 * inspired by Apple Music / modern streaming discovery aesthetics.
 */
object DiscoveryArtRegistry {

    private val curatedArt: Map<String, DiscoveryArtMeta> = mapOf(
        "pop" to DiscoveryArtMeta(
            title = "Pop",
            imageUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFE83D84, // Vibrant rose/pink
            browseQuery = "pop hits"
        ),
        "hits" to DiscoveryArtMeta(
            title = "Hits",
            imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFE5A912, // Golden amber
            browseQuery = "top hits"
        ),
        "r&b" to DiscoveryArtMeta(
            title = "R&B",
            imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF7E57C2, // Royal purple
            browseQuery = "r&b soul hits"
        ),
        "hip-hop" to DiscoveryArtMeta(
            title = "Hip-Hop",
            imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF1E88E5, // Electric royal blue
            browseQuery = "hip hop hits"
        ),
        "holiday" to DiscoveryArtMeta(
            title = "Holiday",
            imageUrl = "https://images.unsplash.com/photo-1512389142860-9c449e58a543?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFC62828, // Deep crimson red
            browseQuery = "holiday music festive"
        ),
        "sonza live" to DiscoveryArtMeta(
            title = "Sonza Live",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF7986CB, // Lavender indigo
            browseQuery = "live concert performances"
        ),
        "sonza radio" to DiscoveryArtMeta(
            title = "Sonza Radio",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFE53935, // Coral crimson
            browseQuery = "radio mix hits"
        ),
        "coming soon" to DiscoveryArtMeta(
            title = "Coming Soon",
            imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFEC407A, // Rose blush
            browseQuery = "new releases music"
        ),
        "spatial audio" to DiscoveryArtMeta(
            title = "Spatial Audio",
            imageUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFD32F2F, // Ruby red
            browseQuery = "spatial audio lossless"
        ),
        "country" to DiscoveryArtMeta(
            title = "Country",
            imageUrl = "https://images.unsplash.com/photo-1485579149621-3123dd979885?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFD97706, // Warm amber ochre
            browseQuery = "country hits acoustic"
        ),
        "electronic" to DiscoveryArtMeta(
            title = "Electronic",
            imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF00ACC1, // Cyan teal
            browseQuery = "electronic dance edm"
        ),
        "latin" to DiscoveryArtMeta(
            title = "Latin",
            imageUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFC2185B, // Magenta berry
            browseQuery = "latin reggaeton hits"
        ),
        "rock" to DiscoveryArtMeta(
            title = "Rock",
            imageUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFD84315, // Crimson orange
            browseQuery = "rock classics alternative"
        ),
        "chill" to DiscoveryArtMeta(
            title = "Chill",
            imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF00897B, // Soft teal
            browseQuery = "chill relax music"
        ),
        "workout" to DiscoveryArtMeta(
            title = "Workout",
            imageUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFFF6D00, // Neon energetic orange
            browseQuery = "workout gym motivation"
        ),
        "romance" to DiscoveryArtMeta(
            title = "Romance",
            imageUrl = "https://images.unsplash.com/photo-1518199266791-5375a83190b7?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFF06292, // Soft blush rose
            browseQuery = "romantic love songs"
        ),
        "lo-fi" to DiscoveryArtMeta(
            title = "Lo-fi",
            imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF5C6BC0, // Indigo lavender
            browseQuery = "lofi chill beats"
        ),
        "sleep" to DiscoveryArtMeta(
            title = "Sleep",
            imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF303F9F, // Midnight navy
            browseQuery = "sleep ambient rain"
        ),
        "focus" to DiscoveryArtMeta(
            title = "Focus",
            imageUrl = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF455A64, // Slate blue
            browseQuery = "focus study instrumental"
        ),
        "party" to DiscoveryArtMeta(
            title = "Party",
            imageUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFFF5722, // Deep orange
            browseQuery = "party dance club hits"
        ),
        "trending" to DiscoveryArtMeta(
            title = "Trending",
            imageUrl = "https://images.unsplash.com/photo-1429962714451-bb934ecdc4ec?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF7C4DFF, // Electric violet
            browseQuery = "trending songs top"
        ),
        "bollywood" to DiscoveryArtMeta(
            title = "Bollywood",
            imageUrl = "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFFF9800, // Saffron orange
            browseQuery = "bollywood latest hits"
        ),
        "punjabi" to DiscoveryArtMeta(
            title = "Punjabi",
            imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFFF59E0B, // Electric gold
            browseQuery = "punjabi hits latest"
        ),
        "indie" to DiscoveryArtMeta(
            title = "Indie",
            imageUrl = "https://images.unsplash.com/photo-1460723237483-7a6dc9d0b212?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF689F38, // Olive sage
            browseQuery = "indie alternative songs"
        ),
        "classical" to DiscoveryArtMeta(
            title = "Classical",
            imageUrl = "https://images.unsplash.com/photo-1507838153414-b4b713384a76?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF8D6E63, // Warm taupe
            browseQuery = "classical orchestra piano"
        ),
        "jazz" to DiscoveryArtMeta(
            title = "Jazz",
            imageUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF5D4037, // Espresso brown
            browseQuery = "jazz blues cafe"
        ),
        "metal" to DiscoveryArtMeta(
            title = "Metal",
            imageUrl = "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF37474F, // Dark slate
            browseQuery = "heavy metal rock"
        ),
        "halftime" to DiscoveryArtMeta(
            title = "Bad Bunny's Road to Halftime",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop",
            tintColor = 0xFF8D5B4C, // Warm sepia / terracotta
            browseQuery = "bad bunny hits"
        )
    )

    private val fallbackColors = listOf(
        0xFFE83D84, // Rose
        0xFFE5A912, // Golden
        0xFF7E57C2, // Purple
        0xFF1E88E5, // Blue
        0xFFD32F2F, // Crimson
        0xFF00ACC1, // Cyan
        0xFFC2185B, // Magenta
        0xFF00897B, // Teal
        0xFFFF6D00, // Orange
        0xFF5C6BC0  // Indigo
    )

    /**
     * Resolves artwork and vibrant tint metadata for any category title.
     */
    fun getDiscoveryMeta(title: String): DiscoveryArtMeta {
        val key = title.trim().lowercase()
        
        // Exact match
        curatedArt[key]?.let { return it }

        // Keyword matches
        when {
            key.contains("pop") -> return curatedArt["pop"]!!
            key.contains("hit") || key.contains("chart") -> return curatedArt["hits"]!!
            key.contains("r&b") || key.contains("soul") -> return curatedArt["r&b"]!!
            key.contains("hip") || key.contains("rap") -> return curatedArt["hip-hop"]!!
            key.contains("holiday") || key.contains("festive") || key.contains("xmas") -> return curatedArt["holiday"]!!
            key.contains("live") -> return curatedArt["sonza live"]!!
            key.contains("radio") -> return curatedArt["sonza radio"]!!
            key.contains("new") || key.contains("coming") || key.contains("release") -> return curatedArt["coming soon"]!!
            key.contains("spatial") || key.contains("atmos") || key.contains("lossless") -> return curatedArt["spatial audio"]!!
            key.contains("country") || key.contains("folk") -> return curatedArt["country"]!!
            key.contains("electronic") || key.contains("edm") || key.contains("dance") -> return curatedArt["electronic"]!!
            key.contains("latin") || key.contains("reggaeton") -> return curatedArt["latin"]!!
            key.contains("rock") || key.contains("metal") || key.contains("punk") -> return curatedArt["rock"]!!
            key.contains("chill") || key.contains("relax") -> return curatedArt["chill"]!!
            key.contains("workout") || key.contains("gym") || key.contains("energy") -> return curatedArt["workout"]!!
            key.contains("romance") || key.contains("love") -> return curatedArt["romance"]!!
            key.contains("lo-fi") || key.contains("lofi") || key.contains("beat") -> return curatedArt["lo-fi"]!!
            key.contains("sleep") || key.contains("night") -> return curatedArt["sleep"]!!
            key.contains("focus") || key.contains("study") -> return curatedArt["focus"]!!
            key.contains("party") || key.contains("club") -> return curatedArt["party"]!!
            key.contains("bolly") || key.contains("hindi") || key.contains("desi") -> return curatedArt["bollywood"]!!
            key.contains("punjabi") -> return curatedArt["punjabi"]!!
            key.contains("indie") || key.contains("alt") -> return curatedArt["indie"]!!
            key.contains("classic") || key.contains("piano") -> return curatedArt["classical"]!!
            key.contains("jazz") || key.contains("blues") -> return curatedArt["jazz"]!!
            key.contains("bad bunny") || key.contains("halftime") -> return curatedArt["halftime"]!!
        }

        // Deterministic fallback color
        val hash = title.hashCode()
        val fallbackColor = fallbackColors[Math.floorMod(hash, fallbackColors.size)]
        val fallbackImage = curatedArt.values.elementAt(Math.floorMod(hash, curatedArt.size)).imageUrl

        return DiscoveryArtMeta(
            title = title,
            imageUrl = fallbackImage,
            tintColor = fallbackColor,
            browseQuery = "$title music"
        )
    }

    /**
     * Enriches a [BrowseCategory] with high-res artwork URL and vibrant tint color if missing.
     */
    fun enrichCategory(category: BrowseCategory): BrowseCategory {
        val meta = getDiscoveryMeta(category.title)
        return category.copy(
            thumbnailUrl = category.thumbnailUrl?.takeIf { it.isNotBlank() } ?: meta.imageUrl,
            color = category.color ?: meta.tintColor
        )
    }

    /**
     * Default curated categories inspired by the reference music discovery screen.
     */
    fun getDefaultDiscoveryCategories(): List<BrowseCategory> = listOf(
        BrowseCategory(
            title = "Bad Bunny's Road to Halftime",
            browseId = "SEARCH::bad bunny hits",
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop",
            color = 0xFF8D5B4C
        ),
        BrowseCategory(
            title = "Pop",
            browseId = "SEARCH::pop hits latest",
            thumbnailUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?q=80&w=800&auto=format&fit=crop",
            color = 0xFFE83D84
        ),
        BrowseCategory(
            title = "Hits",
            browseId = "SEARCH::top hits music",
            thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=800&auto=format&fit=crop",
            color = 0xFFE5A912
        ),
        BrowseCategory(
            title = "R&B",
            browseId = "SEARCH::r&b soul hits",
            thumbnailUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=800&auto=format&fit=crop",
            color = 0xFF7E57C2
        ),
        BrowseCategory(
            title = "Holiday",
            browseId = "SEARCH::holiday festive songs",
            thumbnailUrl = "https://images.unsplash.com/photo-1512389142860-9c449e58a543?q=80&w=800&auto=format&fit=crop",
            color = 0xFFC62828
        ),
        BrowseCategory(
            title = "Sonza Live",
            browseId = "SEARCH::live music performances",
            thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=800&auto=format&fit=crop",
            color = 0xFF7986CB
        ),
        BrowseCategory(
            title = "Sonza Radio",
            browseId = "SEARCH::radio mix hits",
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop",
            color = 0xFFE53935
        ),
        BrowseCategory(
            title = "Coming Soon",
            browseId = "SEARCH::new music releases",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop",
            color = 0xFFEC407A
        ),
        BrowseCategory(
            title = "Spatial Audio",
            browseId = "SEARCH::spatial audio music",
            thumbnailUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?q=80&w=800&auto=format&fit=crop",
            color = 0xFFD32F2F
        ),
        BrowseCategory(
            title = "Hip-Hop",
            browseId = "SEARCH::hip hop hits",
            thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop",
            color = 0xFF1E88E5
        ),
        BrowseCategory(
            title = "Country",
            browseId = "SEARCH::country hits acoustic",
            thumbnailUrl = "https://images.unsplash.com/photo-1485579149621-3123dd979885?q=80&w=800&auto=format&fit=crop",
            color = 0xFFD97706
        ),
        BrowseCategory(
            title = "Electronic",
            browseId = "SEARCH::electronic dance music",
            thumbnailUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=800&auto=format&fit=crop",
            color = 0xFF00ACC1
        ),
        BrowseCategory(
            title = "Latin",
            browseId = "SEARCH::latin hits reggaeton",
            thumbnailUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=800&auto=format&fit=crop",
            color = 0xFFC2185B
        ),
        BrowseCategory(
            title = "Rock",
            browseId = "SEARCH::rock classics hits",
            thumbnailUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=800&auto=format&fit=crop",
            color = 0xFFD84315
        ),
        BrowseCategory(
            title = "Chill",
            browseId = "SEARCH::chill relaxing music",
            thumbnailUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?q=80&w=800&auto=format&fit=crop",
            color = 0xFF00897B
        ),
        BrowseCategory(
            title = "Workout",
            browseId = "SEARCH::workout gym motivation",
            thumbnailUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=800&auto=format&fit=crop",
            color = 0xFFFF6D00
        ),
        BrowseCategory(
            title = "Romance",
            browseId = "SEARCH::romantic love songs",
            thumbnailUrl = "https://images.unsplash.com/photo-1518199266791-5375a83190b7?q=80&w=800&auto=format&fit=crop",
            color = 0xFFF06292
        ),
        BrowseCategory(
            title = "Lo-fi",
            browseId = "SEARCH::lofi beats to relax",
            thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=800&auto=format&fit=crop",
            color = 0xFF5C6BC0
        ),
        BrowseCategory(
            title = "Bollywood",
            browseId = "SEARCH::bollywood latest hits",
            thumbnailUrl = "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?q=80&w=800&auto=format&fit=crop",
            color = 0xFFFF9800
        ),
        BrowseCategory(
            title = "Punjabi",
            browseId = "SEARCH::punjabi hits songs",
            thumbnailUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?q=80&w=800&auto=format&fit=crop",
            color = 0xFFF59E0B
        ),
        BrowseCategory(
            title = "Sleep",
            browseId = "SEARCH::sleep rain ambient",
            thumbnailUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=800&auto=format&fit=crop",
            color = 0xFF303F9F
        ),
        BrowseCategory(
            title = "Focus",
            browseId = "SEARCH::focus study instrumental",
            thumbnailUrl = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?q=80&w=800&auto=format&fit=crop",
            color = 0xFF455A64
        )
    )
}
