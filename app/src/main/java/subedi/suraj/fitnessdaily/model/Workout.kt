package subedi.suraj.fitnessdaily.model

import java.util.Date

data class Workout(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val duration: Int,
    val caloriesBurned: Int,
    val date: Date = Date(),
    val exercises: List<Exercise> = emptyList()
)

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double? = null
)