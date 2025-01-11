package com.gcti.flipscreenambient
import android.util.Log
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val enableButton = findViewById<Button>(R.id.enable_overlay_button)

        enableButton.setOnClickListener {
            requestOverlayPermission()
            if (!isAccessibilityServiceEnabled(this, OverlayAccessibilityService::class.java)) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                Toast.makeText(this, "Enable the 'Overlay Service' in Accessibility Settings.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Overlay Service is already enabled.", Toast.LENGTH_SHORT).show()
                startServiceManually()
            }
        }
        val settingsButton = findViewById<Button>(R.id.settings_button)

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
    }
    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val accessibilityEnabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val serviceId = "${context.packageName}/${service.name}"
        return accessibilityEnabled?.contains(serviceId) == true
    }

    private fun startServiceManually() {
        try {
            val intent = Intent(this, OverlayAccessibilityService::class.java)
            startService(intent)
            Log.d("MainActivity", "Service start intent sent")
            Toast.makeText(this, "Overlay Service started.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start Overlay Service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
