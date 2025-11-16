package subedi.suraj.fitnessdaily

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import subedi.suraj.fitnessdaily.repository.DataRepository
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    private lateinit var btnDeleteAllData: Button
    private lateinit var btnShareProgress: Button

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
        btnShareProgress = findViewById(R.id.btnShareProgress)
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

        btnShareProgress.setOnClickListener {
            shareProgress()
        }
    }

    private fun shareProgress() {
        try {
            val shareMessage = createProgressShareMessage()

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                type = "text/plain"
            }

            val shareChooser = Intent.createChooser(shareIntent, "Share Your Fitness Progress")
            startActivity(shareChooser)

        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing progress", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createProgressShareMessage(): String {
        val workouts = DataRepository.getLast30DaysWorkouts()
        val totalWorkouts = workouts.size
        val totalCalories = workouts.sumOf { it.caloriesBurned }
        val currentStreak = calculateCurrentStreak()
        val favoriteWorkouts = DataRepository.getFavoriteWorkoutTemplates()

        val message = StringBuilder()
        message.append("🏋️ My Fitness Progress from FitnessDaily! 🏋️\n\n")

        message.append("🔥 Current Streak: $currentStreak days\n")
        message.append("💪 Recent Workouts (30 days): $totalWorkouts workouts\n")
        message.append("🔥 Calories Burned: $totalCalories\n")

        if (favoriteWorkouts.isNotEmpty()) {
            message.append("⭐ Favorite Workouts:\n")
            favoriteWorkouts.take(3).forEach { workout ->
                message.append("• ${workout.name} (${workout.duration}min)\n")
            }
        }

        val recentAchievement = getRecentAchievement(currentStreak, totalWorkouts)
        if (recentAchievement.isNotEmpty()) {
            message.append("\n🎯 $recentAchievement\n")
        }

        message.append("\nKeep pushing! Let's get fit together! 💥")

        return message.toString()
    }

    private fun calculateCurrentStreak(): Int {
        val workouts = DataRepository.getWorkouts().sortedByDescending { it.date }
        if (workouts.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        var streak = 0
        var currentDate = calendar.time

        val todayWorkout = workouts.firstOrNull {
            isSameDay(it.date, currentDate)
        }
        if (todayWorkout != null) {
            streak++
        } else {
            return 0
        }

        for (i in 1 until workouts.size) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val previousDate = calendar.time

            val hasWorkoutOnDay = workouts.any { isSameDay(it.date, previousDate) }
            if (hasWorkoutOnDay) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getRecentAchievement(streak: Int, totalWorkouts: Int): String {
        return when {
            streak >= 7 -> "🔥 7-Day Streak! Amazing consistency!"
            streak >= 3 -> "🚀 3-Day Streak! Keep it up!"
            totalWorkouts >= 10 -> "💪 10+ Workouts this month! Great progress!"
            totalWorkouts >= 5 -> "⭐ 5 Workouts completed! Building momentum!"
            else -> "🌟 Fitness journey started! Every step counts!"
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
            DataRepository.clearAllData()
            clearAllSharedPreferences()
            showSuccessMessage()
        } catch (e: Exception) {
            Toast.makeText(this, "Error deleting data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearAllSharedPreferences() {
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