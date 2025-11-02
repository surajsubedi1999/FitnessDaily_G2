package subedi.suraj.fitnessdaily.repository

import subedi.suraj.fitnessdaily.model.Meal
import subedi.suraj.fitnessdaily.model.Workout
import java.util.Calendar
import java.util.Date

object DataRepository {
    private val workouts = mutableListOf<Workout>()
    private val meals = mutableListOf<Meal>()
    private val favoriteWorkoutTemplates = mutableListOf<Workout>()

    // Workout methods
    fun addWorkout(workout: Workout) {
        workouts.add(workout)
    }

    fun getWorkouts(): List<Workout> = workouts.toList()

    fun getFavoriteWorkoutTemplates(): List<Workout> = favoriteWorkoutTemplates.toList()

    fun addFavoriteTemplate(workout: Workout) {
        // Check if template already exists to avoid duplicates ONLY in favorites
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

    // New method to update workout favorite status in the main workouts list
    fun updateWorkoutFavorite(workoutId: Long, isFavorite: Boolean) {
        val index = workouts.indexOfFirst { it.id == workoutId }
        if (index != -1) {
            workouts[index] = workouts[index].copy(isFavorite = isFavorite)
        }
    }

    // New method to remove workout from all favorites and update all matching workouts
    fun removeWorkoutFromAllFavorites(workoutName: String, duration: Int, caloriesBurned: Int) {
        // Remove from favorite templates
        favoriteWorkoutTemplates.removeAll {
            it.name == workoutName &&
                    it.duration == duration &&
                    it.caloriesBurned == caloriesBurned
        }

        // Update all workouts with same details to not favorite
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

    // ADD THIS METHOD FOR DELETING ALL DATA
    fun clearAllData() {
        workouts.clear()
        meals.clear()
        favoriteWorkoutTemplates.clear()
    }
}