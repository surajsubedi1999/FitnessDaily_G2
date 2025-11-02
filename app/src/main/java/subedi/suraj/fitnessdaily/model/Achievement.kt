package subedi.suraj.fitnessdaily.model

import java.util.Date

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val milestone: Int,
    val iconResId: Int = 0, // Default to 0, we'll handle icons separately
    var earned: Boolean = false,
    var earnedDate: Date? = null
) {
    // Helper method to get icon resource name
    fun getIconResourceName(): String {
        return when (id) {
            "ach_1" -> "ic_achievement_first"
            "ach_2" -> "ic_achievement_regular"
            "ach_3" -> "ic_achievement_fanatic"
            "ach_4" -> "ic_achievement_eater"
            "ach_5" -> "ic_achievement_goals"
            "ach_6" -> "ic_achievement_streak"
            "ach_7" -> "ic_achievement_master"
            else -> "ic_achievement_default"
        }
    }
}

enum class AchievementType {
    WORKOUT_COUNT,
    MEAL_COUNT,
    GOAL_COMPLETED,
    STREAK_DAYS
}