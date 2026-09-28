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
import androidx.compose.ui.platform.LocalContext
import com.lodkin.dartstrainer.data.SettingsStorage
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.ui.LoadingScreen
import com.lodkin.dartstrainer.ui.MainMenuScreen
import com.lodkin.dartstrainer.ui.OnboardingResult
import com.lodkin.dartstrainer.ui.OnboardingScreen
import com.lodkin.dartstrainer.ui.StatsScreen
import com.lodkin.dartstrainer.ui.WelcomeScreen
import kotlinx.coroutines.delay

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
    val context = LocalContext.current

    // Пройдена ли анкета
    var onboardingDone by remember {
        mutableStateOf(SettingsStorage.isOnboardingDone(context))
    }

    // Этап: "welcome" → "loading" → "main"
    // Если анкета НЕ пройдена → сначала "welcome" (приветствие), затем "onboarding"
    // Если анкета пройдена → "loading" (3 сек) → "main"
    var stage by remember {
        mutableStateOf(if (onboardingDone) "loading" else "welcome")
    }

    var screen by remember { mutableStateOf("main") }

    // Переход с "loading" на "main" через 3 секунды
    if (stage == "loading") {
        LaunchedEffect(Unit) {
            delay(3000)
            stage = "main"
        }
    }

    when (stage) {
        "welcome" -> WelcomeScreen(onStart = { stage = "onboarding" })

        "onboarding" -> OnboardingScreen(onFinish = { result: OnboardingResult ->
            SettingsStorage.setPlayerName(context, result.name)
            SettingsStorage.setTrainingMinutes(context, result.trainingMinutes)
            SettingsStorage.setTrainingsPerWeek(context, result.trainingsPerWeek)
            SettingsStorage.setTrainingMode(context, result.trainingMode)
            SettingsStorage.setStartMode(context, result.startMode)
            SettingsStorage.setOnboardingDone(context)
            onboardingDone = true
            stage = "main"
        })

        "loading" -> LoadingScreen()

        "main" -> when (screen) {
            "main" -> MainMenuScreen(
                onTraining = { screen = "training" },
                onFreePlay = { screen = "free" },
                onStatsClick = { screen = "stats" }
            )
            "stats" -> StatsScreen(onBack = { screen = "main" })
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
