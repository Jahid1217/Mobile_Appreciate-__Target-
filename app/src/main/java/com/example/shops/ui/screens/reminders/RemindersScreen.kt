package com.example.shops.ui.screens.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.model.GoalsUiState
import com.example.shops.ui.components.InfoCard
import com.example.shops.ui.components.ReminderGoalCard
import com.example.shops.ui.components.SectionLabel
import com.example.shops.ui.theme.ShopsTheme
import java.time.LocalDate

@Composable
fun RemindersScreen(
    uiState: GoalsUiState,
    onReminderChange: (GoalUiModel, Boolean, Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item {
            Text("Reminders & Reports", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        if (uiState.goals.isNotEmpty()) {
            item { SectionLabel("Your Goals") }
            items(uiState.goals, key = { it.id }) { goal ->
                ReminderGoalCard(goal = goal)
            }
        }
        item { Text("Missed Dates Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (uiState.missedReports.isEmpty()) {
            item {
                InfoCard(Icons.Rounded.CheckCircle, Color(0xFF15803D), "No missed dates recorded. Great work!")
            }
        } else {
            items(uiState.missedReports) { report ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Text(report.goalName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Text(report.missedDates.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RemindersScreenPreview() {
    ShopsTheme {
        RemindersScreen(
            uiState = GoalsUiState(
                goals = listOf(
                    GoalUiModel(
                        name = "Drink Water",
                        category = GoalCategory.WATER,
                        type = GoalType.DAILY,
                        targetValue = 20f,
                        currentValue = 10f,
                        unit = "glasses",
                        startDate = LocalDate.now().minusDays(1),
                        endDate = LocalDate.now().plusDays(30),
                        glassSizeMl = 250,
                        reminderEnabled = true,
                        reminderHour = 8,
                        reminderMinute = 0
                    )
                ),
                missedReports = emptyList()
            ),
            onReminderChange = { _, _, _, _ -> }
        )
    }
}
