package subedi.suraj.fitnessdaily

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        setupClickListeners()
        loadCurrentTheme()
    }

    private fun initializeViews() {
        radioGroupTheme = findViewById(R.id.radioGroupTheme)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveThemeSettings()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentTheme() {
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> radioGroupTheme.check(R.id.radioLight)
            AppCompatDelegate.MODE_NIGHT_YES -> radioGroupTheme.check(R.id.radioDark)
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> radioGroupTheme.check(R.id.radioSystem)
        }
    }

    private fun saveThemeSettings() {
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val selectedTheme = when (radioGroupTheme.checkedRadioButtonId) {
            R.id.radioLight -> AppCompatDelegate.MODE_NIGHT_NO
            R.id.radioDark -> AppCompatDelegate.MODE_NIGHT_YES
            R.id.radioSystem -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        sharedPreferences.edit().putInt("theme_mode", selectedTheme).apply()
        AppCompatDelegate.setDefaultNightMode(selectedTheme)

        // Restart the app to apply theme properly
        restartApp()
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}