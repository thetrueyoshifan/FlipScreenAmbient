package com.gcti.flipscreenambient

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.util.TypedValue
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class OverlayAccessibilityService : AccessibilityService() {

    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager
    private lateinit var displayManager: DisplayManager
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var preferences: SharedPreferences


    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("OverlayService", "Service connected")
        handler.postDelayed({
            try {
                // Load preferences and apply them
                preferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
                setupOverlayForDisplay(1)
            } catch (e: Exception) {
                Log.e("OverlayService", "Failed to set up overlay: ${e.message}")
            }
        }, 500)

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("OverlayService", "Accessibility event received: ${event?.eventType}")
    }

    override fun onInterrupt() {
        Log.w("OverlayService", "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
        Log.d("OverlayService", "Service destroyed")
    }


    private fun setupOverlayForDisplay(displayId: Int) {
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(displayId)

        if (display == null) {
            Log.e("OverlayService", "Display ID $displayId not found.")
            return
        }
        // Register the display listener
        displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
                Log.d("OverlayService", "Display added: $displayId")
            }

            override fun onDisplayRemoved(displayId: Int) {
                Log.d("OverlayService", "Display removed: $displayId")
            }

            override fun onDisplayChanged(displayId: Int) {
                Log.d("OverlayService", "Display changed: $displayId")

                val display = displayManager.getDisplay(displayId)
                if (display != null && display.state == Display.STATE_ON) {
                    Log.d("OverlayService", "Display $displayId is ON, updating")
                    updateScreenContents()
                }
            }
        }, null)
        val displayContext = createDisplayContext(display)
        windowManager = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d("OverlayService", "Using displayContext for display ID $displayId")


        overlayView = LayoutInflater.from(displayContext).inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = android.view.Gravity.CENTER

        try {
            windowManager.addView(overlayView, params)
            Log.d("OverlayService", "Overlay added to display ID $displayId")
        } catch (e: Exception) {
            Log.e("OverlayService", "Error adding overlay: ${e.message}")
        }

        processPreferences()
        startClock()
        updateScreenContents()
    }

    private fun processPreferences() {
        val clockText: TextView? = overlayView?.findViewById(R.id.clock_text)
        val dateText: TextView? = overlayView?.findViewById(R.id.date_text)
        val batteryText: TextView? = overlayView?.findViewById(R.id.battery_percentage)
        val batteryIcon: ImageView? = overlayView?.findViewById(R.id.battery_icon)
        val backgroundImage: ImageView? = overlayView?.findViewById(R.id.background_image)

        val clockSize = preferences.getFloat("clock_size", 40f)
        val clockFont = preferences.getString("clock_font", "sans-serif-light") ?: "sans-serif-light"
        clockText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, clockSize)
        clockText?.typeface = if (!listOf("sans-serif-light", "sans-serif", "serif", "monospace").contains(clockFont)) {
            Typeface.createFromFile("/system/fonts/$clockFont.ttf")
        } else {
            Typeface.create(clockFont, Typeface.NORMAL)
        }
        dateText?.typeface = if (!listOf("sans-serif-light", "sans-serif", "serif", "monospace").contains(clockFont)) {
            Typeface.createFromFile("/system/fonts/$clockFont.ttf")
        } else {
            Typeface.create(clockFont, Typeface.NORMAL)
        }
        batteryText?.typeface = if (!listOf("sans-serif-light", "sans-serif", "serif", "monospace").contains(clockFont)) {
            Typeface.createFromFile("/system/fonts/$clockFont.ttf")
        } else {
            Typeface.create(clockFont, Typeface.NORMAL)
        }
        val textColor = Color.parseColor(preferences.getString("text_color", "#ffffff")) ?: Color.WHITE
        val textShadow = preferences.getBoolean("text_shadow", true) ?: true
        clockText?.setTextColor(textColor)
        dateText?.setTextColor(textColor)
        batteryText?.setTextColor(textColor)
        batteryIcon?.setColorFilter(textColor)
        if (textShadow) {
            clockText?.setShadowLayer(6f, 3f, 3f, Color.parseColor("#80000000"))
            dateText?.setShadowLayer(4f, 2f, 2f, Color.parseColor("#80000000"))
            batteryText?.setShadowLayer(2f, 1f, 1f, Color.parseColor("#80000000"))
        }
        val dateSize = preferences.getFloat("date_size", 20f)
        dateText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize)

        val backgroundPath = preferences.getString("background_image_path", null)
        if (backgroundPath != null) {
            val file = File(backgroundPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                backgroundImage?.setImageBitmap(bitmap)
            }
        }
    }

    private fun startClock() {
        val clockUpdater = object : Runnable {
            override fun run() {
               if (displayManager.displays.any{it.state != Display.STATE_OFF}) {
                    updateScreenContents()
               }
               handler.postDelayed(this, 1000)
            }
        }
        handler.post(clockUpdater)
    }

    private fun updateScreenContents() {
        val clockText: TextView? = overlayView?.findViewById(R.id.clock_text)
        val dateText: TextView? = overlayView?.findViewById(R.id.date_text)
        val batteryPercentage: TextView? = overlayView?.findViewById(R.id.battery_percentage)
        val batteryIcon: ImageView? = overlayView?.findViewById(R.id.battery_icon)

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

        clockText?.text = currentTime
        dateText?.text = currentDate

        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, intentFilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val charging = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

        val batteryPct = (level / scale.toFloat() * 100).toInt()
        batteryPercentage?.text = "$batteryPct%"

        val batteryIndex = (batteryPct / 20).coerceIn(0, 5)
        batteryIcon?.setImageResource(
            listOf(
                R.drawable.ic_battery_0,
                R.drawable.ic_battery_1,
                R.drawable.ic_battery_2,
                R.drawable.ic_battery_3,
                R.drawable.ic_battery_4,
                R.drawable.ic_battery_5
            )[batteryIndex]
        )
        if (charging != 0)
            batteryIcon?.setColorFilter(Color.GREEN)
        else
            batteryIcon?.setColorFilter(Color.parseColor(preferences.getString("text_color", "#ffffff")))
    }

}
