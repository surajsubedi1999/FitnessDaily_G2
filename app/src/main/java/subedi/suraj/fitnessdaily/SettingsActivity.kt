package subedi.suraj.fitnessdaily

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import subedi.suraj.fitnessdaily.repository.DataRepository

class SettingsActivity : AppCompatActivity() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    private lateinit var btnDeleteAllData: Button

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
        btnDeleteAllData = findViewById(R.id.btnDeleteAllData)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveThemeSettings()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnDeleteAllData.setOnClickListener {
            showDeleteConfirmationDialog()
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

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Data")
            .setMessage("Are you sure you want to delete all your data? This includes:\n\n• All workout records\n• All meal logs\n• All fitness goals\n• All progress data\n\nThis action cannot be undone.")
            .setPositiveButton("Delete All") { dialog, which ->
                deleteAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAllData() {
        try {
            // Clear all data from DataRepository
            DataRepository.clearAllData()

            // Clear all SharedPreferences
            clearAllSharedPreferences()

            showSuccessMessage()

        } catch (e: Exception) {
            Toast.makeText(this, "Error deleting data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearAllSharedPreferences() {
        // List all SharedPreferences files used by the app
        val prefsToClear = listOf(
            "app_settings",
            "fitness_data",
            "goals_data",
            "nutrition_data",
            "workout_data",
            "user_preferences"
        )

        prefsToClear.forEach { prefName ->
            getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
    }

    private fun showSuccessMessage() {
        AlertDialog.Builder(this)
            .setTitle("Data Deleted")
            .setMessage("All your data has been successfully deleted. The app will now restart.")
            .setPositiveButton("OK") { dialog, which ->
                restartApp()
            }
            .setCancelable(false)
            .show()
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}