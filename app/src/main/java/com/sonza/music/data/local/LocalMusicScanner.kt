package com.sonza.music.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.sonza.music.core.database.TrackEntity
import com.sonza.music.core.logging.SonzaLogger
import com.sonza.music.core.model.AudioCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMusicScanner(private val context: Context) {

    suspend fun scanLocalTracks(): List<TrackEntity> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<TrackEntity>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.IS_MUSIC
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 15000"

        try {
            val cursor = context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Unknown Track"
                    val artist = it.getString(artistCol) ?: "Unknown Artist"
                    val album = it.getString(albumCol) ?: "Unknown Album"
                    val albumId = it.getLong(albumIdCol)
                    val duration = it.getLong(durationCol)
                    val trackNum = it.getInt(trackCol)
                    val year = it.getInt(yearCol)
                    val mime = it.getString(mimeCol) ?: "audio/mpeg"

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    val codec = when {
                        mime.contains("flac", ignoreCase = true) -> AudioCodec.FLAC
                        mime.contains("wav", ignoreCase = true) -> AudioCodec.WAV
                        mime.contains("opus", ignoreCase = true) -> AudioCodec.OPUS
                        mime.contains("aac", ignoreCase = true) -> AudioCodec.AAC
                        mime.contains("ogg", ignoreCase = true) -> AudioCodec.VORBIS
                        else -> AudioCodec.MP3
                    }

                    val isLossless = codec == AudioCodec.FLAC || codec == AudioCodec.WAV || codec == AudioCodec.ALAC
                    val bitDepth = if (isLossless) 24 else 16
                    val sampleRate = if (isLossless) 96000 else 44100
                    val bitRate = if (isLossless) 2400 else 320

                    tracks.add(
                        TrackEntity(
                            id = "local_$id",
                            title = title,
                            artist = artist,
                            artistId = "artist_${artist.hashCode()}",
                            album = album,
                            albumId = "album_$albumId",
                            durationMs = duration,
                            mediaUri = contentUri.toString(),
                            artworkUri = artworkUri,
                            trackNumber = trackNum % 1000,
                            discNumber = if (trackNum >= 1000) trackNum / 1000 else 1,
                            year = if (year > 0) year else 2026,
                            genre = "Local Audio",
                            codec = codec.name,
                            bitDepth = bitDepth,
                            sampleRateHz = sampleRate,
                            bitRateKbps = bitRate,
                            isLossless = isLossless,
                            channelCount = 2,
                            trackGainDb = 0.0f,
                            trackPeak = 1.0f,
                            albumGainDb = 0.0f,
                            albumPeak = 1.0f,
                            isFavorite = false,
                            isDownloaded = true,
                            isLocal = true,
                            sourceProvider = "LOCAL_STORAGE",
                            playCount = 0,
                            dateAdded = System.currentTimeMillis()
                        )
                    )
                }
            }
            SonzaLogger.i("LocalMusicScanner", "Scanned ${tracks.size} local music tracks")
        } catch (e: Exception) {
            SonzaLogger.e("LocalMusicScanner", "Failed scanning local music: ${e.message}", e)
        }

        tracks
    }
}
