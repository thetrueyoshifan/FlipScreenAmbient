package com.gcti.flipscreenambient
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class SettingsActivity : AppCompatActivity() {

    private lateinit var fontSpinner: Spinner
    private lateinit var clockSizeInput: EditText
    private lateinit var dateSizeInput: EditText
    private lateinit var textColorInput: EditText
    private lateinit var textShadowCheck: CheckBox
    private lateinit var clock24hCheck: CheckBox
    private lateinit var classicClockCheck: CheckBox
    private lateinit var selectBackgroundButton: Button
    private lateinit var saveButton: Button
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(uri)
            if (savedPath != null) {
                val preferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
                preferences.edit().putString("background_image_path", savedPath).apply()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val intent = intent
        if (Intent.ACTION_SEND == intent.action && intent.type?.startsWith("image/") == true) {
            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (imageUri != null) {
                handleSharedImage(imageUri)
            }
        }

        fontSpinner = findViewById(R.id.font_spinner)
        clockSizeInput = findViewById(R.id.clock_size_input)
        dateSizeInput = findViewById(R.id.date_size_input)
        textColorInput = findViewById(R.id.text_color_input)
        textShadowCheck = findViewById(R.id.text_shadow_check)
        clock24hCheck = findViewById(R.id.clock_24h_check)
        classicClockCheck = findViewById(R.id.classic_clock_check)
        selectBackgroundButton = findViewById(R.id.select_background_button)
        saveButton = findViewById(R.id.save_button)

        setupFontSpinner()

        loadPreferences()

        selectBackgroundButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            savePreferences()
            Handler(Looper.getMainLooper()).postDelayed({
                restartApp()
            }, 100)
        }
    }

    private fun handleSharedImage(uri: Uri) {
        val savedPath = saveImageToInternalStorage(uri)
        if (savedPath != null) {
            val preferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
            preferences.edit().putString("background_image_path", savedPath).apply()

            Toast.makeText(this, "Image saved as background", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFontSpinner() {
        var fonts = File("/system/fonts").listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
        fonts += listOf("sans-serif-light", "sans-serif", "serif", "monospace")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fonts)
        fontSpinner.adapter = adapter
    }

    private fun loadPreferences() {
        val preferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        clockSizeInput.setText(preferences.getFloat("clock_size", 40f).toString())
        dateSizeInput.setText(preferences.getFloat("date_size", 20f).toString())
        textColorInput.setText(preferences.getString("text_color", "#FFFFFF"))
        textShadowCheck.isChecked = preferences.getBoolean("text_shadow", true)
        clock24hCheck.isChecked = preferences.getBoolean("24h_clock", true)
        classicClockCheck.isChecked = preferences.getBoolean("classic_clock", false)

        val font = preferences.getString("clock_font", "sans-serif-light") ?: "sans-serif-light"
        val fontIndex = (fontSpinner.adapter as ArrayAdapter<String>).getPosition(font)
        fontSpinner.setSelection(fontIndex)
    }

    private fun savePreferences() {
        val preferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val editor = preferences.edit()

        val clockSize = clockSizeInput.text.toString().toFloatOrNull() ?: 40f
        val dateSize = dateSizeInput.text.toString().toFloatOrNull() ?: 20f
        val textColor = textColorInput.text.toString()
        val textShadow = textShadowCheck.isChecked
        val clock24h = clock24hCheck.isChecked
        val classicClock = classicClockCheck.isChecked
        val selectedFont = fontSpinner.selectedItem as String

        editor.putFloat("clock_size", clockSize)
        editor.putFloat("date_size", dateSize)
        editor.putString("clock_font", selectedFont)
        editor.putString("text_color", textColor)
        editor.putBoolean("text_shadow", textShadow)
        editor.putBoolean("24h_clock", clock24h)
        editor.putBoolean("classic_clock", classicClock)
        editor.apply()

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                val savedPath = saveImageToInternalStorage(imageUri)
                if (savedPath != null) {
                    val preferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
                    preferences.edit().putString("background_image_path", savedPath).apply()
                }
            }
        }
    }


    private fun saveImageToInternalStorage(uri: Uri): String? {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val file = File(filesDir, "background_image.png")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            outputStream.close()
            inputStream?.close()

            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}