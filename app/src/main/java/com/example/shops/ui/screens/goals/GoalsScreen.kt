package com.example.shops.ui.screens.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.ui.components.AppScreenHeader
import com.example.shops.ui.components.EmptyState
import com.example.shops.ui.components.GoalCard
import com.example.shops.ui.components.InfoCard
import com.example.shops.ui.theme.ShopsTheme
import java.time.LocalDate

@Composable
fun MyGoalsScreen(
    goals: List<GoalUiModel>,
    onEditGoal: (GoalUiModel) -> Unit,
    onDeleteGoal: (GoalUiModel) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All") + GoalType.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))
        AppScreenHeader(
            title = "My targets",
            subtitle = "Filter your goals by cadence and keep the most important ones easy to review.",
            trailingContent = {
                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.surface) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            items(filters) { filter ->
                FilterChip(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter) })
            }
        }
        val filtered = goals.filter { selectedFilter == "All" || it.type.name.equals(selectedFilter, true) }
        InfoCard(
            icon = Icons.Rounded.FilterList,
            color = MaterialTheme.colorScheme.primary,
            text = "${filtered.size} target${if (filtered.size == 1) "" else "s"} visible in $selectedFilter."
        )
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            EmptyState(Icons.Rounded.FilterList, "No targets in this category.\nTap + to add one.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(filtered, key = { it.id }) { goal ->
                    GoalCard(goal = goal, onEdit = { onEditGoal(goal) }, onDelete = { onDeleteGoal(goal) })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyGoalsScreenPreview() {
    ShopsTheme {
        MyGoalsScreen(
            goals = listOf(
                GoalUiModel(
                    name = "Read Books",
                    category = GoalCategory.BOOK_READING,
                    type = GoalType.YEARLY,
                    targetValue = 12f,
                    currentValue = 5f,
                    unit = "books",
                    startDate = LocalDate.now().minusDays(10),
                    endDate = LocalDate.now().plusDays(50),
                    reminderEnabled = true,
                    reminderHour = 9,
                    reminderMinute = 0
                )
            ),
            onEditGoal = {},
            onDeleteGoal = {}
        )
    }
}
