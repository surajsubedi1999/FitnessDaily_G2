package subedi.suraj.fitnessdaily.model

import java.util.Date

data class Meal(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val date: Date = Date(),
    val mealType: MealType
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}