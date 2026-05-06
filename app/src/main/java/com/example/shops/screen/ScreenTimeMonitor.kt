package com.example.shops.screen

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import com.example.shops.model.ScreenTimeStatus
import com.example.shops.model.ScreenTimeSummary
import java.time.LocalDate
import java.time.ZoneId

object ScreenTimeMonitor {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun hasUsageAccess(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTodayScreenTime(context: Context): ScreenTimeSummary? {
        if (!hasUsageAccess(context)) return null

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val zoneId = ZoneId.systemDefault()
        val startOfDay = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(startOfDay, now)
        val event = UsageEvents.Event()

        var lastInteractiveAt: Long? = null
        var totalInteractiveMillis = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    lastInteractiveAt = event.timeStamp.coerceAtLeast(startOfDay)
                }

                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    val startedAt = lastInteractiveAt ?: continue
                    totalInteractiveMillis += (event.timeStamp - startedAt).coerceAtLeast(0L)
                    lastInteractiveAt = null
                }
            }
        }

        lastInteractiveAt?.let { startedAt ->
            totalInteractiveMillis += (now - startedAt).coerceAtLeast(0L)
        }

        return ScreenTimeSummary(
            totalMillis = totalInteractiveMillis,
            status = when {
                totalInteractiveMillis <= 2L * 60L * 60L * 1000L -> ScreenTimeStatus.OPTIMAL
                totalInteractiveMillis <= 4L * 60L * 60L * 1000L -> ScreenTimeStatus.HEALTHY
                else -> ScreenTimeStatus.HIGH
            }
        )
    }
}
