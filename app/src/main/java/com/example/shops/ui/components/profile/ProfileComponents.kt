package com.example.shops.ui.components.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.shops.model.UserProfileUiModel
import com.example.shops.ui.components.rememberProfileBitmap

@Composable
fun DashboardProfileHeader(profile: UserProfileUiModel?, onOpenProfile: () -> Unit) {
    Card(
        onClick = onOpenProfile,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile == null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
