package com.example.shops.ui.components.profile

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.shops.model.ScreenTimeStatus
import com.example.shops.model.ScreenTimeSummary
import com.example.shops.model.UserProfileUiModel
import com.example.shops.screen.ScreenTimeMonitor
import com.example.shops.ui.components.rememberProfileBitmap
import kotlinx.coroutines.delay

@Composable
fun DashboardProfileHeader(profile: UserProfileUiModel?, onOpenProfile: () -> Unit) {
    val context = LocalContext.current
    var refreshToken by remember { mutableStateOf(0) }
    val screenTimeState by produceState(initialValue = ScreenTimeUiState(), refreshToken) {
        while (true) {
            val hasUsageAccess = ScreenTimeMonitor.hasUsageAccess(context)
            value = ScreenTimeUiState(
                hasUsageAccess = hasUsageAccess,
                summary = if (hasUsageAccess) ScreenTimeMonitor.loadTodayScreenTime(context) else null
            )
            delay(60_000)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        refreshToken += 1
    }

    Card(
        onClick = onOpenProfile,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile == null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileAvatar(profile = profile, size = 72.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        profile?.name?.ifBlank { "Your Profile" } ?: "Your Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        profile?.email?.ifBlank { "Tap to complete your profile" } ?: "Tap to complete your profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (profile?.bmi != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(onClick = onOpenProfile, label = { Text("BMI ${profile.bmiLabel}") })
                            AssistChip(onClick = onOpenProfile, label = { Text(profile.healthStatus) })
                        }
                    }
                }
                Icon(
                    Icons.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ScreenTimeCard(
                summary = screenTimeState.summary,
                hasUsageAccess = screenTimeState.hasUsageAccess,
                onGrantAccess = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )
        }
    }
}

@Composable
private fun ScreenTimeCard(
    summary: ScreenTimeSummary?,
    hasUsageAccess: Boolean,
    onGrantAccess: () -> Unit
) {
    var detailsExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Rounded.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Screen Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (hasUsageAccess) "Today only" else "Usage access is required to monitor screen activity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!hasUsageAccess) {
                Button(onClick = onGrantAccess) {
                    Text("Enable Usage Access")
                }
                Text(
                    "Grant access in system settings to view daily screen time, healthy limits, and recommendations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val resolvedSummary = summary ?: ScreenTimeSummary(0L, ScreenTimeStatus.OPTIMAL)
                val statusTint = resolvedSummary.status.color()

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoPill("Today", resolvedSummary.formattedDuration, Modifier.weight(1f), tint = MaterialTheme.colorScheme.primary)
                    InfoPill("Status", resolvedSummary.status.label, Modifier.weight(1f), tint = statusTint)
                }

                FilledTonalButton(
                    onClick = { detailsExpanded = !detailsExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = statusTint.copy(alpha = 0.12f),
                        contentColor = statusTint
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = resolvedSummary.status.icon(),
                                contentDescription = null,
                                tint = statusTint
                            )
                            Text(
                                "View details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (detailsExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                            contentDescription = if (detailsExpanded) "Hide details" else "Show details"
                        )
                    }
                }

                if (detailsExpanded) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = statusTint.copy(alpha = 0.12f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                resolvedSummary.status.guideline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.TipsAndUpdates, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text(
                                "Suggested guideline: optimal under 2h/day, acceptable up to 4h/day with regular breaks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSummaryCard(profile: UserProfileUiModel, onPickImage: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        )
                    )
                )
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileAvatar(profile = profile, size = 84.dp)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(profile.name.ifBlank { "Your Name" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(profile.email.ifBlank { "Email not set" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (profile.age != null) "Age ${profile.age}" else "Age not set", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = onPickImage) { Text("Photo") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoPill("BMI", profile.bmiLabel, Modifier.weight(1f))
                    InfoPill("Health", profile.healthStatus, Modifier.weight(1f), tint = profile.healthStatusColor)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoPill("Blood", profile.bloodGroup.ifBlank { "--" }, Modifier.weight(1f))
                    InfoPill("Gender", profile.gender.ifBlank { "--" }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ProfileAvatar(profile: UserProfileUiModel?, size: Dp) {
    val bitmap = rememberProfileBitmap(profile?.profilePictureUri)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                profile?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun InfoPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = tint.copy(alpha = 0.12f)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun ScreenTimeStatus.color(): Color = when (this) {
    ScreenTimeStatus.OPTIMAL -> Color(0xFF15803D)
    ScreenTimeStatus.HEALTHY -> Color(0xFFB45309)
    ScreenTimeStatus.HIGH -> Color(0xFFB91C1C)
}

private fun ScreenTimeStatus.icon(): ImageVector = when (this) {
    ScreenTimeStatus.OPTIMAL -> Icons.Rounded.CheckCircle
    ScreenTimeStatus.HEALTHY -> Icons.Rounded.HourglassTop
    ScreenTimeStatus.HIGH -> Icons.Rounded.WarningAmber
}

private data class ScreenTimeUiState(
    val hasUsageAccess: Boolean = false,
    val summary: ScreenTimeSummary? = null
)
