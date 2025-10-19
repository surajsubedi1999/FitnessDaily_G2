//pawanphuyal author
package subedi.suraj.fitnessdaily.model

import java.util.Date

data class FitnessGoal(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val deadline: Date? = null,
    val goalType: GoalType,
    val isCompleted: Boolean = false
)

enum class GoalType {
    WEIGHT_LOSS, MUSCLE_GAIN, ENDURANCE, FLEXIBILITY
}