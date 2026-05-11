package com.example.shops.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.shops.data.GoalDatabase
import com.example.shops.data.GoalEntity
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.roundToInt

object ReminderScheduler {

    suspend fun syncAllReminders(context: Context) = withContext(Dispatchers.IO) {
        val goals = GoalDatabase.getInstance(context).goalDao().getAllGoals()
        goals.forEach { goal ->
            syncReminders(context, goal.toReminderUiModel())
        }
    }

    fun syncReminders(context: Context, goal: GoalUiModel) {
        cancelAllRemindersForGoal(context, goal.id)
        
        if (!goal.reminderEnabled) return

        when (goal.category) {
            GoalCategory.WATER -> scheduleWaterReminders(context, goal)
            GoalCategory.WALKING -> scheduleEndOfDayReminder(context, goal)
            GoalCategory.WAKEUP -> goal.wakeupTime?.let {
                scheduleSingleReminder(
                    context = context,
                    requestId = goal.id,
                    goal = goal,
                    title = "Wake-up reminder",
                    message = "It's time to wake up for ${goal.finalDisplayName}.",
                    time = it,
                    isAlarm = true
                )
            }
            GoalCategory.SLEEPING -> goal.sleepTime?.let {
                scheduleSingleReminder(
                    context = context,
                    requestId = goal.id,
                    goal = goal,
                    title = "Sleep reminder",
                    message = "It's time to wind down for ${goal.finalDisplayName}.",
                    time = it
                )
            }
            GoalCategory.OTHER -> {
                goal.multipleReminders.forEachIndexed { index, time ->
                    scheduleSingleReminder(
                        context = context,
                        requestId = "${goal.id}_$index",
                        goal = goal,
                        title = "Custom reminder",
                        message = "Reminder for ${goal.finalDisplayName}.",
                        time = time
                    )
                }
            }
            else -> {
                scheduleSingleReminder(
                    context = context,
                    requestId = goal.id,
                    goal = goal,
                    title = "Goal reminder",
                    message = "Time to check in for ${goal.finalDisplayName}."
                )
            }
        }
    }

    private fun scheduleWaterReminders(context: Context, goal: GoalUiModel) {
        val creationTimeAnchor = if (goal.startDate == goal.createdAt.toLocalDate()) {
            goal.createdAt.toLocalTime()
        } else {
            null
        }
        val start = maxOf(goal.wakeupTime ?: LocalTime.of(7, 0), creationTimeAnchor ?: LocalTime.MIN)
        val end = goal.sleepTime ?: LocalTime.of(22, 0)

        val glasses = goal.targetValue.roundToInt().coerceAtLeast(1)

        if (glasses <= 0) return

        val startMinutes = start.hour * 60 + start.minute
        val endMinutes = end.hour * 60 + end.minute
        val duration = if (endMinutes > startMinutes) endMinutes - startMinutes else (24 * 60 - startMinutes) + endMinutes
        
        val interval = duration / glasses

        for (i in 1..glasses) {
            val reminderTime = start.plusMinutes((i * interval).toLong())
            scheduleSingleReminder(
                context = context,
                requestId = "${goal.id}_$i",
                goal = goal,
                title = "Water reminder",
                message = "Time to drink water. Glass $i of $glasses.",
                time = reminderTime
            )
        }
    }

    private fun scheduleEndOfDayReminder(context: Context, goal: GoalUiModel) {
        scheduleSingleReminder(
            context = context,
            requestId = goal.id,
            goal = goal,
            title = "Walking check-in",
            message = "Did you reach ${goal.targetValue.toInt()} steps today?",
            time = LocalTime.of(21, 0)
        )
    }

    private fun scheduleSingleReminder(
        context: Context,
        requestId: String,
        goal: GoalUiModel,
        title: String,
        message: String,
        time: LocalTime = LocalTime.of(goal.reminderHour, goal.reminderMinute),
        isAlarm: Boolean = false
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerAt(goal, time) ?: return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_GOAL_ID, goal.id)
            putExtra(ReminderReceiver.EXTRA_GOAL_NAME, goal.finalDisplayName)
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, requestId)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_BODY, message)
            if (isAlarm) putExtra("IS_ALARM", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAllRemindersForGoal(context: Context, goalId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildCancelIntent(context, goalId))
        for (i in 0..64) {
            alarmManager.cancel(buildCancelIntent(context, "${goalId}_$i"))
        }
    }

    fun scheduleSnoozeReminder(
        context: Context,
        goalId: String,
        reminderId: String,
        message: String,
        delayMinutes: Long = 5L
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = LocalDateTime.now().plusMinutes(delayMinutes)
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_GOAL_ID, goalId)
            putExtra(ReminderReceiver.EXTRA_GOAL_NAME, message)
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderReceiver.EXTRA_IS_SNOOZED, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "snooze_${goalId}_$reminderId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun buildCancelIntent(context: Context, id: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerAt(goal: GoalUiModel, time: LocalTime): LocalDateTime? {
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        if (today.isAfter(goal.endDate)) return null

        val baseDate = when {
            today.isBefore(goal.startDate) -> goal.startDate
            else -> today
        }

        val scheduledTime = if (baseDate.isAfter(goal.createdAt.toLocalDate())) {
            time
        } else {
            maxOf(time, goal.createdAt.toLocalTime())
        }

        var triggerAt = LocalDateTime.of(baseDate, scheduledTime)
        if (!triggerAt.isAfter(now)) {
            triggerAt = triggerAt.plusDays(1)
        }

        return if (triggerAt.toLocalDate().isAfter(goal.endDate)) null else triggerAt
    }
}

internal fun GoalEntity.toReminderUiModel(): GoalUiModel {
    val goalCategory = GoalCategory.valueOf(category)
    return GoalUiModel(
        id = id,
        name = name,
        category = goalCategory,
        customCategoryName = customCategoryName,
        type = com.example.shops.model.GoalType.valueOf(type),
        targetValue = targetValue,
        currentValue = 0f,
        unit = unit,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        createdAt = LocalDateTime.parse(createdAt),
        glassSizeMl = glassSizeMl,
        wakeupTime = wakeupTime?.let(LocalTime::parse),
        sleepTime = sleepTime?.let(LocalTime::parse),
        reminderEnabled = reminderEnabled,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        multipleReminders = multipleReminders
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map(LocalTime::parse)
            ?: emptyList()
    )
}
