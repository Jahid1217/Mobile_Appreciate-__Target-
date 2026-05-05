package com.example.shops.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class GoalType(val color: Color) {
    DAILY(Color(0xFF0F766E)),
    MONTHLY(Color(0xFFC2410C)),
    YEARLY(Color(0xFF1D4ED8))
}

data class GoalUiModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val type: GoalType,
    val targetValue: Float,
    val currentValue: Float,
    val unit: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int
) {
    val progress: Float
        get() = if (targetValue > 0f) (currentValue / targetValue).coerceIn(0f, 1f) else 0f

    val reminderLabel: String
        get() = "%02d:%02d".format(reminderHour, reminderMinute)

    val dateRangeLabel: String
        get() = "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)}"
}

data class MissedReportItem(
    val goalId: String,
    val goalName: String,
    val missedDates: List<LocalDate>
)

data class GoalsUiState(
    val goals: List<GoalUiModel> = emptyList(),
    val missedReports: List<MissedReportItem> = emptyList()
)

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
