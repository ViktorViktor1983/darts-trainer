package com.lodkin.dartstrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import com.lodkin.dartstrainer.data.aroundclock.AroundClockDatabase
import com.lodkin.dartstrainer.data.aroundclock.AroundClockOrder
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.aroundclock.AroundClockTarget
import com.lodkin.dartstrainer.data.biground.BigRoundCategory
import com.lodkin.dartstrainer.data.biground.BigRoundDatabase
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.cricket.CricketDatabase
import com.lodkin.dartstrainer.data.cricket.CricketGame
import com.lodkin.dartstrainer.data.cricket.CricketLogic
import com.lodkin.dartstrainer.data.cricket.CricketPlayer
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.cricket.CricketType
import com.lodkin.dartstrainer.data.game501.Game501
import com.lodkin.dartstrainer.data.game501.Game501Database
import com.lodkin.dartstrainer.data.game501.Game501Logic
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.game501.GameType
import com.lodkin.dartstrainer.data.game501.OutMode
import com.lodkin.dartstrainer.data.game501.Player501
import com.lodkin.dartstrainer.data.scoreset.ScoreSetDatabase
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorDatabase
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.ui.GameSelectScreen
import com.lodkin.dartstrainer.ui.LoadingScreen
import com.lodkin.dartstrainer.ui.MainMenuScreen
import com.lodkin.dartstrainer.ui.OnboardingResult
import com.lodkin.dartstrainer.ui.OnboardingScreen
import com.lodkin.dartstrainer.ui.SettingsScreen
import com.lodkin.dartstrainer.ui.StatsMenuScreen
import com.lodkin.dartstrainer.ui.StatsScreen
import com.lodkin.dartstrainer.ui.WelcomeScreen
import com.lodkin.dartstrainer.ui.aroundclock.AroundClockGameScreen
import com.lodkin.dartstrainer.ui.aroundclock.AroundClockSetupScreen
import com.lodkin.dartstrainer.ui.aroundclock.AroundClockStatsScreen
import com.lodkin.dartstrainer.ui.biground.BigRoundGameScreen
import com.lodkin.dartstrainer.ui.biground.BigRoundSetupScreen
import com.lodkin.dartstrainer.ui.biground.BigRoundStatsScreen
import com.lodkin.dartstrainer.ui.cricket.CricketGameScreen
import com.lodkin.dartstrainer.ui.cricket.CricketSetupScreen
import com.lodkin.dartstrainer.ui.cricket.CricketStatsScreen
import com.lodkin.dartstrainer.ui.game501.Game501Screen
import com.lodkin.dartstrainer.ui.game501.Game501SetupScreen
import com.lodkin.dartstrainer.ui.game501.Game501StatsScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetGameScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetSetupScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetStatsScreen
import com.lodkin.dartstrainer.ui.sector.SectorGameScreen
import com.lodkin.dartstrainer.ui.sector.SectorSetupScreen
import com.lodkin.dartstrainer.ui.sector.SectorStatsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ─────────────────────────────────────────────
// Карта: с какого экрана куда возвращаться
// при нажатии системной кнопки «Назад».
// ─────────────────────────────────────────────
private val PARENT_SCREEN: Map<String, String> = mapOf(
    "stats_menu" to "main",
    "stats_cricket" to "stats_menu",
    "stats_501" to "stats_menu",
    "settings" to "main",
    "training" to "main",
    "game_select" to "main",

    "cricket_setup" to "game_select",
    "cricket_game" to "game_select",
    "cricket_stats" to "main",

    "game501_setup" to "game_select",
    "game501_game" to "game_select",
    "game501_stats" to "main",

    "sector_setup" to "game_select",
    "sector_game" to "game_select",
    "sector_stats" to "stats_menu",

    "aroundclock_setup" to "game_select",
    "aroundclock_game" to "game_select",
    "aroundclock_stats" to "stats_menu",

    "biground_setup" to "game_select",
    "biground_game" to "game_select",
    "biground_stats" to "stats_menu",

    "scoreset_setup" to "game_select",
    "scoreset_game" to "game_select",
    "scoreset_stats" to "stats_menu"
)

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

    val cricketRepository = remember {
        CricketRepository(CricketDatabase.get(context).cricketDao())
    }
    val game501Repository = remember {
        Game501Repository(Game501Database.get(context).game501Dao())
    }
    val sectorRepository = remember {
        SectorRepository(SectorDatabase.get(context).sectorDao())
    }
    val aroundClockRepository = remember {
        AroundClockRepository(AroundClockDatabase.get(context).aroundClockDao())
    }
    val bigRoundRepository = remember {
        BigRoundRepository(BigRoundDatabase.get(context).bigRoundDao())
    }
    val scoreSetRepository = remember {
        ScoreSetRepository(ScoreSetDatabase.get(context).scoreSetDao())
    }

    // ── Сессия ──
    val sessionStartTime = remember { System.currentTimeMillis() }
    val sessionForm = remember { 0.85 + Random.nextDouble() * 0.30 }

    var onboardingDone by remember { mutableStateOf(SettingsStorage.isOnboardingDone(context)) }
    var stage by remember { mutableStateOf(if (onboardingDone) "loading" else "welcome") }
    var screen by remember { mutableStateOf("main") }

    var cricketGame by remember { mutableStateOf<CricketGame?>(null) }
    var game501 by remember { mutableStateOf<Game501?>(null) }
    var sectorToPlay by remember { mutableStateOf<Int?>(null) }

    // Параметры текущей игры «Кругосветка»
    var aroundClockTarget by remember { mutableStateOf<AroundClockTarget?>(null) }
    var aroundClockOrder by remember { mutableStateOf<AroundClockOrder?>(null) }
    var aroundClockHits by remember { mutableStateOf(1) }

    // Категория «Большого раунда»
    var bigRoundCategory by remember { mutableStateOf<BigRoundCategory?>(null) }

    // ── Системная кнопка «Назад» ──
    // Работает только на этапе "main". Возвращает на родительский экран.
    // На главном экране (screen == "main") — не перехватывает,
    // Android сам закроет приложение.
    BackHandler(enabled = stage == "main" && screen != "main") {
        PARENT_SCREEN[screen]?.let { parent -> screen = parent }
    }

    if (stage == "loading") {
        LaunchedEffect(Unit) { delay(3000); stage = "main" }
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
                onStatsClick = { screen = "stats_menu" },
                onSettingsClick = { screen = "settings" }
            )

            // ── НОВЫЙ экран-меню статистики (сетка 2x3) ──
            "stats_menu" -> StatsMenuScreen(
                cricketRepository = cricketRepository,
                onCricket = { screen = "stats_cricket" },
                on501 = { screen = "stats_501" },
                onSector = { screen = "sector_stats" },
                onBigRound = { screen = "biground_stats" },
                onScoreSet = { screen = "scoreset_stats" },
                onAroundClock = { screen = "aroundclock_stats" },
                onBack = { screen = "main" }
            )

            // ── Статистика Крикета (открывается сразу на вкладке 0) ──
            "stats_cricket" -> StatsScreen(
                repository = cricketRepository,
                initialTab = 0,
                onBack = { screen = "stats_menu" }
            )

            // ── Статистика 501 (открывается сразу на вкладке 1) ──
            "stats_501" -> StatsScreen(
                repository = cricketRepository,
                initialTab = 1,
                onBack = { screen = "stats_menu" }
            )

            "settings" -> SettingsScreen(
                onBack = { screen = "main" },
                onFactoryReset = {
                    cricketGame = null
                    game501 = null
                    sectorToPlay = null
                    aroundClockTarget = null
                    aroundClockOrder = null
                    aroundClockHits = 1
                    bigRoundCategory = null
                    onboardingDone = false
                    screen = "main"
                    stage = "welcome"
                }
            )

            "training" -> PlaceholderScreen("Тренировка", { screen = "main" })

            "game_select" -> GameSelectScreen(
                onCricket = { screen = "cricket_setup" },
                on501 = { screen = "game501_setup" },
                onSector = { screen = "sector_setup" },
                onAroundClock = { screen = "aroundclock_setup" },
                onBigRound = { screen = "biground_setup" },
                onScoreSet = { screen = "scoreset_setup" },
                onBack = { screen = "main" }
            )

            // ── Крикет ──
            "cricket_setup" -> CricketSetupScreen(
                playerName = SettingsStorage.getPlayerName(context),
                onStartGame = { type: CricketType,
                                players: List<CricketPlayer>,
                                legsPerSet: Int,
                                setsPerMatch: Int,
                                isPairGame: Boolean,
                                startingTeam: Int,
                                autoOkSeconds: Int ->
                    cricketGame = CricketLogic.newGame(
                        type = type,
                        players = players,
                        legsPerSet = legsPerSet,
                        setsPerMatch = setsPerMatch,
                        isPairGame = isPairGame,
                        startingTeamIndex = startingTeam,
                        autoOkSeconds = autoOkSeconds,
                        sessionStartTime = sessionStartTime,
                        sessionForm = sessionForm
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
                            scope.launch { cricketRepository.saveGame(finished) }
                            cricketGame = finished
                            screen = "cricket_stats"
                        },
                        onBack = { screen = "game_select" }
                    )
                } else screen = "cricket_setup"
            }

            "cricket_stats" -> {
                val game = cricketGame
                if (game != null) {
                    CricketStatsScreen(
                        game = game,
                        onPlayAgain = { screen = "cricket_setup" },
                        onBackToMenu = { cricketGame = null; screen = "main" }
                    )
                } else screen = "main"
            }

            // ── x01 ──
            "game501_setup" -> Game501SetupScreen(
                playerName = SettingsStorage.getPlayerName(context),
                onStartGame = { gameType: GameType,
                                outMode: OutMode,
                                players: List<Player501>,
                                legsPerSet: Int,
                                setsPerMatch: Int,
                                isPairGame: Boolean,
                                startingTeam: Int,
                                autoOkSeconds: Int ->
                    game501 = Game501Logic.newGame(
                        gameType = gameType,
                        outMode = outMode,
                        players = players,
                        legsPerSet = legsPerSet,
                        setsPerMatch = setsPerMatch,
                        isPairGame = isPairGame,
                        startingTeamIndex = startingTeam,
                        autoOkSeconds = autoOkSeconds,
                        sessionStartTime = sessionStartTime,
                        sessionForm = sessionForm
                    )
                    screen = "game501_game"
                },
                onBack = { screen = "game_select" }
            )

            "game501_game" -> {
                val game = game501
                if (game != null) {
                    Game501Screen(
                        initialGame = game,
                        onGameFinish = { finished: Game501 ->
                            scope.launch { game501Repository.saveGame(finished) }
                            game501 = finished
                            screen = "game501_stats"
                        },
                        onBack = { screen = "game_select" }
                    )
                } else screen = "game501_setup"
            }

            "game501_stats" -> {
                val game = game501
                if (game != null) {
                    Game501StatsScreen(
                        game = game,
                        onPlayAgain = { screen = "game501_setup" },
                        onBackToMenu = { game501 = null; screen = "main" }
                    )
                } else screen = "main"
            }

            // ── Сектор ──
            "sector_setup" -> SectorSetupScreen(
                sectorRepository = sectorRepository,
                onStartGame = { sector ->
                    sectorToPlay = sector
                    screen = "sector_game"
                },
                onOpenStats = { screen = "sector_stats" },
                onBack = { screen = "game_select" }
            )

            "sector_game" -> {
                val sector = sectorToPlay
                if (sector != null) {
                    SectorGameScreen(
                        sector = sector,
                        repository = sectorRepository,
                        onFinish = { _score ->
                            sectorToPlay = null
                            screen = "game_select"
                        },
                        onBack = { screen = "game_select" }
                    )
                } else screen = "sector_setup"
            }

            "sector_stats" -> SectorStatsScreen(
                repository = sectorRepository,
                onBack = { screen = "stats_menu" }
            )

            // ── Кругосветка ──
            "aroundclock_setup" -> AroundClockSetupScreen(
                playerName = SettingsStorage.getPlayerName(context),
                repository = aroundClockRepository,
                onStartGame = { target, order, hits ->
                    aroundClockTarget = target
                    aroundClockOrder = order
                    aroundClockHits = hits
                    screen = "aroundclock_game"
                },
                onOpenStats = { screen = "aroundclock_stats" },
                onBack = { screen = "game_select" }
            )

            "aroundclock_game" -> {
                val t = aroundClockTarget
                val o = aroundClockOrder
                if (t != null && o != null) {
                    AroundClockGameScreen(
                        target = t,
                        order = o,
                        hitsRequired = aroundClockHits,
                        repository = aroundClockRepository,
                        onFinish = { totalDarts, completed, sectorResults ->
                            scope.launch {
                                aroundClockRepository.saveGame(
                                    target = t,
                                    order = o,
                                    hitsRequired = aroundClockHits,
                                    totalDarts = totalDarts,
                                    completed = completed,
                                    sectorResults = sectorResults
                                )
                            }
                            aroundClockTarget = null
                            aroundClockOrder = null
                            aroundClockHits = 1
                            screen = "game_select"
                        },
                        onBack = { screen = "game_select" }
                    )
                } else screen = "aroundclock_setup"
            }

            "aroundclock_stats" -> AroundClockStatsScreen(
                repository = aroundClockRepository,
                onBack = { screen = "stats_menu" }
            )

            // ── Большой раунд ──
            "biground_setup" -> BigRoundSetupScreen(
                playerName = SettingsStorage.getPlayerName(context),
                repository = bigRoundRepository,
                onStartGame = { category ->
                    bigRoundCategory = category
                    screen = "biground_game"
                },
                onOpenStats = { screen = "biground_stats" },
                onBack = { screen = "game_select" }
            )

            "biground_game" -> {
                val cat = bigRoundCategory
                if (cat != null) {
                    BigRoundGameScreen(
                        category = cat,
                        repository = bigRoundRepository,
                        onFinish = { _score ->
                            bigRoundCategory = null
                            screen = "game_select"
                        },
                        onBack = { screen = "game_select" }
                    )
                } else screen = "biground_setup"
            }

            "biground_stats" -> BigRoundStatsScreen(
                repository = bigRoundRepository,
                onBack = { screen = "stats_menu" }
            )

            // ── Набор очков ──
            "scoreset_setup" -> ScoreSetSetupScreen(
                playerName = SettingsStorage.getPlayerName(context),
                repository = scoreSetRepository,
                onStartGame = { screen = "scoreset_game" },
                onOpenStats = { screen = "scoreset_stats" },
                onBack = { screen = "game_select" }
            )

            "scoreset_game" -> ScoreSetGameScreen(
                repository = scoreSetRepository,
                onFinish = { _score ->
                    screen = "game_select"
                },
                onBack = { screen = "game_select" }
            )

            "scoreset_stats" -> ScoreSetStatsScreen(
                repository = scoreSetRepository,
                onBack = { screen = "stats_menu" }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = Accent)
    }
}
