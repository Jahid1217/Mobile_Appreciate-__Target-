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
            GoalCategory.WAKEUP -> goal.wakeupTime?.let { scheduleSingleReminder(context, goal.id, goal.id, goal.finalDisplayName, it, isAlarm = true) }
            GoalCategory.SLEEPING -> goal.sleepTime?.let { scheduleSingleReminder(context, goal.id, goal.id, goal.finalDisplayName, it) }
            GoalCategory.OTHER -> {
                goal.multipleReminders.forEachIndexed { index, time ->
                    scheduleSingleReminder(context, "${goal.id}_$index", goal.id, goal.finalDisplayName, time)
                }
            }
            else -> {
                val time = LocalTime.of(goal.reminderHour, goal.reminderMinute)
                scheduleSingleReminder(context, goal.id, goal.id, goal.finalDisplayName, time)
            }
        }
    }

    private fun scheduleWaterReminders(context: Context, goal: GoalUiModel) {
        val start = goal.wakeupTime ?: LocalTime.of(7, 0)
        val end = goal.sleepTime ?: LocalTime.of(22, 0)
        
        val glasses = if (goal.glassSizeMl != null && goal.glassSizeMl > 0) {
            (goal.targetValue * 1000 / goal.glassSizeMl).toInt()
        } else 8

        if (glasses <= 0) return

        val startMinutes = start.hour * 60 + start.minute
        val endMinutes = end.hour * 60 + end.minute
        val duration = if (endMinutes > startMinutes) endMinutes - startMinutes else (24 * 60 - startMinutes) + endMinutes
        
        val interval = duration / glasses
        
        for (i in 1..glasses) {
            val reminderTime = start.plusMinutes((i * interval).toLong())
            scheduleSingleReminder(context, "${goal.id}_$i", goal.id, "Time to drink water! (Glass $i/$glasses)", reminderTime)
        }
    }

    private fun scheduleEndOfDayReminder(context: Context, goal: GoalUiModel) {
        // Schedule for 9 PM
        scheduleSingleReminder(context, goal.id, goal.id, "Steps Check: Did you reach your goal of ${goal.targetValue.toInt()} steps?", LocalTime.of(21, 0))
    }

    private fun scheduleSingleReminder(
        context: Context,
        requestId: String,
        goalId: String,
        message: String,
        time: LocalTime,
        isAlarm: Boolean = false
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = LocalDateTime.now()
        var triggerAt = LocalDateTime.of(LocalDate.now(), time)
        
        if (!triggerAt.isAfter(now)) {
            triggerAt = triggerAt.plusDays(1)
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_GOAL_ID, goalId)
            putExtra(ReminderReceiver.EXTRA_GOAL_NAME, message)
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, requestId)
            if (isAlarm) putExtra("IS_ALARM", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelAllRemindersForGoal(context: Context, goalId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Cancel base ID
            alarmManager.cancel(buildCancelIntent(context, goalId))
        // Cancel possible multiple reminders (up to 24 for water/other)
        for (i in 0..24) {
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
}

private fun GoalEntity.toReminderUiModel(): GoalUiModel {
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
