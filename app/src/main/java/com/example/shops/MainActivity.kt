package com.example.shops

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.model.GoalsUiState
import com.example.shops.model.MissedReportItem
import com.example.shops.model.dateFormatter
import com.example.shops.ui.theme.ShopsTheme
import java.time.LocalDate

data class NavigationItem(val title: String, val route: String, val icon: ImageVector)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShopsTheme {
                GoalFlowApp()
            }
        }
    }
}

@Composable
fun GoalFlowApp(goalsViewModel: GoalsViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState by goalsViewModel.uiState.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<GoalUiModel?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showAddGoalDialog) {
        GoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { form ->
                goalsViewModel.saveGoal(
                    id = null,
                    name = form.name,
                    targetValue = form.targetValue,
                    unit = form.unit,
                    type = form.type,
                    startDate = form.startDate,
                    endDate = form.endDate,
                    reminderEnabled = form.reminderEnabled,
                    reminderHour = form.reminderHour,
                    reminderMinute = form.reminderMinute
                )
                showAddGoalDialog = false
            }
        )
    }

    goalToEdit?.let { goal ->
        GoalDialog(
            goal = goal,
            onDismiss = { goalToEdit = null },
            onConfirm = { form ->
                goalsViewModel.saveGoal(
                    id = goal.id,
                    name = form.name,
                    targetValue = form.targetValue,
                    unit = form.unit,
                    type = form.type,
                    startDate = form.startDate,
                    endDate = form.endDate,
                    reminderEnabled = form.reminderEnabled,
                    reminderHour = form.reminderHour,
                    reminderMinute = form.reminderMinute
                )
                goalToEdit = null
            }
        )
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Target")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(uiState.goals) }
            composable("goals") {
                MyGoalsScreen(
                    goals = uiState.goals,
                    onEditGoal = { goalToEdit = it },
                    onDeleteGoal = goalsViewModel::deleteGoal
                )
            }
            composable("checkin") {
                CheckInScreen(
                    goals = uiState.goals,
                    onIncrement = goalsViewModel::incrementGoal,
                    onComplete = goalsViewModel::completeGoal
                )
            }
            composable("reminders") {
                RemindersScreen(
                    uiState = uiState,
                    onReminderChange = goalsViewModel::updateReminder
                )
            }
        }
    }
}

data class GoalFormData(
    val name: String,
    val targetValue: Float,
    val unit: String,
    val type: GoalType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    goal: GoalUiModel? = null,
    onDismiss: () -> Unit,
    onConfirm: (GoalFormData) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var target by remember { mutableStateOf(goal?.targetValue?.toString() ?: "") }
    var unit by remember { mutableStateOf(goal?.unit ?: "") }
    var startDateText by remember { mutableStateOf(goal?.startDate?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter)) }
    var endDateText by remember { mutableStateOf(goal?.endDate?.format(dateFormatter) ?: LocalDate.now().plusDays(30).format(dateFormatter)) }
    var reminderTime by remember { mutableStateOf(goal?.reminderLabel ?: "09:00") }
    var selectedType by remember { mutableStateOf(goal?.type ?: GoalType.DAILY) }
    var reminderEnabled by remember { mutableStateOf(goal?.reminderEnabled ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Create Target" else "Edit Target") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Target name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = { Text("From date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it },
                    label = { Text("To date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoalType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable reminder", modifier = Modifier.weight(1f))
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Reminder time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = reminderEnabled
                )
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetValue = target.toFloatOrNull()
                    val startDate = startDateText.toLocalDateOrNull()
                    val endDate = endDateText.toLocalDateOrNull()
                    val reminderParts = reminderTime.split(":")
                    val hour = reminderParts.getOrNull(0)?.toIntOrNull()
                    val minute = reminderParts.getOrNull(1)?.toIntOrNull()

                    errorMessage = when {
                        name.isBlank() -> "Target name is required."
                        targetValue == null || targetValue <= 0f -> "Target value must be greater than zero."
                        unit.isBlank() -> "Unit is required."
                        startDate == null || endDate == null -> "Dates must use YYYY-MM-DD."
                        endDate.isBefore(startDate) -> "To date must be on or after from date."
                        reminderEnabled && (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) ->
                            "Reminder time must use HH:MM."
                        else -> null
                    }

                    if (errorMessage == null) {
                        onConfirm(
                            GoalFormData(
                                name = name.trim(),
                                targetValue = targetValue!!,
                                unit = unit.trim(),
                                type = selectedType,
                                startDate = startDate!!,
                                endDate = endDate!!,
                                reminderEnabled = reminderEnabled,
                                reminderHour = hour ?: 9,
                                reminderMinute = minute ?: 0
                            )
                        )
                    }
                }
            ) {
                Text(if (goal == null) "Save" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DashboardScreen(goals: List<GoalUiModel>) {
    val today = LocalDate.now()
    val activeGoals = goals.filter { today in it.startDate..it.endDate }
    val averageProgress = if (activeGoals.isNotEmpty()) activeGoals.map { it.progress }.average() else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Target Tracker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Progress across active date ranges", color = Color.Gray)
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Active", activeGoals.size.toString(), Modifier.weight(1f))
                StatCard("Avg. Progress", "${(averageProgress * 100).toInt()}%", Modifier.weight(1f))
            }
        }

        item {
            Text("Current Targets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        items(activeGoals) { goal ->
            GoalCard(goal = goal)
        }
    }
}

@Composable
fun MyGoalsScreen(
    goals: List<GoalUiModel>,
    onEditGoal: (GoalUiModel) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Targets", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Daily", "Monthly", "Yearly").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val filteredGoals = goals.filter {
                selectedFilter == "All" || it.type.name.equals(selectedFilter, ignoreCase = true)
            }
            items(filteredGoals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    onEdit = { onEditGoal(goal) },
                    onDelete = { onDeleteGoal(goal.id) }
                )
            }
        }
    }
}

@Composable
fun CheckInScreen(
    goals: List<GoalUiModel>,
    onIncrement: (GoalUiModel) -> Unit,
    onComplete: (GoalUiModel) -> Unit
) {
    val activeGoals = goals.filter { LocalDate.now() in it.startDate..it.endDate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Daily Check-in", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activeGoals, key = { it.id }) { goal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(goal.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${goal.currentValue.toInt()}/${goal.targetValue.toInt()} ${goal.unit}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(goal.dateRangeLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        IconButton(onClick = { onIncrement(goal) }) {
                            Icon(Icons.Default.Add, contentDescription = "Increment", tint = goal.type.color)
                        }
                        IconButton(onClick = { onComplete(goal) }) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Complete",
                                tint = if (goal.progress >= 1f) Color(0xFF15803D) else Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersScreen(
    uiState: GoalsUiState,
    onReminderChange: (GoalUiModel, Boolean, Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Reminders Management", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Manage local reminders and review missed dates.", color = Color.Gray)
            }
        }

        item {
            Text("Reminder Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(uiState.goals, key = { it.id }) { goal ->
            ReminderCard(goal = goal, onReminderChange = onReminderChange)
        }

        item {
            Text("Missed Dates Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (uiState.missedReports.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("No missed dates recorded yet.")
                    }
                }
            }
        } else {
            items(uiState.missedReports, key = { it.goalId }) { report ->
                MissedDatesCard(report)
            }
        }
    }
}

@Composable
fun ReminderCard(
    goal: GoalUiModel,
    onReminderChange: (GoalUiModel, Boolean, Int, Int) -> Unit
) {
    var reminderEnabled by remember(goal.id, goal.reminderEnabled) { mutableStateOf(goal.reminderEnabled) }
    var reminderTime by remember(goal.id, goal.reminderLabel) { mutableStateOf(goal.reminderLabel) }

    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = goal.type.color)
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, fontWeight = FontWeight.Bold)
                    Text(goal.dateRangeLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = {
                        reminderEnabled = it
                        val (hour, minute) = reminderTime.toTimeParts()
                        onReminderChange(goal, reminderEnabled, hour, minute)
                    }
                )
            }
            OutlinedTextField(
                value = reminderTime,
                onValueChange = { reminderTime = it },
                label = { Text("HH:MM") },
                enabled = reminderEnabled,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val (hour, minute) = reminderTime.toTimeParts()
                    onReminderChange(goal, reminderEnabled, hour, minute)
                },
                enabled = reminderEnabled
            ) {
                Text("Save Reminder")
            }
        }
    }
}

@Composable
fun MissedDatesCard(report: MissedReportItem) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(report.goalName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                report.missedDates.joinToString(separator = ", ") { it.format(dateFormatter) },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GoalCard(
    goal: GoalUiModel,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(goal.type.color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(goal.name.take(1).uppercase(), color = goal.type.color, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, fontWeight = FontWeight.Bold)
                    Text(goal.type.name, style = MaterialTheme.typography.labelSmall, color = goal.type.color)
                    Text(goal.dateRangeLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Text("${(goal.progress * 100).toInt()}%", fontWeight = FontWeight.Black)
            }

            Text("${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}")

            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = goal.type.color,
                strokeCap = StrokeCap.Round,
                trackColor = goal.type.color.copy(alpha = 0.2f)
            )

            if (onEdit != null || onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit target")
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete target")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem("Dashboard", "dashboard", Icons.Default.Dashboard),
        NavigationItem("Targets", "goals", Icons.Default.List),
        NavigationItem("Check-in", "checkin", Icons.Default.FactCheck),
        NavigationItem("Reminders", "reminders", Icons.Default.Notifications)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

private fun String.toTimeParts(): Pair<Int, Int> {
    val parts = split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hour to minute
}

private operator fun ClosedRange<LocalDate>.contains(date: LocalDate): Boolean {
    return date >= start && date <= endInclusive
}
