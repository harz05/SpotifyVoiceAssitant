// file: app/src/main/java/com/yourname/heyspotify/SpotifyController.kt
package com.h4rsh.botify

import android.content.Context
import android.content.Intent
import android.view.KeyEvent

class SpotifyController(private val context: Context) {

    // A simple way to control local playback without the full SDK
    private fun sendMediaKey(keyCode: Int) {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            // Target Spotify specifically to avoid controlling other media players
            setPackage("com.spotify.music")
        }
        context.sendBroadcast(intent)

        // We need to send an ACTION_UP event too
        val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, keyCode))
            setPackage("com.spotify.music")
        }
        context.sendBroadcast(upIntent)
    }

    fun next() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun pause() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
    }

    fun play() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    fun previous() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    // We will add the Web API search function here later
}