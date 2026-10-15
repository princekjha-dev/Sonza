package com.sonza.app.ai.orbit

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sonza.app.MainActivity
import com.sonza.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sqrt

/**
 * Foreground Wake-Word Service for "Hey Orbit".
 * Runs battery-conscious background listening with Android privacy compliance,
 * audio focus handling, and seamless transition into Sonza's Orbit assistant.
 */
class OrbitWakeWordService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var listeningJob: Job? = null

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    private lateinit var audioManager: AudioManager
    private var hasAudioFocus = true
    private var speechRecognizer: SpeechRecognizer? = null
    private var isVadActive = false

    companion object {
        const val CHANNEL_ID = "orbit_wake_word_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP_LISTENING = "com.sonza.app.orbit.ACTION_STOP_LISTENING"
        const val ACTION_WAKE_WORD_DETECTED = "com.sonza.app.ORBIT_WAKE_WORD_DETECTED"

        fun start(context: Context) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            val intent = Intent(context, OrbitWakeWordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OrbitWakeWordService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        setupAudioFocus()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_LISTENING) {
            stopSelf()
            return START_NOT_STICKY
        }

        startAsForeground()
        startLowPowerWakeWordPipeline()
        return START_STICKY
    }

    private fun startAsForeground() {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("sonza://orbit_listen")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, OrbitWakeWordService::class.java).apply {
            action = ACTION_STOP_LISTENING
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Orbit Voice Assistant")
            .setContentText("Listening for \"Hey Orbit\"...")
            .setSmallIcon(R.drawable.ic_mic)
            .setColor(0xFF38BDF8.toInt())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingOpen)
            .addAction(R.drawable.ic_close, "Stop Listening", pendingStop)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Orbit Assistant Wake Word",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when Sonza's Orbit is actively listening for wake-word activation."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun setupAudioFocus() {
        val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    hasAudioFocus = false
                    pauseAudioCapture()
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                    resumeAudioCapture()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    /**
     * Continuous battery-conscious audio detection.
     * Uses energy-threshold Voice Activity Detection (VAD) to keep CPU low,
     * only invoking the recognition pipeline when real speech is present.
     */
    private fun startLowPowerWakeWordPipeline() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        listeningJob?.cancel()
        listeningJob = serviceScope.launch {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    return@launch
                }

                audioRecord?.startRecording()
                isRecording = true

                val buffer = ShortArray(1024)
                while (isActive && isRecording) {
                    if (!hasAudioFocus) {
                        delay(500L)
                        continue
                    }

                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sum / read)

                        // Speech energy threshold
                        if (rms > 750 && !isVadActive) {
                            isVadActive = true
                            serviceScope.launch(Dispatchers.Main) {
                                triggerKeywordVerification()
                            }
                            delay(1800L)
                            isVadActive = false
                        }
                    }
                    delay(30L)
                }
            } catch (_: Exception) {
            } finally {
                releaseAudioRecord()
            }
        }
    }

    private fun triggerKeywordVerification() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.lowercase(Locale.ROOT).orEmpty()
                        if (text.contains("hey orbit") || text.contains("orbit")) {
                            onWakeWordMatched()
                        }
                    }

                    override fun onError(error: Int) {}
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.lowercase(Locale.ROOT).orEmpty()
                        if (text.contains("hey orbit") || text.contains("orbit")) {
                            onWakeWordMatched()
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    private fun onWakeWordMatched() {
        speechRecognizer?.stopListening()

        // Play wake word chime
        OrbitSoundManager.playWakeWordDetected()

        // Signal app broadcast
        sendBroadcast(Intent(ACTION_WAKE_WORD_DETECTED).setPackage(packageName))

        // Open Sonza to Orbit assistant
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("sonza://orbit_listen")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun pauseAudioCapture() {
        try {
            if (isRecording) {
                audioRecord?.stop()
                isRecording = false
            }
        } catch (_: Exception) {}
    }

    private fun resumeAudioCapture() {
        try {
            if (!isRecording && audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.startRecording()
                isRecording = true
            }
        } catch (_: Exception) {}
    }

    private fun releaseAudioRecord() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        isRecording = false
    }

    override fun onDestroy() {
        super.onDestroy()
        listeningJob?.cancel()
        serviceScope.cancel()
        releaseAudioRecord()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
