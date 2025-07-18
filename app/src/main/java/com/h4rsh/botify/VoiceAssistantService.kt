// file: app/src/main/java/com/h4rsh/botify/VoiceAssistantService.kt
package com.h4rsh.botify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import androidx.core.app.NotificationCompat

class VoiceAssistantService : Service() {

    // SECURE: Reading API key directly from local.properties
    private val picoVoiceAccessKey by lazy {
        getApiKeyFromLocalProperties("PICO_VOICE_ACCESS_KEY")
    }
    private val spotifyPackageName = "com.spotify.music"
    private val tag = "VoiceAssistantService"

    private fun getApiKeyFromLocalProperties(key: String): String {
        return try {
            val properties = java.util.Properties()
            val localPropertiesFile = java.io.File("local.properties")
            if (localPropertiesFile.exists()) {
                properties.load(localPropertiesFile.inputStream())
                properties.getProperty(key, "")
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(tag, "Error reading from local.properties: ${e.message}")
            ""
        }
    }

    private var porcupineManager: PorcupineManager? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var spotifyController: SpotifyController

    private val handler = Handler(Looper.getMainLooper())
    private var isSpotifyInForeground = false

    private val foregroundCheckRunnable = object : Runnable {
        override fun run() {
            val isInForeground = isAppInForeground(spotifyPackageName)
            if (isInForeground && !isSpotifyInForeground) {
                Log.d(tag, "Spotify opened. Starting wake word detection.")
                startWakeWordDetection()
                isSpotifyInForeground = true
            } else if (!isInForeground && isSpotifyInForeground) {
                Log.d(tag, "Spotify closed or in background. Stopping wake word detection.")
                stopWakeWordDetection()
                isSpotifyInForeground = false
            }
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        spotifyController = SpotifyController(this)
        startForegroundService()
        handler.post(foregroundCheckRunnable)
    }

    // REWRITTEN: This function is now updated to use the correct Porcupine v3 API
    private fun startWakeWordDetection() {
        try {
            // The callback is now an object passed into the .build() method
            val porcupineCallback = object : PorcupineManagerCallback {
                override fun invoke(keywordIndex: Int) {
                    Log.d(tag, "Wake word detected!")
                    stopWakeWordDetection()
                    startCommandRecognition()
                }
            }

            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(picoVoiceAccessKey)
                .setKeywordPath("hey-spotify.ppn") // Use singular .setKeywordPath
                .build(applicationContext, porcupineCallback) // Pass callback to build()

            porcupineManager?.start()

        } catch (e: PorcupineException) {
            Log.e(tag, "Failed to initialize Porcupine: ${e.message}")
        }
    }

    private fun stopWakeWordDetection() {
        porcupineManager?.stop()
        porcupineManager?.delete()
        porcupineManager = null
    }

    private fun startCommandRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { Log.d(tag, "Speech recognizer ready.") }
                override fun onBeginningOfSpeech() { Log.d(tag, "User started speaking.") }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { Log.d(tag, "User finished speaking.") }
                override fun onError(error: Int) {
                    Log.e(tag, "Speech recognizer error: $error")
                    startWakeWordDetection()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val command = matches[0].lowercase()
                        Log.d(tag, "Command received: $command")
                        handleCommand(command)
                    }
                    startWakeWordDetection()
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun handleCommand(command: String) {
        when {
            "next" in command -> spotifyController.next()
            "pause" in command || "stop" in command -> spotifyController.pause()
            "play" in command && command.length > 5 -> {
                Log.d(tag, "Search command detected: $command")
            }
            "play" in command -> spotifyController.play()
            "previous" in command || "back" in command -> spotifyController.previous()
        }
    }

    private fun isAppInForeground(packageName: String): Boolean {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        if (stats != null && stats.isNotEmpty()) {
            val sortedStats = stats.toMutableList().sortedBy { it.lastTimeUsed }
            return sortedStats.last().packageName == packageName
        }
        return false
    }

    private fun startForegroundService() {
        val channelId = "VoiceAssistantChannel"
        val channel = NotificationChannel(channelId, "Voice Assistant Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Spotify Assistant")
            .setContentText("Listening for 'Hey Spotify'...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(foregroundCheckRunnable)
        stopWakeWordDetection()
        speechRecognizer?.destroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}