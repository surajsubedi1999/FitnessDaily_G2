package subedi.suraj.fitnessdaily.model

data class MotivationalQuote(
    val id: Int,
    val text: String,
    val category: QuoteCategory
)

enum class QuoteCategory {
    GENERAL,
    WORKOUT,
    NUTRITION,
    GOALS,
    PERSISTENCE
}