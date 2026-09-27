package com.lodkin.dartstrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.ui.MainMenuScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
                    DartsTrainerApp()
                }
            }
        }
    }
}

@Composable
fun DartsTrainerApp() {
    var screen by remember { mutableStateOf("main") }

    when (screen) {
        "main" -> MainMenuScreen(
            onTraining = { screen = "training" },
            onFreePlay = { screen = "free" }
        )
        "training" -> PlaceholderScreen(
            title = "Тренировка",
            onBack = { screen = "main" }
        )
        "free" -> PlaceholderScreen(
            title = "Свободная игра",
            onBack = { screen = "main" }
        )
    }
}

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title — в разработке",
            color = Accent
        )
    }
}
