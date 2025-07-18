// file: app/src/main/java/com/yourname/heyspotify/MainActivity.kt
package com.h4rsh.botify

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Your UI layout file

        requestPermissions()
    }

    private fun requestPermissions() {
        PermissionX.init(this)
            .permissions(Manifest.permission.RECORD_AUDIO)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "This app needs microphone access to listen for the wake word.", "OK", "Cancel")
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    // After microphone permission, check for Usage Stats permission
                    checkUsageStatsPermission()
                } else {
                    Toast.makeText(this, "Microphone permission is required to run the assistant", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
    }

    private fun checkUsageStatsPermission() {
        if (!isUsageStatsPermissionGranted()) {
            Toast.makeText(this, "Please grant Usage Access permission to detect Spotify", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        } else {
            startVoiceAssistant()
        }
    }

    // Helper to check if the permission is already granted
    private fun isUsageStatsPermissionGranted(): Boolean {
        return try {
            val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    private fun startVoiceAssistant() {
        Toast.makeText(this, "Permissions granted! Starting assistant.", Toast.LENGTH_SHORT).show()
        val serviceIntent = Intent(this, VoiceAssistantService::class.java)
        startForegroundService(serviceIntent)
        // You can finish the activity if you don't need a UI
        finish()
    }

    // When the user returns from the settings screen, check again
    override fun onResume() {
        super.onResume()
        if (isUsageStatsPermissionGranted()) {
            startVoiceAssistant()
        }
    }
}