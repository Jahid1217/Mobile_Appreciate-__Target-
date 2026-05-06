package com.example.shops.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.math.pow

enum class GoalType(val color: Color) {
    DAILY(Color(0xFF0F766E)),
    MONTHLY(Color(0xFFC2410C)),
    YEARLY(Color(0xFF1D4ED8))
}

enum class GoalCategory(val displayName: String) {
    WATER("Water"),
    WORKING("Working"),
    WALKING("Walking"),
    SLEEPING("Sleeping"),
    WAKEUP("Wakeup"),
    MONEY_SAVING("Money Saving"),
    BOOK_READING("Book Reading"),
    OTHER("Other")
}

data class GoalUiModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: GoalCategory,
    val customCategoryName: String? = null,
    val type: GoalType,
    val targetValue: Float,
    val currentValue: Float,
    val unit: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val glassSizeMl: Int? = null,
    val wakeupTime: LocalTime? = null,
    val sleepTime: LocalTime? = null,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val multipleReminders: List<LocalTime> = emptyList()
) {
    val progress: Float
        get() = if (targetValue > 0f) (currentValue / targetValue).coerceIn(0f, 1f) else 0f

    val displayTargetValue: Int
        get() = if (category == GoalCategory.WATER) targetValue.roundToInt() else targetValue.toInt()

    val displayCurrentValue: Int
        get() = if (category == GoalCategory.WATER) currentValue.roundToInt() else currentValue.toInt()

    val displayUnit: String
        get() = if (category == GoalCategory.WATER) "glasses" else unit

    val reminderLabel: String
        get() = "%02d:%02d".format(reminderHour, reminderMinute)

    val dateRangeLabel: String
        get() = "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)}"
    
    val finalDisplayName: String
        get() = if (category == GoalCategory.OTHER && !customCategoryName.isNullOrBlank()) customCategoryName else name
}

data class MissedReportItem(
    val goalId: String,
    val goalName: String,
    val missedDates: List<LocalDate>
)

data class GoalsUiState(
    val goals: List<GoalUiModel> = emptyList(),
    val missedReports: List<MissedReportItem> = emptyList(),
    val profile: UserProfileUiModel? = null,
    val dailyCheckInCount: Int = 0
)

enum class ScreenTimeStatus(
    val label: String,
    val guideline: String
) {
    OPTIMAL(
        label = "Optimal",
        guideline = "Aim to keep recreational screen exposure close to 2 hours per day when possible."
    ),
    HEALTHY(
        label = "Within Healthy Limit",
        guideline = "Try to stay under 4 hours daily and take a short break every 30 to 60 minutes."
    ),
    HIGH(
        label = "Exceeds Recommended Limit",
        guideline = "Reduce non-essential screen time, add movement breaks, and avoid long continuous sessions."
    )
}

data class ScreenTimeSummary(
    val totalMillis: Long,
    val status: ScreenTimeStatus
) {
    val totalMinutes: Long
        get() = totalMillis / 60_000L

    val formattedDuration: String
        get() {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }
}

data class UserProfileUiModel(
    val id: String = "current_profile",
    val name: String = "",
    val email: String = "",
    val profilePictureUri: String? = null,
    val age: Int? = null,
    val bloodGroup: String = "",
    val gender: String = "",
    val weightKg: Float? = null,
    val heightCm: Float? = null
) {
    val bmi: Float?
        get() {
            val weight = weightKg ?: return null
            val height = heightCm ?: return null
            if (height <= 0f) return null
            val meters = height / 100f
            return weight / meters.pow(2)
        }

    val bmiLabel: String
        get() = bmi?.let { "%.1f".format(it) } ?: "--"

    val healthStatus: String
        get() {
            val bmiValue = bmi ?: return "Add details"
            return when {
                bmiValue < 18.5f -> "Underweight"
                bmiValue < 25f -> "Normal"
                bmiValue < 30f -> "Overweight"
                else -> "Obese"
            }
        }

    val healthStatusColor: Color
        get() {
            val bmiValue = bmi ?: return Color(0xFF64748B)
            return when {
                bmiValue < 18.5f -> Color(0xFF2563EB)
                bmiValue < 25f -> Color(0xFF15803D)
                bmiValue < 30f -> Color(0xFFF59E0B)
                else -> Color(0xFFDC2626)
            }
        }
}

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
