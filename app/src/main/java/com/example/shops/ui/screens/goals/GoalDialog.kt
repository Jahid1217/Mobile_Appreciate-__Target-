package com.example.shops.ui.screens.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.model.dateFormatter
import com.example.shops.ui.components.InfoCard
import com.example.shops.ui.components.ReminderChip
import com.example.shops.ui.components.SectionLabel
import com.example.shops.ui.components.categoryIcon
import com.example.shops.ui.components.defaultTargetValueText
import com.example.shops.ui.components.initialTargetValueText
import com.example.shops.ui.components.litersToGlasses
import com.example.shops.ui.theme.ShopsTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    goal: GoalUiModel? = null,
    existingGoals: List<GoalUiModel> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (GoalUiModel) -> Unit
) {
    var category by remember { mutableStateOf(goal?.category ?: GoalCategory.WATER) }
    var customName by remember { mutableStateOf(goal?.customCategoryName ?: "") }
    var targetValueText by remember(goal?.id, goal?.targetValue, goal?.glassSizeMl, goal?.category) {
        mutableStateOf(initialTargetValueText(goal))
    }
    var unit by remember { mutableStateOf(goal?.unit ?: "") }
    var startDateText by remember { mutableStateOf(goal?.startDate?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter)) }
    var endDateText by remember { mutableStateOf(goal?.endDate?.format(dateFormatter) ?: LocalDate.now().plusDays(30).format(dateFormatter)) }
    var glassSizeText by remember { mutableStateOf(goal?.glassSizeMl?.toString() ?: "250") }
    var wakeupTimeText by remember { mutableStateOf(goal?.wakeupTime?.toString() ?: "07:00") }
    var sleepTimeText by remember { mutableStateOf(goal?.sleepTime?.toString() ?: "22:00") }
    var reminderEnabled by remember { mutableStateOf(goal?.reminderEnabled ?: true) }
    var selectedType by remember { mutableStateOf(goal?.type ?: GoalType.DAILY) }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val multipleReminders = remember {
        mutableStateListOf<LocalTime>().apply {
            if (goal?.category == GoalCategory.OTHER) addAll(goal.multipleReminders)
        }
    }
    var newReminderTimeText by remember { mutableStateOf("09:00") }

    val waterGlasses = remember(targetValueText, glassSizeText) {
        val liters = targetValueText.toFloatOrNull() ?: 0f
        val glassSize = glassSizeText.toIntOrNull() ?: 250
        if (glassSize > 0) litersToGlasses(liters, glassSize) else 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        title = { Text(if (goal == null) "Create New Target" else "Edit Target", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = category.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(categoryIcon(category), contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        GoalCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                leadingIcon = { Icon(categoryIcon(cat), contentDescription = null) },
                                text = { Text(cat.displayName) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                    targetValueText = when {
                                        goal != null && goal.category == cat -> initialTargetValueText(goal)
                                        cat == GoalCategory.WAKEUP || cat == GoalCategory.SLEEPING -> "1"
                                        else -> ""
                                    }
                                    unit = when (cat) {
                                        GoalCategory.WATER -> "Liters"
                                        GoalCategory.WALKING -> "Steps"
                                        GoalCategory.MONEY_SAVING -> "Currency"
                                        GoalCategory.BOOK_READING -> "Pages"
                                        else -> ""
                                    }
                                }
                            )
                        }
                    }
                }

                when (category) {
                    GoalCategory.WATER -> {
                        SectionLabel("Water Intake Settings")
                        OutlinedTextField(value = targetValueText, onValueChange = { targetValueText = it }, label = { Text("Daily target (Liters)") }, leadingIcon = { Icon(Icons.Rounded.WaterDrop, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = glassSizeText, onValueChange = { glassSizeText = it }, label = { Text("Glass size (ml)") }, leadingIcon = { Icon(Icons.Rounded.LocalBar, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        if (waterGlasses > 0) {
                            InfoCard(Icons.Rounded.Info, MaterialTheme.colorScheme.primary, "That's $waterGlasses glasses of ${glassSizeText}ml per day.")
                        }
                        SectionLabel("Sleep Schedule")
                        OutlinedTextField(value = wakeupTimeText, onValueChange = { wakeupTimeText = it }, label = { Text("Wake-up time (HH:MM)") }, leadingIcon = { Icon(Icons.Rounded.WbSunny, null) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = sleepTimeText, onValueChange = { sleepTimeText = it }, label = { Text("Sleep time (HH:MM)") }, leadingIcon = { Icon(Icons.Rounded.Bedtime, null) }, modifier = Modifier.fillMaxWidth())
                    }
                    GoalCategory.WALKING -> {
                        SectionLabel("Step Goal")
                        OutlinedTextField(value = targetValueText, onValueChange = { targetValueText = it }, label = { Text("Daily step target") }, leadingIcon = { Icon(Icons.Rounded.DirectionsWalk, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        InfoCard(Icons.Rounded.Info, MaterialTheme.colorScheme.tertiary, "You'll receive an end-of-day reminder asking whether your step target was completed.")
                    }
                    GoalCategory.WAKEUP -> {
                        SectionLabel("Wake-up Alarm")
                        OutlinedTextField(value = wakeupTimeText, onValueChange = { wakeupTimeText = it }, label = { Text("Wake-up time (HH:MM)") }, leadingIcon = { Icon(Icons.Rounded.Alarm, null) }, modifier = Modifier.fillMaxWidth())
                    }
                    GoalCategory.SLEEPING -> {
                        SectionLabel("Sleep Reminder")
                        OutlinedTextField(value = sleepTimeText, onValueChange = { sleepTimeText = it }, label = { Text("Sleep time (HH:MM)") }, leadingIcon = { Icon(Icons.Rounded.Bedtime, null) }, modifier = Modifier.fillMaxWidth())
                    }
                    GoalCategory.OTHER -> {
                        SectionLabel("Custom Goal")
                        OutlinedTextField(value = customName, onValueChange = { customName = it }, label = { Text("Goal name") }, leadingIcon = { Icon(Icons.Rounded.Edit, null) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = targetValueText, onValueChange = { targetValueText = it }, label = { Text("Target value") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.fillMaxWidth())
                        SectionLabel("Reminders")
                        if (multipleReminders.isEmpty()) {
                            Text("No reminders added yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            multipleReminders.forEachIndexed { index, time ->
                                ReminderChip(time = time.toString(), onDelete = { multipleReminders.removeAt(index) })
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = newReminderTimeText, onValueChange = { newReminderTimeText = it }, label = { Text("New reminder (HH:MM)") }, singleLine = true, modifier = Modifier.weight(1f))
                            FilledIconButton(onClick = {
                                try {
                                    multipleReminders.add(LocalTime.parse(newReminderTimeText))
                                    newReminderTimeText = "09:00"
                                    errorMessage = null
                                } catch (_: Exception) {
                                    errorMessage = "Invalid time format. Use HH:MM."
                                }
                            }) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add reminder")
                            }
                        }
                    }
                    else -> {
                        OutlinedTextField(value = targetValueText, onValueChange = { targetValueText = it }, label = { Text("Target value") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.fillMaxWidth())
                    }
                }

                Divider()
                SectionLabel("Duration")
                OutlinedTextField(value = startDateText, onValueChange = { startDateText = it }, label = { Text("From (YYYY-MM-DD)") }, leadingIcon = { Icon(Icons.Rounded.CalendarToday, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = endDateText, onValueChange = { endDateText = it }, label = { Text("To (YYYY-MM-DD)") }, leadingIcon = { Icon(Icons.Rounded.EventAvailable, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SectionLabel("Tracking Type")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalType.entries.forEach { type ->
                        FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = if (reminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff, contentDescription = null, tint = if (reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Enable reminders", modifier = Modifier.weight(1f))
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }

                AnimatedVisibility(visible = errorMessage != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    errorMessage?.let {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Text(it, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    val resolvedTargetValueText = targetValueText.ifBlank {
                        defaultTargetValueText(category, goal)
                    }
                    val targetVal = resolvedTargetValueText.toFloatOrNull() ?: 0f
                    val startDate = LocalDate.parse(startDateText)
                    val endDate = LocalDate.parse(endDateText)
                    if (endDate.isBefore(startDate)) {
                        errorMessage = "End date must be after start date."
                        return@Button
                    }
                    val finalTargetName = if (category == GoalCategory.OTHER) customName.trim() else category.displayName
                    if (finalTargetName.isBlank()) {
                        errorMessage = "Target name is required."
                        return@Button
                    }
                    val hasDuplicateTarget = existingGoals.any { existing ->
                        existing.id != goal?.id &&
                            existing.finalDisplayName.trim().equals(finalTargetName, ignoreCase = true)
                    }
                    if (hasDuplicateTarget) {
                        errorMessage = "\"$finalTargetName\" is already assigned. Each target can only be added once."
                        return@Button
                    }
                    val wakeupTime = when (category) {
                        GoalCategory.WATER, GoalCategory.WAKEUP -> LocalTime.parse(wakeupTimeText)
                        else -> null
                    }
                    val sleepTime = when (category) {
                        GoalCategory.WATER, GoalCategory.SLEEPING -> LocalTime.parse(sleepTimeText)
                        else -> null
                    }
                    onConfirm(
                        GoalUiModel(
                            id = goal?.id ?: java.util.UUID.randomUUID().toString(),
                            name = if (category == GoalCategory.OTHER) customName else category.displayName,
                            category = category,
                            customCategoryName = customName,
                            type = selectedType,
                            targetValue = when (category) {
                                GoalCategory.WATER -> litersToGlasses(targetVal, glassSizeText.toIntOrNull()).toFloat()
                                GoalCategory.WAKEUP, GoalCategory.SLEEPING -> 1f
                                else -> targetVal
                            },
                            currentValue = goal?.currentValue ?: 0f,
                            unit = if (category == GoalCategory.WATER) "glasses" else unit,
                            startDate = startDate,
                            endDate = endDate,
                            createdAt = goal?.createdAt ?: LocalDateTime.now(),
                            glassSizeMl = glassSizeText.toIntOrNull(),
                            wakeupTime = wakeupTime,
                            sleepTime = sleepTime,
                            reminderEnabled = reminderEnabled,
                            reminderHour = wakeupTime?.hour ?: 9,
                            reminderMinute = wakeupTime?.minute ?: 0,
                            multipleReminders = multipleReminders.toList()
                        )
                    )
                } catch (_: Exception) {
                    errorMessage = "Please check all inputs. Dates: YYYY-MM-DD, Times: HH:MM."
                }
            }, shape = RoundedCornerShape(12.dp)) {
                Icon(imageVector = if (goal == null) Icons.Rounded.Add else Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (goal == null) "Create Target" else "Save Changes")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Preview(showBackground = true)
@Composable
private fun GoalDialogPreview() {
    ShopsTheme {
        GoalDialog(onDismiss = {}, onConfirm = {})
    }
}
