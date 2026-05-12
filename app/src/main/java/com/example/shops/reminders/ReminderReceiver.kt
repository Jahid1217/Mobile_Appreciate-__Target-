package com.example.shops.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.shops.data.GoalDatabase
import com.example.shops.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val goalId = intent.getStringExtra(EXTRA_GOAL_ID) ?: return
        val goalName = intent.getStringExtra(EXTRA_GOAL_NAME) ?: "Target"
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: goalId
        val notificationTitle = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE) ?: "Reminder"
        val notificationBody = intent.getStringExtra(EXTRA_NOTIFICATION_BODY)
            ?: "Time to check in for $goalName."

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val goalEntity = GoalDatabase.getInstance(context).goalDao().getGoalById(goalId)
                if (goalEntity == null) {
                    return@launch
                }

                val today = LocalDate.now().toString()
                val checkIns = GoalDatabase.getInstance(context).goalDao().getCheckInsForGoalOnDate(goalId, today)
                val totalCompletedToday = checkIns.sumOf { it.value.toDouble() }.toFloat()

                if (totalCompletedToday >= goalEntity.targetValue) {
                    ReminderScheduler.cancelAllRemindersForGoal(context.applicationContext, goalId)
                    return@launch
                }

                val popupIntent = Intent(context, ReminderPopupActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_GOAL_ID, goalId)
                    putExtra(EXTRA_GOAL_NAME, goalName)
                    putExtra(EXTRA_REMINDER_ID, reminderId)
                    putExtra(EXTRA_NOTIFICATION_TITLE, notificationTitle)
                    putExtra(EXTRA_NOTIFICATION_BODY, notificationBody)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    reminderId.hashCode(),
                    popupIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setContentIntent(pendingIntent)
                    .setFullScreenIntent(pendingIntent, true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setAutoCancel(true)
                    .build()

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
                }
                ReminderScheduler.syncReminders(context.applicationContext, goalEntity.toReminderUiModel())
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "goal_reminders_v2"
        const val EXTRA_GOAL_ID = "extra_goal_id"
        const val EXTRA_GOAL_NAME = "extra_goal_name"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
        const val EXTRA_NOTIFICATION_BODY = "extra_notification_body"
        const val EXTRA_IS_SNOOZED = "extra_is_snoozed"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Goal reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders for tracked targets."
                setShowBadge(true)
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            manager.createNotificationChannel(channel)
        }
    }
}
