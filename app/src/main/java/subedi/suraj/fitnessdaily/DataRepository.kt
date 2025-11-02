package subedi.suraj.fitnessdaily.repository

import android.content.Context
import android.widget.Toast
import subedi.suraj.fitnessdaily.model.Achievement
import subedi.suraj.fitnessdaily.model.AchievementType
import subedi.suraj.fitnessdaily.model.Meal
import subedi.suraj.fitnessdaily.model.Workout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DataRepository {
    private val workouts = mutableListOf<Workout>()
    private val meals = mutableListOf<Meal>()
    private val favoriteWorkoutTemplates = mutableListOf<Workout>()

    // Achievement tracking fields
    private val achievements = mutableListOf<Achievement>()
    private var workoutCount = 0
    private var mealCount = 0
    private var goalsCompleted = 0
    private var currentStreak = 0
    private var lastWorkoutDate: String? = null

    // Context for showing Toast
    private var appContext: Context? = null

    init {
        initializeAchievements()
    }

    fun initializeAppContext(context: Context) {
        appContext = context.applicationContext
        loadAchievementProgress()
    }

    // Achievement methods - Remove R.drawable references for now
    private fun initializeAchievements() {
        achievements.addAll(
            listOf(
                Achievement("ach_1", "First Steps", "Complete your first workout", AchievementType.WORKOUT_COUNT, 1, 0), // 0 as placeholder
                Achievement("ach_2", "Regular Runner", "Complete 10 workouts", AchievementType.WORKOUT_COUNT, 10, 0),
                Achievement("ach_3", "Fitness Fanatic", "Complete 50 workouts", AchievementType.WORKOUT_COUNT, 50, 0),
                Achievement("ach_4", "Healthy Eater", "Log 20 meals", AchievementType.MEAL_COUNT, 20, 0),
                Achievement("ach_5", "Goal Getter", "Complete 5 goals", AchievementType.GOAL_COMPLETED, 5, 0),
                Achievement("ach_6", "Week Warrior", "7-day workout streak", AchievementType.STREAK_DAYS, 7, 0),
                Achievement("ach_7", "Month Master", "30-day workout streak", AchievementType.STREAK_DAYS, 30, 0)
            )
        )
    }

    private fun loadAchievementProgress() {
        val sharedPreferences = appContext?.getSharedPreferences("achievements", Context.MODE_PRIVATE)
        workoutCount = sharedPreferences?.getInt("workout_count", 0) ?: 0
        mealCount = sharedPreferences?.getInt("meal_count", 0) ?: 0
        goalsCompleted = sharedPreferences?.getInt("goals_completed", 0) ?: 0
        currentStreak = sharedPreferences?.getInt("current_streak", 0) ?: 0
        lastWorkoutDate = sharedPreferences?.getString("last_workout_date", null)

        // Load earned achievements
        achievements.forEach { achievement ->
            val isEarned = sharedPreferences?.getBoolean("achievement_${achievement.id}_earned", false) ?: false
            if (isEarned) {
                achievement.earned = true
                val earnedDateMillis = sharedPreferences.getLong("achievement_${achievement.id}_date", 0)
                if (earnedDateMillis > 0) {
                    achievement.earnedDate = Date(earnedDateMillis)
                }
            }
        }
    }

    private fun saveAchievementProgress() {
        val sharedPreferences = appContext?.getSharedPreferences("achievements", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.apply {
            putInt("workout_count", workoutCount)
            putInt("meal_count", mealCount)
            putInt("goals_completed", goalsCompleted)
            putInt("current_streak", currentStreak)
            putString("last_workout_date", lastWorkoutDate)

            // Save earned achievements
            achievements.forEach { achievement ->
                if (achievement.earned) {
                    putBoolean("achievement_${achievement.id}_earned", true)
                    achievement.earnedDate?.let {
                        putLong("achievement_${achievement.id}_date", it.time)
                    }
                }
            }
            apply()
        }
    }

    // Workout methods
    fun addWorkout(workout: Workout) {
        workouts.add(workout)
        workoutCount++
        updateStreak()
        checkAchievements()
        saveAchievementProgress()
    }

    fun getWorkouts(): List<Workout> = workouts.toList()

    fun getFavoriteWorkoutTemplates(): List<Workout> = favoriteWorkoutTemplates.toList()

    fun addFavoriteTemplate(workout: Workout) {
        if (!favoriteWorkoutTemplates.any {
                it.name == workout.name &&
                        it.duration == workout.duration &&
                        it.caloriesBurned == workout.caloriesBurned
            }) {
            favoriteWorkoutTemplates.add(workout.copy(isFavorite = true))
        }
    }

    fun removeFavoriteTemplate(workout: Workout) {
        favoriteWorkoutTemplates.removeAll {
            it.name == workout.name &&
                    it.duration == workout.duration &&
                    it.caloriesBurned == workout.caloriesBurned
        }
    }

    fun isFavoriteTemplate(workout: Workout): Boolean {
        return favoriteWorkoutTemplates.any {
            it.name == workout.name &&
                    it.duration == workout.duration &&
                    it.caloriesBurned == workout.caloriesBurned
        }
    }

    fun updateWorkoutFavorite(workoutId: Long, isFavorite: Boolean) {
        val index = workouts.indexOfFirst { it.id == workoutId }
        if (index != -1) {
            workouts[index] = workouts[index].copy(isFavorite = isFavorite)
        }
    }

    fun removeWorkoutFromAllFavorites(workoutName: String, duration: Int, caloriesBurned: Int) {
        favoriteWorkoutTemplates.removeAll {
            it.name == workoutName &&
                    it.duration == duration &&
                    it.caloriesBurned == caloriesBurned
        }

        workouts.forEachIndexed { index, w ->
            if (w.name == workoutName && w.duration == duration && w.caloriesBurned == caloriesBurned) {
                workouts[index] = w.copy(isFavorite = false)
            }
        }
    }

    fun getLast30DaysWorkouts(): List<Workout> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.time

        return workouts.filter { it.date.after(startDate) || it.date == startDate }
    }

    fun getTotalCaloriesBurned(): Int {
        return getLast30DaysWorkouts().sumOf { it.caloriesBurned }
    }

    fun getTotalWorkouts(): Int {
        return getLast30DaysWorkouts().size
    }

    // Meal methods
    fun addMeal(meal: Meal) {
        meals.add(meal)
        mealCount++
        checkAchievements()
        saveAchievementProgress()
    }

    fun getMeals(): List<Meal> = meals.toList()

    fun getLast30DaysMeals(): List<Meal> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.time

        return meals.filter { it.date.after(startDate) || it.date == startDate }
    }

    fun getAverageDailyCalories(): Int {
        val meals = getLast30DaysMeals()
        return if (meals.isNotEmpty()) meals.sumOf { it.calories } / 30 else 0
    }

    fun getTotalProtein(): Double {
        return getLast30DaysMeals().sumOf { it.protein }
    }

    fun getTotalCarbs(): Double {
        return getLast30DaysMeals().sumOf { it.carbs }
    }

    fun getTotalFat(): Double {
        return getLast30DaysMeals().sumOf { it.fat }
    }

    // Achievement tracking methods
    fun completeGoal() {
        goalsCompleted++
        checkAchievements()
        saveAchievementProgress()
    }

    private fun updateStreak() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastWorkoutDate == today) {
            return // Already logged today
        }

        if (lastWorkoutDate == getYesterdayDate()) {
            currentStreak++
        } else {
            currentStreak = 1
        }

        lastWorkoutDate = today
        checkAchievements()
    }

    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun checkAchievements() {
        val today = Date()
        var newAchievement: Achievement? = null

        achievements.forEach { achievement ->
            if (!achievement.earned) {
                when (achievement.type) {
                    AchievementType.WORKOUT_COUNT -> {
                        if (workoutCount >= achievement.milestone) {
                            achievement.earned = true
                            achievement.earnedDate = today
                            newAchievement = achievement
                        }
                    }
                    AchievementType.MEAL_COUNT -> {
                        if (mealCount >= achievement.milestone) {
                            achievement.earned = true
                            achievement.earnedDate = today
                            newAchievement = achievement
                        }
                    }
                    AchievementType.GOAL_COMPLETED -> {
                        if (goalsCompleted >= achievement.milestone) {
                            achievement.earned = true
                            achievement.earnedDate = today
                            newAchievement = achievement
                        }
                    }
                    AchievementType.STREAK_DAYS -> {
                        if (currentStreak >= achievement.milestone) {
                            achievement.earned = true
                            achievement.earnedDate = today
                            newAchievement = achievement
                        }
                    }
                }
            }
        }

        // Show notification for new achievement
        newAchievement?.let {
            showAchievementNotification(it)
        }
    }

    private fun showAchievementNotification(achievement: Achievement) {
        appContext?.let { context ->
            Toast.makeText(
                context,
                "🎉 Achievement Unlocked: ${achievement.title}! - ${achievement.description}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Achievement getter methods
    fun getAchievements(): List<Achievement> = achievements.toList()

    fun getEarnedAchievements(): List<Achievement> = achievements.filter { it.earned }

    fun getAchievementProgress(): Map<String, Int> {
        return mapOf(
            "workout_count" to workoutCount,
            "meal_count" to mealCount,
            "goals_completed" to goalsCompleted,
            "current_streak" to currentStreak
        )
    }

    // Clear all data
    fun clearAllData() {
        workouts.clear()
        meals.clear()
        favoriteWorkoutTemplates.clear()

        // Reset achievement progress
        workoutCount = 0
        mealCount = 0
        goalsCompleted = 0
        currentStreak = 0
        lastWorkoutDate = null

        // Reset all achievements
        achievements.forEach {
            it.earned = false
            it.earnedDate = null
        }

        // Clear shared preferences
        appContext?.getSharedPreferences("achievements", Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
        saveAchievementProgress()
    }
}