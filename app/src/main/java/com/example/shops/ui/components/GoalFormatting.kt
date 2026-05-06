package com.example.shops.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import com.example.shops.model.GoalCategory
import com.example.shops.model.GoalUiModel
import java.time.LocalDate
import kotlin.math.ceil

internal fun categoryIcon(category: GoalCategory) = when (category) {
    GoalCategory.WATER -> androidx.compose.material.icons.Icons.Rounded.WaterDrop
    GoalCategory.WALKING -> androidx.compose.material.icons.Icons.Rounded.DirectionsWalk
    GoalCategory.WAKEUP -> androidx.compose.material.icons.Icons.Rounded.WbSunny
    GoalCategory.SLEEPING -> androidx.compose.material.icons.Icons.Rounded.Bedtime
    GoalCategory.MONEY_SAVING -> androidx.compose.material.icons.Icons.Rounded.Savings
    GoalCategory.BOOK_READING -> androidx.compose.material.icons.Icons.Rounded.MenuBook
    GoalCategory.WORKING -> androidx.compose.material.icons.Icons.Rounded.Work
    GoalCategory.OTHER -> androidx.compose.material.icons.Icons.Rounded.Category
}

internal fun initialTargetValueText(goal: GoalUiModel?): String {
    if (goal == null) return ""
    return if (goal.category == GoalCategory.WATER) {
        goal.waterLitersInputText()
    } else {
        goal.targetValue.toString()
    }
}

internal fun defaultTargetValueText(category: GoalCategory, existingGoal: GoalUiModel? = null): String {
    existingGoal?.let { goal ->
        return if (goal.category == GoalCategory.WATER) {
            goal.waterLitersInputText()
        } else {
            goal.targetValue.toString()
        }
    }

    return when (category) {
        GoalCategory.WAKEUP, GoalCategory.SLEEPING -> "1"
        else -> ""
    }
}

internal fun GoalUiModel.waterLitersInputText(): String {
    val glassSize = glassSizeMl ?: return targetValue.toString()
    if (glassSize <= 0) return targetValue.toString()
    return ((targetValue * glassSize) / 1000f).toString()
}

internal fun litersToGlasses(liters: Float, glassSizeMl: Int?): Int {
    val glassSize = glassSizeMl ?: return liters.toInt()
    if (glassSize <= 0) return liters.toInt()
    return ceil((liters * 1000f) / glassSize).toInt()
}

@Composable
internal fun rememberProfileBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, uriString) {
        if (uriString.isNullOrBlank()) {
            value = null
            return@produceState
        }
        value = runCatching {
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }.value
}

internal operator fun ClosedRange<LocalDate>.contains(date: LocalDate): Boolean =
    date >= start && date <= endInclusive
