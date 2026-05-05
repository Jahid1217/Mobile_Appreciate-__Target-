package com.example.shops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shops.data.CheckInEntity
import com.example.shops.data.GoalDatabase
import com.example.shops.data.GoalEntity
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.model.GoalsUiState
import com.example.shops.model.MissedReportItem
import com.example.shops.reminders.ReminderReceiver
import com.example.shops.reminders.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = GoalDatabase.getInstance(application).goalDao()

    val uiState: StateFlow<GoalsUiState> = combine(
        dao.getGoalsFlow(),
        dao.getCheckInsFlow()
    ) { goals, checkIns ->
        val today = LocalDate.now()
        val mappedGoals = goals.map { goal ->
            val currentValue = currentValueForGoal(goal, checkIns, today)
            goal.toUiModel(currentValue)
        }
        val reports = mappedGoals.map { goal ->
            MissedReportItem(
                goalId = goal.id,
                goalName = goal.name,
                missedDates = missedDatesForGoal(goal, checkIns, today)
            )
        }.filter { it.missedDates.isNotEmpty() }

        GoalsUiState(goals = mappedGoals, missedReports = reports)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    init {
        ReminderReceiver.createNotificationChannel(application)
    }

    fun saveGoal(
        id: String?,
        name: String,
        targetValue: Float,
        unit: String,
        type: GoalType,
        startDate: LocalDate,
        endDate: LocalDate,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        val entity = GoalEntity(
            id = id ?: UUID.randomUUID().toString(),
            name = name,
            category = "General",
            type = type.name,
            targetValue = targetValue,
            unit = unit,
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            reminderEnabled = reminderEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )

        viewModelScope.launch {
            dao.upsertGoal(entity)
            ReminderScheduler.syncReminder(getApplication(), entity.toUiModel(0f))
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            dao.deleteGoal(goalId)
            ReminderScheduler.cancelReminder(getApplication(), goalId)
        }
    }

    fun incrementGoal(goal: GoalUiModel) {
        val remaining = (goal.targetValue - goal.currentValue).coerceAtLeast(0f)
        if (remaining <= 0f) return

        viewModelScope.launch {
            dao.insertCheckIn(
                CheckInEntity(
                    goalId = goal.id,
                    entryDate = LocalDate.now().toString(),
                    value = remaining.coerceAtMost(1f)
                )
            )
        }
    }

    fun completeGoal(goal: GoalUiModel) {
        val remaining = (goal.targetValue - goal.currentValue).coerceAtLeast(0f)
        if (remaining <= 0f) return

        viewModelScope.launch {
            dao.insertCheckIn(
                CheckInEntity(
                    goalId = goal.id,
                    entryDate = LocalDate.now().toString(),
                    value = remaining
                )
            )
        }
    }

    fun updateReminder(goal: GoalUiModel, enabled: Boolean, hour: Int, minute: Int) {
        saveGoal(
            id = goal.id,
            name = goal.name,
            targetValue = goal.targetValue,
            unit = goal.unit,
            type = goal.type,
            startDate = goal.startDate,
            endDate = goal.endDate,
            reminderEnabled = enabled,
            reminderHour = hour,
            reminderMinute = minute
        )
    }

    private fun currentValueForGoal(
        goal: GoalEntity,
        checkIns: List<CheckInEntity>,
        today: LocalDate
    ): Float {
        return checkIns
            .asSequence()
            .filter { it.goalId == goal.id }
            .filter { matchesPeriod(goal.type, LocalDate.parse(it.entryDate), today) }
            .sumOf { it.value.toDouble() }
            .toFloat()
    }

    private fun missedDatesForGoal(
        goal: GoalUiModel,
        checkIns: List<CheckInEntity>,
        today: LocalDate
    ): List<LocalDate> {
        val lastDate = minOf(goal.endDate, today.minusDays(1))
        if (lastDate.isBefore(goal.startDate)) return emptyList()

        val totalsByDate = checkIns
            .asSequence()
            .filter { it.goalId == goal.id }
            .groupBy { LocalDate.parse(it.entryDate) }
            .mapValues { (_, entries) -> entries.sumOf { it.value.toDouble() }.toFloat() }

        return generateSequence(goal.startDate) { current ->
            current.plusDays(1).takeIf { !it.isAfter(lastDate) }
        }.filter { date ->
            (totalsByDate[date] ?: 0f) < goal.targetValue
        }.toList()
    }

    private fun matchesPeriod(type: String, entryDate: LocalDate, today: LocalDate): Boolean {
        return when (GoalType.valueOf(type)) {
            GoalType.DAILY -> entryDate == today
            GoalType.MONTHLY -> entryDate.year == today.year && entryDate.month == today.month
            GoalType.YEARLY -> entryDate.year == today.year
        }
    }
}

private fun GoalEntity.toUiModel(currentValue: Float): GoalUiModel {
    return GoalUiModel(
        id = id,
        name = name,
        category = category,
        type = GoalType.valueOf(type),
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        reminderEnabled = reminderEnabled,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute
    )
}
