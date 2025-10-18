package subedi.suraj.fitnessdaily.repository

import subedi.suraj.fitnessdaily.model.Meal
import subedi.suraj.fitnessdaily.model.Workout
import java.util.Calendar
import java.util.Date

object DataRepository {
    private val workouts = mutableListOf<Workout>()
    private val meals = mutableListOf<Meal>()

    // Workout methods
    fun addWorkout(workout: Workout) {
        workouts.add(workout)
    }

    fun getWorkouts(): List<Workout> = workouts.toList()

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
}