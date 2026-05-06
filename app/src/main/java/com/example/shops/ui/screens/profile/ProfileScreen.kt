package com.example.shops.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shops.model.UserProfileUiModel
import com.example.shops.ui.components.profile.DashboardProfileHeader
import com.example.shops.ui.components.profile.InfoPill
import com.example.shops.ui.components.profile.ProfileSummaryCard
import com.example.shops.ui.theme.ShopsTheme

@Composable
fun ProfileScreen(
    profile: UserProfileUiModel?,
    onSaveProfile: (UserProfileUiModel) -> Unit
) {
    var name by remember(profile?.id) { mutableStateOf(profile?.name.orEmpty()) }
    var email by remember(profile?.id) { mutableStateOf(profile?.email.orEmpty()) }
    var ageText by remember(profile?.id) { mutableStateOf(profile?.age?.toString().orEmpty()) }
    var bloodGroup by remember(profile?.id) { mutableStateOf(profile?.bloodGroup.orEmpty()) }
    var gender by remember(profile?.id) { mutableStateOf(profile?.gender.orEmpty()) }
    var weightText by remember(profile?.id) { mutableStateOf(profile?.weightKg?.toString().orEmpty()) }
    var heightText by remember(profile?.id) { mutableStateOf(profile?.heightCm?.toString().orEmpty()) }
    var profilePictureUri by remember(profile?.id) { mutableStateOf(profile?.profilePictureUri) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        profilePictureUri = uri?.toString()
    }

    val previewProfile = UserProfileUiModel(
        id = profile?.id ?: "current_profile",
        name = name,
        email = email,
        profilePictureUri = profilePictureUri,
        age = ageText.toIntOrNull(),
        bloodGroup = bloodGroup,
        gender = gender,
        weightKg = weightText.toFloatOrNull(),
        heightCm = heightText.toFloatOrNull()
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp, horizontal = 16.dp)
    ) {
        item {
            Text("User Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Update your personal details and profile picture.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            ProfileSummaryCard(profile = previewProfile, onPickImage = { imagePicker.launch("image/*") })
        }
        item {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ageText, onValueChange = { ageText = it }, label = { Text("Age") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Text("Gender", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            FilterChip(selected = gender.equals(option, ignoreCase = true), onClick = { gender = option }, label = { Text(option) })
                        }
                    }
                    OutlinedTextField(value = bloodGroup, onValueChange = { bloodGroup = it.uppercase() }, label = { Text("Blood Group") }, placeholder = { Text("A+, O-, AB+ ...") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Weight (kg)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = heightText, onValueChange = { heightText = it }, label = { Text("Height (cm)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { imagePicker.launch("image/*") }) { Text("Choose Photo") }
                        TextButton(onClick = { profilePictureUri = null }) { Text("Remove Photo") }
                    }
                    Button(
                        onClick = {
                            errorMessage = when {
                                name.isBlank() -> "Name is required."
                                email.isBlank() -> "Email is required."
                                weightText.isNotBlank() && weightText.toFloatOrNull() == null -> "Weight must be a number."
                                heightText.isNotBlank() && heightText.toFloatOrNull() == null -> "Height must be a number."
                                ageText.isNotBlank() && ageText.toIntOrNull() == null -> "Age must be a whole number."
                                else -> null
                            }
                            if (errorMessage == null) {
                                onSaveProfile(
                                    UserProfileUiModel(
                                        id = profile?.id ?: "current_profile",
                                        name = name.trim(),
                                        email = email.trim(),
                                        profilePictureUri = profilePictureUri,
                                        age = ageText.toIntOrNull(),
                                        bloodGroup = bloodGroup.trim(),
                                        gender = gender.trim(),
                                        weightKg = weightText.toFloatOrNull(),
                                        heightCm = heightText.toFloatOrNull()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Profile") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ShopsTheme {
        ProfileScreen(
            profile = UserProfileUiModel(
                name = "Jahid",
                email = "jahid@example.com",
                age = 27,
                bloodGroup = "O+",
                gender = "Male",
                weightKg = 70f,
                heightCm = 172f
            ),
            onSaveProfile = {}
        )
    }
}
