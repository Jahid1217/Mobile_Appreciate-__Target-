package com.example.shops.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shops.GoalsViewModel
import com.example.shops.model.GoalUiModel
import com.example.shops.ui.components.BottomNavigationBar
import com.example.shops.ui.components.NavItemUi
import com.example.shops.ui.screens.checkin.CheckInScreen
import com.example.shops.ui.screens.dashboard.DashboardScreen
import com.example.shops.ui.screens.goals.GoalDialog
import com.example.shops.ui.screens.goals.MyGoalsScreen
import com.example.shops.ui.screens.profile.ProfileScreen
import com.example.shops.ui.screens.reminders.RemindersScreen

private val navItems = listOf(
    NavItemUi("dashboard", "Home", Icons.Filled.Dashboard),
    NavItemUi("goals", "Targets", Icons.Filled.List),
    NavItemUi("checkin", "Log", Icons.Filled.FactCheck),
    NavItemUi("reminders", "Alerts", Icons.Filled.Notifications),
    NavItemUi("profile", "Profile", Icons.Filled.Person)
)

@Composable
fun GoalFlowApp(goalsViewModel: GoalsViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState by goalsViewModel.uiState.collectAsState()
    val profile = uiState.profile

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<GoalUiModel?>(null) }
    var goalToDelete by remember { mutableStateOf<GoalUiModel?>(null) }

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
            existingGoals = uiState.goals,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = {
                goalsViewModel.saveGoal(it)
                showAddGoalDialog = false
            }
        )
    }

    goalToEdit?.let { goal ->
        GoalDialog(
            goal = goal,
            existingGoals = uiState.goals,
            onDismiss = { goalToEdit = null },
            onConfirm = {
                goalsViewModel.saveGoal(it)
                goalToEdit = null
            }
        )
    }

    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Target") },
            text = {
                Text("Are you sure you want to delete \"${goal.finalDisplayName}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        goalsViewModel.deleteGoal(goal.id)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = { BottomNavigationBar(navController, navItems) },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddGoalDialog = true },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Target")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    goals = uiState.goals,
                    profile = profile,
                    dailyCheckInCount = uiState.dailyCheckInCount,
                    onOpenCheckIn = { navController.navigate("checkin") },
                    onOpenProfile = { navController.navigate("profile") },
                    onOpenGoalLog = { navController.navigate("checkin") }
                )
            }
            composable("goals") {
                MyGoalsScreen(
                    goals = uiState.goals,
                    onEditGoal = { goalToEdit = it },
                    onDeleteGoal = { goalToDelete = it }
                )
            }
            composable("checkin") {
                CheckInScreen(
                    goals = uiState.goals,
                    onIncrement = goalsViewModel::incrementGoal,
                    onComplete = goalsViewModel::completeGoal,
                    onNegative = goalsViewModel::decrementGoal
                )
            }
            composable("reminders") {
                RemindersScreen(
                    uiState = uiState,
                    onReminderChange = goalsViewModel::updateReminder
                )
            }
            composable("profile") {
                ProfileScreen(
                    profile = profile,
                    onSaveProfile = goalsViewModel::saveProfile
                )
            }
        }
    }
}
