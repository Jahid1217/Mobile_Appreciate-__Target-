package com.example.shops.ui.screens.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.ui.components.AppScreenHeader
import com.example.shops.ui.components.CheckInCard
import com.example.shops.ui.components.EmptyState
import com.example.shops.ui.components.InfoCard
import com.example.shops.ui.theme.ShopsTheme
import java.time.LocalDate

@Composable
fun CheckInScreen(
    goals: List<GoalUiModel>,
    onIncrement: (GoalUiModel) -> Unit,
    onComplete: (GoalUiModel) -> Unit,
    onNegative: (GoalUiModel) -> Unit
) {
    val active = goals.filter {
        LocalDate.now() in it.startDate..it.endDate && it.progress < 1f
    }
    var goalToConfirmComplete by remember { mutableStateOf<GoalUiModel?>(null) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))
        AppScreenHeader(
            title = "Daily check-in",
            subtitle = "Log small actions consistently. Quick updates compound faster than occasional perfect days.",
            trailingContent = {
                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.surface) {
                    Icon(
                        imageVector = Icons.Rounded.Today,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        )
        Spacer(Modifier.height(16.dp))
        InfoCard(
            icon = Icons.Rounded.TaskAlt,
            color = MaterialTheme.colorScheme.secondary,
            text = "${active.size} active goal${if (active.size == 1) "" else "s"} remaining for ${LocalDate.now()}."
        )
        Spacer(Modifier.height(12.dp))
        if (active.isEmpty()) {
            EmptyState(Icons.Rounded.TaskAlt, "Nothing to check in today!\nAdd a goal to get started.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(active, key = { it.id }) { goal ->
                    CheckInCard(
                        goal = goal,
                        onIncrement = { onIncrement(goal) },
                        onComplete = { goalToConfirmComplete = goal },
                        onNegative = { onNegative(goal) }
                    )
                }
            }
        }
    }

    goalToConfirmComplete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToConfirmComplete = null },
            icon = { Icon(Icons.Rounded.TaskAlt, contentDescription = null) },
            title = { Text("Confirm completion") },
            text = { Text("Mark \"${goal.finalDisplayName}\" as complete for today? All remaining sets for this day will be completed and reminders will stop.") },
            confirmButton = {
                TextButton(onClick = {
                    onComplete(goal)
                    goalToConfirmComplete = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToConfirmComplete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckInScreenPreview() {
    ShopsTheme {
        CheckInScreen(
            goals = listOf(
                GoalUiModel(
                    name = "Drink Water",
                    category = GoalCategory.WATER,
                    type = GoalType.DAILY,
                    targetValue = 20f,
                    currentValue = 5f,
                    unit = "glasses",
                    startDate = LocalDate.now().minusDays(1),
                    endDate = LocalDate.now().plusDays(30),
                    glassSizeMl = 250,
                    reminderEnabled = true,
                    reminderHour = 8,
                    reminderMinute = 0
                )

            ),
            onIncrement = {},
            onComplete = {},
            onNegative = {}
        )
    }
}
