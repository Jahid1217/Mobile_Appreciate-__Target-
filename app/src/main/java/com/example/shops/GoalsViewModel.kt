package com.example.shops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shops.data.CheckInEntity
import com.example.shops.data.GoalDatabase
import com.example.shops.data.GoalEntity
import com.example.shops.data.UserProfileEntity
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.model.GoalsUiState
import com.example.shops.model.MissedReportItem
import com.example.shops.model.UserProfileUiModel
import com.example.shops.reminders.ReminderReceiver
import com.example.shops.reminders.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = GoalDatabase.getInstance(application).goalDao()
    private val profileDao = GoalDatabase.getInstance(application).userProfileDao()

    val uiState: StateFlow<GoalsUiState> = combine(
        dao.getGoalsFlow(),
        dao.getCheckInsFlow(),
        profileDao.observeProfileFlow()
    ) { goals, checkIns, profile ->
        val today = LocalDate.now()
        val mappedGoals = goals.map { goal ->
            val currentValue = currentValueForGoal(goal, checkIns, today)
            goal.toUiModel(currentValue)
        }
        val dailyCheckInCount = checkIns.count { it.entryDate == today.toString() }
        val reports = mappedGoals.map { goal ->
            MissedReportItem(
                goalId = goal.id,
                goalName = goal.finalDisplayName,
                missedDates = missedDatesForGoal(goal, checkIns, today)
            )
        }.filter { it.missedDates.isNotEmpty() }

        GoalsUiState(
            goals = mappedGoals,
            missedReports = reports,
            profile = profile?.toUiModel(),
            dailyCheckInCount = dailyCheckInCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    init {
        ReminderReceiver.createNotificationChannel(application)
        viewModelScope.launch {
            ReminderScheduler.syncAllReminders(application)
        }
    }

    fun saveGoal(goal: GoalUiModel) {
        val entity = goal.toEntity()
        viewModelScope.launch {
            dao.upsertGoal(entity)
            ReminderScheduler.syncReminders(getApplication(), goal)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            dao.deleteGoal(goalId)
            ReminderScheduler.cancelAllRemindersForGoal(getApplication(), goalId)
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

    fun decrementGoal(goal: GoalUiModel) {
        if (goal.currentValue <= 0f) return

        viewModelScope.launch {
            dao.insertCheckIn(
                CheckInEntity(
                    goalId = goal.id,
                    entryDate = LocalDate.now().toString(),
                    value = -1f
                )
            )
        }
    }

    fun updateReminder(goal: GoalUiModel, enabled: Boolean, hour: Int, minute: Int) {
        saveGoal(goal.copy(reminderEnabled = enabled, reminderHour = hour, reminderMinute = minute))
    }

    fun saveProfile(profile: UserProfileUiModel) {
        viewModelScope.launch {
            profileDao.upsertProfile(profile.toEntity())
        }
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
    val goalCategory = GoalCategory.valueOf(category)
    val normalizedTargetValue = if (goalCategory == GoalCategory.WATER) {
        normalizeWaterTargetToGlasses(targetValue, glassSizeMl, unit)
    } else {
        targetValue
    }

    return GoalUiModel(
        id = id,
        name = name,
        category = goalCategory,
        customCategoryName = customCategoryName,
        type = GoalType.valueOf(type),
        targetValue = normalizedTargetValue,
        currentValue = currentValue,
        unit = if (goalCategory == GoalCategory.WATER) "glasses" else unit,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        glassSizeMl = glassSizeMl,
        wakeupTime = wakeupTime?.let { LocalTime.parse(it) },
        sleepTime = sleepTime?.let { LocalTime.parse(it) },
        reminderEnabled = reminderEnabled,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        multipleReminders = multipleReminders?.split(",")?.filter { it.isNotBlank() }?.map { LocalTime.parse(it) } ?: emptyList()
    )
}

private fun GoalUiModel.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        name = name,
        category = category.name,
        customCategoryName = customCategoryName,
        type = type.name,
        targetValue = targetValue,
        unit = if (category == GoalCategory.WATER) "glasses" else unit,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        glassSizeMl = glassSizeMl,
        wakeupTime = wakeupTime?.toString(),
        sleepTime = sleepTime?.toString(),
        reminderEnabled = reminderEnabled,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        multipleReminders = multipleReminders.joinToString(",") { it.toString() }
    )
}

private fun UserProfileUiModel.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        name = name,
        email = email,
        profilePictureUri = profilePictureUri,
        age = age,
        bloodGroup = bloodGroup.ifBlank { null },
        gender = gender.ifBlank { null },
        weightKg = weightKg,
        heightCm = heightCm
    )
}

private fun UserProfileEntity.toUiModel(): UserProfileUiModel {
    return UserProfileUiModel(
        id = id,
        name = name,
        email = email,
        profilePictureUri = profilePictureUri,
        age = age,
        bloodGroup = bloodGroup.orEmpty(),
        gender = gender.orEmpty(),
        weightKg = weightKg,
        heightCm = heightCm
    )
}

private fun normalizeWaterTargetToGlasses(
    targetValue: Float,
    glassSizeMl: Int?,
    unit: String
): Float {
    if (glassSizeMl == null || glassSizeMl <= 0) return targetValue
    if (!unit.equals("Liters", ignoreCase = true)) return targetValue
    return ((targetValue * 1000f) / glassSizeMl).coerceAtLeast(1f)
}
