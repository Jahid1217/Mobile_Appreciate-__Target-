package com.example.shops.ui.screens.reminders

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalReportEntry
import com.example.shops.model.GoalType
import com.example.shops.model.GoalUiModel
import com.example.shops.model.GoalsUiState
import com.example.shops.model.MissedReportItem
import com.example.shops.ui.components.AppScreenHeader
import com.example.shops.ui.components.InfoCard
import com.example.shops.ui.components.ReminderGoalCard
import com.example.shops.ui.components.SectionLabel
import com.example.shops.ui.theme.MintGreen
import com.example.shops.ui.theme.OceanBlue
import com.example.shops.ui.theme.ShopsTheme
import com.example.shops.ui.theme.SoftGold
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private enum class ReportRangePreset(val label: String, val days: Long?) {
    LAST_7("7D", 7),
    LAST_30("30D", 30),
    LAST_90("90D", 90),
    ALL("All", null),
    CUSTOM("Custom", null)
}

private data class DailyReportSummary(
    val date: LocalDate,
    val actualTotal: Float,
    val expectedTotal: Float,
    val completedCount: Int,
    val missedCount: Int,
    val averageProgress: Float
)

private data class ChartSegment(
    val label: String,
    val value: Float,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    uiState: GoalsUiState,
    onReminderChange: (GoalUiModel, Boolean, Int, Int) -> Unit
) {
    val today = LocalDate.now()
    val availableStart = remember(uiState.reportEntries, uiState.goals) {
        uiState.reportEntries.minOfOrNull { it.date } ?: uiState.goals.minOfOrNull { it.startDate } ?: today
    }
    val availableEnd = remember(uiState.reportEntries, uiState.goals) {
        uiState.reportEntries.maxOfOrNull { it.date } ?: uiState.goals.maxOfOrNull { minOf(it.endDate, today) } ?: today
    }

    var selectedPreset by remember(availableStart, availableEnd) { mutableStateOf(ReportRangePreset.LAST_30) }
    var startDate by remember(availableStart, availableEnd) {
        mutableStateOf(defaultRangeStart(availableStart, availableEnd, ReportRangePreset.LAST_30))
    }
    var endDate by remember(availableEnd) { mutableStateOf(availableEnd) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val filteredEntries = remember(uiState.reportEntries, startDate, endDate) {
        uiState.reportEntries.filter { it.date in startDate..endDate }
    }
    val dailySummaries = remember(filteredEntries, startDate, endDate) {
        buildDailySummaries(filteredEntries, startDate, endDate)
    }
    val goalStatusSegments = remember(filteredEntries) {
        buildGoalStatusSegments(filteredEntries)
    }
    val categorySegments = remember(filteredEntries) {
        buildCategorySegments(filteredEntries)
    }
    val filteredMissedReports = remember(uiState.missedReports, startDate, endDate) {
        uiState.missedReports.mapNotNull { report ->
            val dates = report.missedDates.filter { it in startDate..endDate }
            if (dates.isEmpty()) null else report.copy(missedDates = dates)
        }
    }

    val trackedGoals = filteredEntries.map { it.goalId }.distinct().size
    val completionRate = if (filteredEntries.isNotEmpty()) {
        filteredEntries.count { it.isCompleted }.toFloat() / filteredEntries.size
    } else {
        0f
    }
    val totalMissedDays = filteredEntries.count { it.isMissed }
    val reminderEnabledGoals = uiState.goals.count { it.reminderEnabled }

    fun applyPreset(preset: ReportRangePreset) {
        selectedPreset = preset
        if (preset == ReportRangePreset.CUSTOM) return
        startDate = defaultRangeStart(availableStart, availableEnd, preset)
        endDate = availableEnd
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp, horizontal = 0.dp)
    ) {
        item {
            AppScreenHeader(
                title = "Reminders and reports",
                subtitle = "Filter by calendar range, compare consistency, and review missed days with charts built from your goal history.",
                trailingContent = {
                    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surface) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("$reminderEnabledGoals on", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            )
        }
        item {
            CalendarFilterCard(
                selectedPreset = selectedPreset,
                startDate = startDate,
                endDate = endDate,
                availableStart = availableStart,
                availableEnd = availableEnd,
                onPresetSelected = ::applyPreset,
                onPickStart = { showStartPicker = true },
                onPickEnd = { showEndPicker = true }
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportMetricCard("Tracked goals", trackedGoals.toString(), Icons.Rounded.Analytics, Modifier.weight(1f))
                ReportMetricCard("Completion", "${(completionRate * 100).toInt()}%", Icons.Rounded.CheckCircle, Modifier.weight(1f))
                ReportMetricCard("Missed days", totalMissedDays.toString(), Icons.Rounded.Warning, Modifier.weight(1f))
            }
        }
        item {
            ReportCard(
                title = "Daily activity trend",
                subtitle = "Actual progress compared with expected progress for the selected dates.",
                icon = Icons.Rounded.ShowChart
            ) {
                if (dailySummaries.isEmpty()) {
                    InfoCard(Icons.Rounded.CheckCircle, MintGreen, "No report data is available in this date range.")
                } else {
                    ActivityTrendChart(dailySummaries = dailySummaries)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportCard(
                    title = "Goal status split",
                    subtitle = "Completed, active, and untouched goals in this range.",
                    icon = Icons.Rounded.PieChart,
                    modifier = Modifier.weight(1f)
                ) {
                    StatusPieChart(segments = goalStatusSegments)
                }
                ReportCard(
                    title = "Category consistency",
                    subtitle = "Normalized progress share by category.",
                    icon = Icons.Rounded.Analytics,
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryBarChart(segments = categorySegments)
                }
            }
        }
        item {
            ReportCard(
                title = "Calendar heatmap",
                subtitle = "Darker cells indicate stronger average completion on that day.",
                icon = Icons.Rounded.CalendarMonth
            ) {
                CalendarHeatmap(dailySummaries = dailySummaries)
            }
        }
        if (uiState.goals.isNotEmpty()) {
            item { SectionLabel("Reminder Coverage") }
            items(uiState.goals, key = { "goal_${it.id}" }) { goal ->
                ReminderGoalCard(
                    goal = goal,
                    onToggleReminder = { enabled ->
                        onReminderChange(goal, enabled, goal.reminderHour, goal.reminderMinute)
                    }
                )
            }
        }
        item {
            SectionLabel("Missed Day Reports")
        }
        if (filteredMissedReports.isEmpty()) {
            item {
                InfoCard(Icons.Rounded.CheckCircle, MintGreen, "No missed dates recorded for the current filter.")
            }
        } else {
            items(filteredMissedReports, key = { "missed_${it.goalId}" }) { report ->
                MissedDatesCard(report = report)
            }
        }
    }

    if (showStartPicker) {
        ReportDatePickerDialog(
            title = "Select start date",
            initialDate = startDate,
            selectableStart = availableStart,
            selectableEnd = endDate,
            onDismiss = { showStartPicker = false },
            onConfirm = { selected ->
                startDate = selected.coerceAtLeast(availableStart)
                if (startDate > endDate) endDate = startDate
                selectedPreset = ReportRangePreset.CUSTOM
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        ReportDatePickerDialog(
            title = "Select end date",
            initialDate = endDate,
            selectableStart = startDate,
            selectableEnd = availableEnd,
            onDismiss = { showEndPicker = false },
            onConfirm = { selected ->
                endDate = selected.coerceAtMost(availableEnd)
                if (endDate < startDate) startDate = endDate
                selectedPreset = ReportRangePreset.CUSTOM
                showEndPicker = false
            }
        )
    }
}

@Composable
private fun CalendarFilterCard(
    selectedPreset: ReportRangePreset,
    startDate: LocalDate,
    endDate: LocalDate,
    availableStart: LocalDate,
    availableEnd: LocalDate,
    onPresetSelected: (ReportRangePreset) -> Unit,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.DateRange, null, tint = MaterialTheme.colorScheme.primary)
                Text("Calendar filter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReportRangePreset.entries) { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = { onPresetSelected(preset) },
                        label = { Text(preset.label) }
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateRangeButton(
                    label = "From",
                    value = startDate.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onPickStart
                )
                DateRangeButton(
                    label = "To",
                    value = endDate.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onPickEnd
                )
            }
            Text(
                text = "History available from $availableStart to $availableEnd.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DateRangeButton(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReportMetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp).size(16.dp))
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp).size(18.dp))
                }
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
private fun ActivityTrendChart(dailySummaries: List<DailyReportSummary>) {
    val maxValue = dailySummaries.maxOf { maxOf(it.actualTotal, it.expectedTotal).coerceAtLeast(1f) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            if (dailySummaries.size < 2) return@Canvas

            val horizontalStep = size.width / (dailySummaries.size - 1)
            val chartHeight = size.height - 24.dp.toPx()

            fun yFor(value: Float): Float {
                val normalized = value / maxValue
                return chartHeight - (normalized * chartHeight) + 12.dp.toPx()
            }

            val expectedPath = Path()
            val actualPath = Path()

            dailySummaries.forEachIndexed { index, summary ->
                val x = horizontalStep * index
                val expectedY = yFor(summary.expectedTotal)
                val actualY = yFor(summary.actualTotal)

                if (index == 0) {
                    expectedPath.moveTo(x, expectedY)
                    actualPath.moveTo(x, actualY)
                } else {
                    expectedPath.lineTo(x, expectedY)
                    actualPath.lineTo(x, actualY)
                }

                drawCircle(
                    color = OceanBlue,
                    radius = 4.dp.toPx(),
                    center = Offset(x, actualY)
                )
            }

            drawPath(
                path = expectedPath,
                color = SoftGold,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = actualPath,
                color = OceanBlue,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChartLegend(label = "Actual", color = OceanBlue)
            ChartLegend(label = "Expected", color = SoftGold)
        }
    }
}

@Composable
private fun StatusPieChart(segments: List<ChartSegment>) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            var startAngle = -90f
            segments.forEach { segment ->
                val sweep = 360f * (segment.value / total)
                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Butt),
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }
        segments.forEach { segment ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(segment.color, CircleShape))
                    Text(segment.label, style = MaterialTheme.typography.bodySmall)
                }
                Text(segment.value.toInt().toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CategoryBarChart(segments: List<ChartSegment>) {
    val maxValue = segments.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        segments.forEach { segment ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(segment.label, style = MaterialTheme.typography.bodySmall)
                    Text("${(segment.value * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(segment.value / maxValue)
                            .height(10.dp)
                            .background(segment.color, RoundedCornerShape(999.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarHeatmap(dailySummaries: List<DailyReportSummary>) {
    if (dailySummaries.isEmpty()) {
        InfoCard(Icons.Rounded.CheckCircle, MintGreen, "No calendar data available in this range.")
        return
    }

    val weeks = dailySummaries.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                week.forEach { summary ->
                    val progress = summary.averageProgress.coerceIn(0f, 1f)
                    val tileColor = when {
                        summary.missedCount > 0 && progress == 0f -> MaterialTheme.colorScheme.error.copy(alpha = 0.28f)
                        progress >= 1f -> MintGreen.copy(alpha = 0.85f)
                        progress > 0f -> OceanBlue.copy(alpha = 0.25f + (progress * 0.55f))
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = tileColor
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(summary.date.dayOfMonth.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                repeat(7 - week.size) {
                    SpacerCell()
                }
            }
        }
    }
}

@Composable
private fun RowScope.SpacerCell() {
    Box(modifier = Modifier.weight(1f).height(56.dp))
}

@Composable
private fun ChartLegend(label: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MissedDatesCard(report: MissedReportItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                Text(report.goalName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Text(
                report.missedDates.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportDatePickerDialog(
    title: String,
    initialDate: LocalDate,
    selectableStart: LocalDate,
    selectableEnd: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMillis(zoneId)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
                    } ?: initialDate
                    val clamped = selectedDate.coerceIn(selectableStart, selectableEnd)
                    onConfirm(clamped)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        Column {
            Text(
                text = title,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            DatePicker(state = datePickerState)
        }
    }
}

private fun defaultRangeStart(
    availableStart: LocalDate,
    availableEnd: LocalDate,
    preset: ReportRangePreset
): LocalDate {
    return when (preset) {
        ReportRangePreset.LAST_7 -> maxOf(availableStart, availableEnd.minusDays(6))
        ReportRangePreset.LAST_30 -> maxOf(availableStart, availableEnd.minusDays(29))
        ReportRangePreset.LAST_90 -> maxOf(availableStart, availableEnd.minusDays(89))
        ReportRangePreset.ALL, ReportRangePreset.CUSTOM -> availableStart
    }
}

private fun buildDailySummaries(
    filteredEntries: List<GoalReportEntry>,
    startDate: LocalDate,
    endDate: LocalDate
): List<DailyReportSummary> {
    val grouped = filteredEntries.groupBy { it.date }
    return generateSequence(startDate) { current ->
        current.plusDays(1).takeIf { !it.isAfter(endDate) }
    }.map { date ->
        val entries = grouped[date].orEmpty()
        val actualTotal = entries.sumOf { it.actualValue.toDouble() }.toFloat()
        val expectedTotal = entries.sumOf { it.expectedValue.toDouble() }.toFloat()
        DailyReportSummary(
            date = date,
            actualTotal = actualTotal,
            expectedTotal = expectedTotal,
            completedCount = entries.count { it.isCompleted },
            missedCount = entries.count { it.isMissed },
            averageProgress = if (entries.isNotEmpty()) {
                entries.map { it.normalizedProgress.coerceIn(0f, 1f) }.average().toFloat()
            } else {
                0f
            }
        )
    }.toList()
}

private fun buildGoalStatusSegments(filteredEntries: List<GoalReportEntry>): List<ChartSegment> {
    if (filteredEntries.isEmpty()) {
        return listOf(
            ChartSegment("Completed", 0f, MintGreen),
            ChartSegment("In progress", 0f, SoftGold),
            ChartSegment("No activity", 0f, MaterialThemeFallback)
        )
    }

    var completed = 0
    var inProgress = 0
    var noActivity = 0

    filteredEntries.groupBy { it.goalId }.values.forEach { entries ->
        val actual = entries.sumOf { it.actualValue.toDouble() }.toFloat()
        val expected = entries.sumOf { it.expectedValue.toDouble() }.toFloat().coerceAtLeast(1f)
        val ratio = actual / expected
        when {
            ratio >= 1f -> completed += 1
            actual > 0f -> inProgress += 1
            else -> noActivity += 1
        }
    }

    return listOf(
        ChartSegment("Completed", completed.toFloat(), MintGreen),
        ChartSegment("In progress", inProgress.toFloat(), SoftGold),
        ChartSegment("No activity", noActivity.toFloat(), MaterialThemeFallback)
    )
}

private fun buildCategorySegments(filteredEntries: List<GoalReportEntry>): List<ChartSegment> {
    val categoryColors = mapOf(
        GoalCategory.WATER to OceanBlue,
        GoalCategory.WALKING to MintGreen,
        GoalCategory.MONEY_SAVING to SoftGold,
        GoalCategory.BOOK_READING to Color(0xFF8B5CF6),
        GoalCategory.WAKEUP to Color(0xFFEC4899),
        GoalCategory.SLEEPING to Color(0xFF6366F1),
        GoalCategory.WORKING to Color(0xFF14B8A6),
        GoalCategory.OTHER to Color(0xFF94A3B8)
    )

    return filteredEntries
        .groupBy { it.goalCategory }
        .map { (category, entries) ->
            ChartSegment(
                label = category.displayName,
                value = entries.sumOf { it.normalizedProgress.coerceIn(0f, 1f).toDouble() }.toFloat(),
                color = categoryColors.getValue(category)
            )
        }
        .sortedByDescending { it.value }
        .take(5)
}

private fun LocalDate.toEpochMillis(zoneId: ZoneId): Long {
    return atStartOfDay(zoneId).toInstant().toEpochMilli()
}

private val MaterialThemeFallback = Color(0xFF94A3B8)

@Preview(showBackground = true)
@Composable
private fun RemindersScreenPreview() {
    val today = LocalDate.now()
    val goal = GoalUiModel(
        name = "Drink Water",
        category = GoalCategory.WATER,
        type = GoalType.DAILY,
        targetValue = 8f,
        currentValue = 4f,
        unit = "glasses",
        startDate = today.minusDays(14),
        endDate = today.plusDays(30),
        glassSizeMl = 250,
        reminderEnabled = true,
        reminderHour = 8,
        reminderMinute = 0
    )

    ShopsTheme {
        RemindersScreen(
            uiState = GoalsUiState(
                goals = listOf(goal),
                missedReports = listOf(
                    MissedReportItem(goalId = goal.id, goalName = goal.finalDisplayName, missedDates = listOf(today.minusDays(2), today.minusDays(5)))
                ),
                reportEntries = listOf(
                    GoalReportEntry(goal.id, goal.finalDisplayName, goal.category, goal.type, today.minusDays(6), 6f, 8f, true),
                    GoalReportEntry(goal.id, goal.finalDisplayName, goal.category, goal.type, today.minusDays(5), 0f, 8f, true),
                    GoalReportEntry(goal.id, goal.finalDisplayName, goal.category, goal.type, today.minusDays(4), 8f, 8f, true),
                    GoalReportEntry(goal.id, goal.finalDisplayName, goal.category, goal.type, today.minusDays(3), 5f, 8f, true),
                    GoalReportEntry(goal.id, goal.finalDisplayName, goal.category, goal.type, today.minusDays(2), 0f, 8f, true),
                    GoalReportEntry(goal.id, goal.finalDisplayName, goal.category, goal.type, today.minusDays(1), 7f, 8f, true),
                    GoalReportEntry(goal.id, goal.finalDisplayName, goal.category, goal.type, today, 4f, 8f, true)
                )
            ),
            onReminderChange = { _, _, _, _ -> }
        )
    }
}
