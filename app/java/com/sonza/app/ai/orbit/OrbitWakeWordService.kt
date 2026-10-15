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
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sonza.app.MainActivity
import com.sonza.app.R

/**
 * Foreground Wake-Word Service for "Hey Orbit".
 *
 * This service serves TWO purposes:
 * 1. Provides the foreground service + notification required by Android to keep
 *    the app alive and hold FOREGROUND_SERVICE_MICROPHONE type.
 * 2. Hosts the single authoritative [OrbitWakeWordDetector] instance that listens
 *    for "Hey Orbit" and broadcasts the detection event.
 *
 * The in-app [OrbitAssistantViewModel] does NOT create its own detector — it communicates
 * with the single detector hosted here via broadcasts and a static accessor.
 */
class OrbitWakeWordService : Service() {

    private var wakeWordDetector: OrbitWakeWordDetector? = null

    companion object {
        private const val TAG = "OrbitWakeService"
        const val CHANNEL_ID = "orbit_wake_word_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP_LISTENING = "com.sonza.app.orbit.ACTION_STOP_LISTENING"
        const val ACTION_WAKE_WORD_DETECTED = "com.sonza.app.ORBIT_WAKE_WORD_DETECTED"
        const val ACTION_PAUSE_DETECTOR = "com.sonza.app.orbit.ACTION_PAUSE_DETECTOR"
        const val ACTION_RESUME_DETECTOR = "com.sonza.app.orbit.ACTION_RESUME_DETECTOR"
        const val EXTRA_COMMAND = "extra_orbit_command"

        // Static accessor for the ViewModel to pause/resume the single detector
        @Volatile
        private var instance: OrbitWakeWordService? = null

        fun getDetector(): OrbitWakeWordDetector? = instance?.wakeWordDetector

        fun start(context: Context) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "start: no RECORD_AUDIO permission")
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

        fun pauseDetector(context: Context) {
            val intent = Intent(context, OrbitWakeWordService::class.java).apply {
                action = ACTION_PAUSE_DETECTOR
            }
            context.startService(intent)
        }

        fun resumeDetector(context: Context) {
            val intent = Intent(context, OrbitWakeWordService::class.java).apply {
                action = ACTION_RESUME_DETECTOR
            }
            try { context.startService(intent) } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        Log.d(TAG, "Service CREATED")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_LISTENING -> {
                Log.d(TAG, "Received ACTION_STOP_LISTENING")
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PAUSE_DETECTOR -> {
                Log.d(TAG, "Received ACTION_PAUSE_DETECTOR")
                wakeWordDetector?.pauseListening()
                return START_STICKY
            }
            ACTION_RESUME_DETECTOR -> {
                Log.d(TAG, "Received ACTION_RESUME_DETECTOR")
                wakeWordDetector?.resumeListening()
                return START_STICKY
            }
        }

        startAsForeground()
        initializeDetector()
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
            .setColor(0xFF00E5FF.toInt())
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

    private fun initializeDetector() {
        if (wakeWordDetector != null) {
            Log.d(TAG, "Detector already initialized, ensuring it's listening")
            wakeWordDetector?.startListening()
            return
        }

        Log.d(TAG, "Creating new OrbitWakeWordDetector")
        wakeWordDetector = OrbitWakeWordDetector(
            context = this,
            onWakeWordDetected = { command ->
                onWakeWordTriggered(command)
            }
        )
        wakeWordDetector?.startListening()
    }

    private fun onWakeWordTriggered(command: String?) {
        Log.d(TAG, "🎯 Wake word triggered! command=${command ?: "(none)"}")

        // Play distinct Orbit wake word chime
        OrbitSoundManager.playWakeWordDetected()

        // Broadcast to in-app receiver
        val broadcastIntent = Intent(ACTION_WAKE_WORD_DETECTED).apply {
            setPackage(packageName)
            if (!command.isNullOrBlank()) {
                putExtra(EXTRA_COMMAND, command)
            }
        }
        sendBroadcast(broadcastIntent)

        // Launch / bring MainActivity to front with deep link
        val targetUri = if (!command.isNullOrBlank()) {
            Uri.parse("sonza://orbit_listen?command=" + Uri.encode(command))
        } else {
            Uri.parse("sonza://orbit_listen")
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = targetUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(launchIntent)
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service DESTROYED")
        wakeWordDetector?.stopListening()
        wakeWordDetector = null
        instance = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
