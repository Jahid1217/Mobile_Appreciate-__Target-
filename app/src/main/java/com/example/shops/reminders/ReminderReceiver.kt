package com.example.shops.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.shops.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val goalId = intent.getStringExtra(EXTRA_GOAL_ID) ?: return
        val goalName = intent.getStringExtra(EXTRA_GOAL_NAME) ?: "Target"
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: goalId

        val popupIntent = Intent(context, ReminderPopupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_GOAL_ID, goalId)
            putExtra(EXTRA_GOAL_NAME, goalName)
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            popupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reminder")
            .setContentText(goalName)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Target: $goalName"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "goal_reminders"
        const val EXTRA_GOAL_ID = "extra_goal_id"
        const val EXTRA_GOAL_NAME = "extra_goal_name"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
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
            }
            manager.createNotificationChannel(channel)
        }
    }
}
