package com.example.shops.reminders

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.example.shops.GoalsViewModel
import com.example.shops.model.GoalUiModel
import com.example.shops.ui.theme.ShopsTheme

class ReminderPopupActivity : ComponentActivity() {
    private val goalsViewModel: GoalsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContent {
            ShopsTheme {
                val uiState by goalsViewModel.uiState.collectAsState()
                val goalId = intent.getStringExtra(ReminderReceiver.EXTRA_GOAL_ID).orEmpty()
                val reminderId = intent.getStringExtra(ReminderReceiver.EXTRA_REMINDER_ID) ?: goalId
                val title = intent.getStringExtra(ReminderReceiver.EXTRA_NOTIFICATION_TITLE).orEmpty()
                val prompt = intent.getStringExtra(ReminderReceiver.EXTRA_NOTIFICATION_BODY)
                    ?: intent.getStringExtra(ReminderReceiver.EXTRA_GOAL_NAME).orEmpty()
                val goal = uiState.goals.firstOrNull { it.id == goalId }

                ReminderPopupContent(
                    goal = goal,
                    title = title,
                    prompt = prompt,
                    dailyCheckInCount = uiState.dailyCheckInCount,
                    onDone = {
                        goal?.let { goalsViewModel.incrementGoal(it) }
                        NotificationManagerCompat.from(this@ReminderPopupActivity).cancel(reminderId.hashCode())
                        finish()
                    },
                    onLater = {
                        val target = goal?.finalDisplayName ?: prompt.ifBlank { "Target" }
                        ReminderScheduler.scheduleSnoozeReminder(
                            context = this@ReminderPopupActivity,
                            goalId = goalId,
                            reminderId = reminderId,
                            message = target,
                            delayMinutes = 5L
                        )
                        NotificationManagerCompat.from(this@ReminderPopupActivity).cancel(reminderId.hashCode())
                        finish()
                    },
                    onClose = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun ReminderPopupContent(
    goal: GoalUiModel?,
    title: String,
    prompt: String,
    dailyCheckInCount: Int,
    onDone: () -> Unit,
    onLater: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.42f)),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title.ifBlank { "Reminder" }, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(goal?.finalDisplayName ?: "Target", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = prompt.ifBlank { "It's time to check in on this target." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Daily Check-in", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("$dailyCheckInCount completed today", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Done")
                        }
                        Button(
                            onClick = onLater,
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Rounded.Snooze, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Later")
                        }
                    }

                    Text(
                        text = "You can close this popup any time.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
