package com.example.shops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.shops.ui.navigation.GoalFlowApp
import com.example.shops.ui.theme.ShopsTheme

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
