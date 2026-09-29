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
import com.lodkin.dartstrainer.data.cricket.CricketDatabase
import com.lodkin.dartstrainer.data.cricket.CricketGame
import com.lodkin.dartstrainer.data.cricket.CricketPlayer
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.cricket.CricketType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.ui.GameSelectScreen
import com.lodkin.dartstrainer.ui.LoadingScreen
import com.lodkin.dartstrainer.ui.MainMenuScreen
import com.lodkin.dartstrainer.ui.OnboardingResult
import com.lodkin.dartstrainer.ui.OnboardingScreen
import com.lodkin.dartstrainer.ui.StatsScreen
import com.lodkin.dartstrainer.ui.WelcomeScreen
import com.lodkin.dartstrainer.ui.cricket.CricketGameScreen
import com.lodkin.dartstrainer.ui.cricket.CricketSetupScreen
import com.lodkin.dartstrainer.ui.cricket.CricketStatsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    // Репозиторий крикета
    val cricketRepository = remember {
        CricketRepository(CricketDatabase.get(context).cricketDao())
    }

    var onboardingDone by remember {
        mutableStateOf(SettingsStorage.isOnboardingDone(context))
    }
    var stage by remember {
        mutableStateOf(if (onboardingDone) "loading" else "welcome")
    }
    var screen by remember { mutableStateOf("main") }

    // Состояние крикета
    var cricketGame by remember { mutableStateOf<CricketGame?>(null) }

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
                onFreePlay = { screen = "game_select" },
                onStatsClick = { screen = "stats" }
            )

            "stats" -> StatsScreen(onBack = { screen = "main" })

            "training" -> PlaceholderScreen(
                title = "Тренировка",
                onBack = { screen = "main" }
            )

            // ── Выбор игры ──
            "game_select" -> GameSelectScreen(
                onCricket = { screen = "cricket_setup" },
                on501 = { screen = "placeholder_501" },
                onBack = { screen = "main" }
            )

            "placeholder_501" -> PlaceholderScreen(
                title = "501 — в разработке",
                onBack = { screen = "game_select" }
            )

            // ── Крикет ──
            "cricket_setup" -> CricketSetupScreen(
                playerName = SettingsStorage.getPlayerName(context),
                onStartGame = { type: CricketType, players: List<CricketPlayer> ->
                    cricketGame = CricketGame(
                        type = type,
                        players = players,
                        currentPlayerIndex = 0,
                        isFinished = false,
                        winnerIndex = null,
                        currentTurnDarts = 0
                    )
                    screen = "cricket_game"
                },
                onBack = { screen = "game_select" }
            )

            "cricket_game" -> {
                val game = cricketGame
                if (game != null) {
                    CricketGameScreen(
                        initialGame = game,
                        onGameFinish = { finished: CricketGame ->
                            // Сохраняем игру в базу
                            scope.launch {
                                cricketRepository.saveGame(finished)
                            }
                            cricketGame = finished
                            screen = "cricket_stats"
                        },
                        onBack = { screen = "game_select" }
                    )
                } else {
                    screen = "cricket_setup"
                }
            }

            "cricket_stats" -> {
                val game = cricketGame
                if (game != null) {
                    CricketStatsScreen(
                        game = game,
                        onPlayAgain = { screen = "cricket_setup" },
                        onBackToMenu = {
                            cricketGame = null
                            screen = "main"
                        }
                    )
                } else {
                    screen = "main"
                }
            }
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
            text = "$title",
            color = Accent
        )
    }
}
