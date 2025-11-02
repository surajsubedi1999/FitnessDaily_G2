package subedi.suraj.fitnessdaily

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
    private lateinit var btnAchievements: Button

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
        btnAchievements = findViewById(R.id.btnAchievements)
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

        btnAchievements.setOnClickListener {
            showAchievementsDialog()
        }
    }

    // UPDATED: Show achievements dialog with real data
    private fun showAchievementsDialog() {
        val achievements = DataRepository.getAchievements()
        val earnedAchievements = DataRepository.getEarnedAchievements()
        val progress = DataRepository.getAchievementProgress()

        val dialogView = layoutInflater.inflate(R.layout.dialog_achievements, null)
        val achievementsContainer = dialogView.findViewById<android.widget.LinearLayout>(R.id.achievementsContainer)
        val tvProgress = dialogView.findViewById<android.widget.TextView>(R.id.tvAchievementProgress)

        // Update progress text with real data
        tvProgress.text = "🎯 Your Progress: ${earnedAchievements.size}/${achievements.size} achievements earned"

        // Clear existing views
        achievementsContainer.removeAllViews()

        // Add each achievement to the dialog with real progress data
        achievements.forEach { achievement ->
            val achievementView = layoutInflater.inflate(R.layout.item_achievement, null)
            val tvTitle = achievementView.findViewById<android.widget.TextView>(R.id.tvAchievementTitle)
            val tvDescription = achievementView.findViewById<android.widget.TextView>(R.id.tvAchievementDescription)
            val tvProgress = achievementView.findViewById<android.widget.TextView>(R.id.tvAchievementProgress)
            val progressBar = achievementView.findViewById<android.widget.ProgressBar>(R.id.progressBarAchievement)
            val ivStatus = achievementView.findViewById<android.widget.ImageView>(R.id.ivAchievementStatus)

            tvTitle.text = achievement.title
            tvDescription.text = achievement.description

            // Get REAL progress data from DataRepository
            val (currentProgress, progressText) = when (achievement.type) {
                subedi.suraj.fitnessdaily.model.AchievementType.WORKOUT_COUNT -> {
                    val workoutCount = progress["workout_count"] ?: 0
                    val text = "Workouts completed: $workoutCount/${achievement.milestone}"
                    Pair(workoutCount, text)
                }
                subedi.suraj.fitnessdaily.model.AchievementType.MEAL_COUNT -> {
                    val mealCount = progress["meal_count"] ?: 0
                    val text = "Meals logged: $mealCount/${achievement.milestone}"
                    Pair(mealCount, text)
                }
                subedi.suraj.fitnessdaily.model.AchievementType.GOAL_COMPLETED -> {
                    val goalsCompleted = progress["goals_completed"] ?: 0
                    val text = "Goals completed: $goalsCompleted/${achievement.milestone}"
                    Pair(goalsCompleted, text)
                }
                subedi.suraj.fitnessdaily.model.AchievementType.STREAK_DAYS -> {
                    val currentStreak = progress["current_streak"] ?: 0
                    val text = "Current streak: $currentStreak/${achievement.milestone} days"
                    Pair(currentStreak, text)
                }
            }

            // Calculate progress percentage
            val progressPercentage = if (achievement.milestone > 0) {
                (currentProgress * 100) / achievement.milestone
            } else {
                0
            }.coerceAtMost(100)

            tvProgress.text = progressText
            progressBar.progress = progressPercentage

            // Set status based on real earned status
            if (achievement.earned) {
                ivStatus.setImageResource(android.R.drawable.presence_online)
                ivStatus.setColorFilter(android.graphics.Color.parseColor("#4CAF50"))
                tvTitle.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                achievementView.alpha = 1.0f

                // Show earned date if available
                achievement.earnedDate?.let { date ->
                    val dateText = "\nEarned on: ${android.text.format.DateFormat.getDateFormat(this).format(date)}"
                    tvProgress.text = tvProgress.text.toString() + dateText
                }
            } else {
                ivStatus.setImageResource(android.R.drawable.presence_invisible)
                ivStatus.setColorFilter(android.graphics.Color.parseColor("#9E9E9E"))
                tvTitle.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
                achievementView.alpha = 0.7f
            }

            achievementsContainer.addView(achievementView)
        }

        // Add real statistics summary
        val summaryView = layoutInflater.inflate(R.layout.item_achievement_summary, null)
        val tvWorkouts = summaryView.findViewById<android.widget.TextView>(R.id.tvWorkoutsSummary)
        val tvMeals = summaryView.findViewById<android.widget.TextView>(R.id.tvMealsSummary)
        val tvGoals = summaryView.findViewById<android.widget.TextView>(R.id.tvGoalsSummary)
        val tvStreak = summaryView.findViewById<android.widget.TextView>(R.id.tvStreakSummary)

        tvWorkouts.text = "Total Workouts: ${progress["workout_count"] ?: 0}"
        tvMeals.text = "Total Meals: ${progress["meal_count"] ?: 0}"
        tvGoals.text = "Completed Goals: ${progress["goals_completed"] ?: 0}"
        tvStreak.text = "Current Streak: ${progress["current_streak"] ?: 0} days"

        achievementsContainer.addView(summaryView)

        AlertDialog.Builder(this)
            .setTitle("🏆 Your Achievements")
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Refresh") { dialog, _ ->
                showAchievementsDialog() // Refresh data
            }
            .create()
            .show()
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
            .setMessage("Are you sure you want to delete all your data? This includes:\n\n• All workout records\n• All meal logs\n• All fitness goals\n• All progress data\n• All achievement badges\n\nThis action cannot be undone.")
            .setPositiveButton("Delete All") { dialog, which ->
                deleteAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAllData() {
        try {
            // Clear all data from DataRepository (including achievements)
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
            "user_preferences",
            "achievements"
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