package com.sonza.music.audio.engine

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sonza.music.audio.analyzer.AudioAnalyzer
import com.sonza.music.audio.effects.ReplayGainNormalizer
import com.sonza.music.audio.equalizer.SonzaEqualizer
import com.sonza.music.audio.spatial.SpatialAudioProcessor
import com.sonza.music.core.logging.SonzaLogger
import com.sonza.music.core.model.Track
import com.sonza.music.core.model.VolumeNormalizationMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SonzaAudioEngine(
    private val context: Context,
    val equalizer: SonzaEqualizer,
    val spatialProcessor: SpatialAudioProcessor,
    val analyzer: AudioAnalyzer
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var exoPlayer: ExoPlayer? = null
    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var volumeNormalizationMode = VolumeNormalizationMode.TRACK_GAIN

    init {
        initExoPlayer()
    }

    private fun initExoPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true) // handleAudioFocus = true
            .setHandleAudioBecomingNoisy(true) // pauses on headphone disconnect
            .build().apply {
                addListener(PlayerListener())
            }

        exoPlayer?.audioSessionId?.let { sessionId ->
            if (sessionId != C.AUDIO_SESSION_ID_UNSET) {
                equalizer.attachAudioSession(sessionId)
                spatialProcessor.attachAudioSession(sessionId)
                analyzer.attachAudioSession(sessionId)
            }
        }
    }

    fun getExoPlayer(): ExoPlayer? = exoPlayer

    fun setQueue(tracks: List<Track>, startIndex: Int = 0, autoPlay: Boolean = true) {
        if (tracks.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, tracks.lastIndex)
        _playerState.update {
            it.copy(
                queue = tracks,
                queueIndex = validIndex,
                currentTrack = tracks[validIndex]
            )
        }

        val mediaItems = tracks.map { track ->
            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(track.artworkUri?.let { Uri.parse(it) })
                .build()

            MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(Uri.parse(track.mediaUri))
                .setMediaMetadata(metadata)
                .build()
        }

        exoPlayer?.let { player ->
            player.setMediaItems(mediaItems, validIndex, 0L)
            player.prepare()
            if (autoPlay) {
                player.play()
            }
        }
        applyTrackNormalization(tracks[validIndex])
        startPositionTicker()
    }

    fun playTrack(track: Track) {
        val currentQueue = _playerState.value.queue
        val existingIndex = currentQueue.indexOfFirst { it.id == track.id }

        if (existingIndex != -1) {
            playQueueIndex(existingIndex)
        } else {
            val newQueue = currentQueue + track
            setQueue(newQueue, startIndex = newQueue.lastIndex, autoPlay = true)
        }
    }

    fun playQueueIndex(index: Int) {
        val queue = _playerState.value.queue
        if (index in queue.indices) {
            _playerState.update {
                it.copy(
                    queueIndex = index,
                    currentTrack = queue[index]
                )
            }
            exoPlayer?.seekTo(index, 0L)
            exoPlayer?.play()
            applyTrackNormalization(queue[index])
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        if (_playerState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playerState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun next() {
        if (exoPlayer?.hasNextMediaItem() == true) {
            exoPlayer?.seekToNextMediaItem()
        } else if (_playerState.value.repeatMode == PlayerRepeatMode.ALL && _playerState.value.queue.isNotEmpty()) {
            playQueueIndex(0)
        }
    }

    fun previous() {
        val currentPos = exoPlayer?.currentPosition ?: 0L
        if (currentPos > 3000L) {
            seekTo(0L)
        } else if (exoPlayer?.hasPreviousMediaItem() == true) {
            exoPlayer?.seekToPreviousMediaItem()
        } else {
            seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_playerState.value.isShuffleEnabled
        _playerState.update { it.copy(isShuffleEnabled = newShuffle) }
        exoPlayer?.shuffleModeEnabled = newShuffle
    }

    fun cycleRepeatMode() {
        val nextMode = when (_playerState.value.repeatMode) {
            PlayerRepeatMode.OFF -> PlayerRepeatMode.ALL
            PlayerRepeatMode.ALL -> PlayerRepeatMode.ONE
            PlayerRepeatMode.ONE -> PlayerRepeatMode.OFF
        }
        _playerState.update { it.copy(repeatMode = nextMode) }
        exoPlayer?.repeatMode = when (nextMode) {
            PlayerRepeatMode.OFF -> Player.REPEAT_MODE_OFF
            PlayerRepeatMode.ALL -> Player.REPEAT_MODE_ALL
            PlayerRepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        _playerState.update { it.copy(playbackSpeed = clamped) }
        exoPlayer?.playbackParameters = PlaybackParameters(clamped)
    }

    fun setVolumeNormalizationMode(mode: VolumeNormalizationMode) {
        volumeNormalizationMode = mode
        _playerState.value.currentTrack?.let { applyTrackNormalization(it) }
    }

    private fun applyTrackNormalization(track: Track) {
        val scale = ReplayGainNormalizer.calculateVolumeScale(
            mode = volumeNormalizationMode,
            loudness = track.loudness,
            preampDb = equalizer.getPreampGain()
        )
        exoPlayer?.volume = scale
        SonzaLogger.i("SonzaAudioEngine", "Applied loudness normalization scale: $scale")
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60L
        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                _playerState.update { it.copy(sleepTimerRemainingSeconds = remaining) }
                delay(1000)
                remaining--
            }
            // Graceful fadeout over 5 seconds
            val currentVol = exoPlayer?.volume ?: 1.0f
            for (step in 10 downTo 1) {
                exoPlayer?.volume = (currentVol * (step / 10.0f))
                delay(500)
            }
            pause()
            exoPlayer?.volume = currentVol
            _playerState.update { it.copy(sleepTimerRemainingSeconds = null) }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _playerState.update { it.copy(sleepTimerRemainingSeconds = null) }
    }

    private fun startPositionTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    val duration = player.duration.coerceAtLeast(0L)
                    val buffered = player.bufferedPosition
                    _playerState.update {
                        it.copy(
                            currentPositionMs = pos,
                            durationMs = duration,
                            bufferedPositionMs = buffered
                        )
                    }
                }
                delay(60) // 16-60fps smooth scrubber & lyrics update
            }
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _playerState.update {
                it.copy(isBuffering = (playbackState == Player.STATE_BUFFERING))
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newIndex = exoPlayer?.currentMediaItemIndex ?: -1
            val queue = _playerState.value.queue
            if (newIndex in queue.indices) {
                val currentTrack = queue[newIndex]
                _playerState.update {
                    it.copy(
                        queueIndex = newIndex,
                        currentTrack = currentTrack
                    )
                }
                applyTrackNormalization(currentTrack)
            }
        }
    }

    fun release() {
        tickerJob?.cancel()
        sleepTimerJob?.cancel()
        equalizer.release()
        spatialProcessor.release()
        analyzer.release()
        exoPlayer?.release()
        exoPlayer = null
    }
}
