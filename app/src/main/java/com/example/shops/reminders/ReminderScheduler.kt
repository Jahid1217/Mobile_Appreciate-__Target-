package com.example.shops.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.shops.model.GoalUiModel
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {
    fun syncReminder(context: Context, goal: GoalUiModel) {
        if (goal.reminderEnabled) {
            scheduleReminder(context, goal)
        } else {
            cancelReminder(context, goal.id)
        }
    }

    fun cancelReminder(context: Context, goalId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context, goalId, ""))
    }

    private fun scheduleReminder(context: Context, goal: GoalUiModel) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = LocalDateTime.now()
        var triggerAt = now.withHour(goal.reminderHour).withMinute(goal.reminderMinute).withSecond(0).withNano(0)
        if (!triggerAt.isAfter(now)) {
            triggerAt = triggerAt.plusDays(1)
        }

        alarmManager.cancel(buildPendingIntent(context, goal.id, goal.name))
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            buildPendingIntent(context, goal.id, goal.name)
        )
    }

    private fun buildPendingIntent(context: Context, goalId: String, goalName: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_GOAL_ID, goalId)
            putExtra(ReminderReceiver.EXTRA_GOAL_NAME, goalName)
        }
        return PendingIntent.getBroadcast(
            context,
            goalId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
