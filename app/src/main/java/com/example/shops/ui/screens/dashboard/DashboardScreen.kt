package com.example.shops.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.model.UserProfileUiModel
import com.example.shops.ui.components.AppScreenHeader
import com.example.shops.ui.components.EmptyState
import com.example.shops.ui.components.GoalCard
import com.example.shops.ui.components.HighlightBanner
import com.example.shops.ui.components.SectionLabel
import com.example.shops.ui.components.StatCard
import com.example.shops.ui.components.profile.DashboardProfileHeader
import com.example.shops.ui.theme.ShopsTheme
import java.time.LocalDate

@Composable
fun DashboardScreen(
    goals: List<GoalUiModel>,
    profile: UserProfileUiModel?,
    dailyCheckInCount: Int,
    onOpenCheckIn: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenGoalLog: (GoalUiModel) -> Unit
) {
    val today = LocalDate.now()
    val activeGoals = goals.filter { today in it.startDate..it.endDate }
    val avgProgress = if (activeGoals.isNotEmpty()) activeGoals.map { it.progress }.average() else 0.0
    val completed = activeGoals.count { it.progress >= 1f }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item {
            AppScreenHeader(
                title = "Progress at a glance",
                subtitle = if (profile?.name.isNullOrBlank()) {
                    "Track your health routines, savings, and daily habits in one place."
                } else {
                    "Welcome back, ${profile?.name}. Here is how today's goals are shaping up."
                }
            )
        }
        item { DashboardProfileHeader(profile = profile, onOpenProfile = onOpenProfile) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Active", activeGoals.size.toString(), Icons.Rounded.TrendingUp, Modifier.weight(1f))
                StatCard("Progress", "${(avgProgress * 100).toInt()}%", Icons.Rounded.Analytics, Modifier.weight(1f))
                StatCard("Done", "$completed", Icons.Rounded.CheckCircle, Modifier.weight(1f))
            }
        }
//        item {
//            HighlightBanner(
//                icon = Icons.Rounded.TaskAlt,
//                title = "Daily check-in is ready",
//                description = "You have $dailyCheckInCount logged actions so far. Review active goals and capture progress before the day ends.",
//                actionLabel = "Open check-in",
//                onAction = onOpenCheckIn
//            )
//        }
        item {
            SectionLabel("Today's Focus")
        }
        if (activeGoals.isEmpty()) {
            item {
                EmptyState(Icons.Rounded.EmojiEvents, "No active targets today.\nTap + to create your first goal.")
            }
        } else {
            items(activeGoals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    onClick = { onOpenGoalLog(goal) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    ShopsTheme {
        DashboardScreen(
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
            profile = UserProfileUiModel(name = "Jahid", email = "jahid@example.com", weightKg = 70f, heightCm = 172f),
            dailyCheckInCount = 4,
            onOpenCheckIn = {},
            onOpenProfile = {},
            onOpenGoalLog = {}
        )
    }
}
