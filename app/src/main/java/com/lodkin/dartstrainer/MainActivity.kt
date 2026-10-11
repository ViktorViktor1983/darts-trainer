package com.lodkin.dartstrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.lodkin.dartstrainer.data.cricket.CricketSerializer
import com.lodkin.dartstrainer.data.cricket.CricketType
import com.lodkin.dartstrainer.data.game501.Game501
import com.lodkin.dartstrainer.data.game501.Game501Database
import com.lodkin.dartstrainer.data.game501.Game501Logic
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.game501.Game501Serializer
import com.lodkin.dartstrainer.data.game501.GameType
import com.lodkin.dartstrainer.data.game501.OutMode
import com.lodkin.dartstrainer.data.game501.Player501
import com.lodkin.dartstrainer.data.scoreset.ScoreSetDatabase
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorDatabase
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.data.training.BlockType
import com.lodkin.dartstrainer.data.training.ExerciseResult
import com.lodkin.dartstrainer.data.training.TrainingDatabase
import com.lodkin.dartstrainer.data.training.TrainingMode
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingSession
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
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
import com.lodkin.dartstrainer.ui.training.ExerciseSummary
import com.lodkin.dartstrainer.ui.training.PlannedExercise
import com.lodkin.dartstrainer.ui.training.SummaryStatus
import com.lodkin.dartstrainer.ui.training.TrainingBlockScreen
import com.lodkin.dartstrainer.ui.training.TrainingMenuScreen
import com.lodkin.dartstrainer.ui.training.TrainingSummaryScreen
import com.lodkin.dartstrainer.ui.training.TrainingTimerOverlay
import com.lodkin.dartstrainer.ui.training.TrainingWarmupScreen
import com.lodkin.dartstrainer.ui.training.defaultMainExercises
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val PARENT_SCREEN: Map<String, String> = mapOf(
    "stats_menu" to "main",
    "stats_cricket" to "stats_menu",
    "stats_501" to "stats_menu",
    "settings" to "main",
    "training" to "main",
    "training_warmup" to "training",
    "training_block" to "training",
    "training_exercise" to "training_block",
    "training_summary" to "training",
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

private val TRAINING_SCREENS: Set<String> = setOf(
    "training_warmup",
    "training_block",
    "training_exercise"
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
    val trainingRepository = remember {
        TrainingRepository(TrainingDatabase.getInstance(context))
    }

    val sessionStartTime = remember { System.currentTimeMillis() }
    val sessionForm = remember { 0.85 + Random.nextDouble() * 0.30 }

    var onboardingDone by remember { mutableStateOf(SettingsStorage.isOnboardingDone(context)) }
    var stage by remember { mutableStateOf(if (onboardingDone) "loading" else "welcome") }
    var screen by remember { mutableStateOf("main") }

    var cricketGame by remember { mutableStateOf<CricketGame?>(null) }
    var cricketExistingId by remember { mutableStateOf(0L) }

    var game501 by remember { mutableStateOf<Game501?>(null) }
    var game501ExistingId by remember { mutableStateOf(0L) }

    var sectorToPlay by remember { mutableStateOf<Int?>(null) }

    var aroundClockTarget by remember { mutableStateOf<AroundClockTarget?>(null) }
    var aroundClockOrder by remember { mutableStateOf<AroundClockOrder?>(null) }
    var aroundClockHits by remember { mutableStateOf(1) }

    var bigRoundCategory by remember { mutableStateOf<BigRoundCategory?>(null) }

    var statsMenuKey by remember { mutableStateOf(0) }

    var trainingMinutes by remember {
        mutableStateOf(
            context.getSharedPreferences("training_prefs", android.content.Context.MODE_PRIVATE)
                .getInt("session_minutes", SettingsStorage.getTrainingMinutes(context))
        )
    }

    var trainingSessionId by remember { mutableStateOf(0L) }

    var isTrainingActive by remember { mutableStateOf(false) }
    var trainingElapsedSeconds by remember { mutableStateOf(0) }
    var trainingPaused by remember { mutableStateOf(false) }
    var showTimeUpDialog by remember { mutableStateOf(false) }

    // Автооткрытие диалога времени при заходе в тренировку.
    // true — зашли из главного меню. false — вернулись после завершения.
    var autoOpenTimeDialog by remember { mutableStateOf(true) }

    // Основная часть тренировки
    var mainExercises by remember { mutableStateOf<List<PlannedExercise>>(emptyList()) }
    var currentExerciseIndex by remember { mutableStateOf(0) }
    // Статусы: для каждого индекса — DONE или SKIPPED.
    // Если упражнение не в мапе — ещё не пройдено и не пропущено.
    var exerciseStatuses by remember { mutableStateOf<Map<Int, SummaryStatus>>(emptyMap()) }

    LaunchedEffect(isTrainingActive, trainingPaused, screen) {
        if (!isTrainingActive || trainingPaused || screen !in TRAINING_SCREENS) {
            return@LaunchedEffect
        }
        while (true) {
            delay(1000L)
            trainingElapsedSeconds += 1
            if (trainingElapsedSeconds >= trainingMinutes * 60) {
                showTimeUpDialog = true
                break
            }
        }
    }

    val trainingSecondsLeft = (trainingMinutes * 60 - trainingElapsedSeconds).coerceAtLeast(0)

    // Сбрасывает состояние тренировки (при выходе в меню или после итогов)
    fun resetTrainingState() {
        isTrainingActive = false
        trainingElapsedSeconds = 0
        trainingPaused = false
        trainingSessionId = 0L
        mainExercises = emptyList()
        currentExerciseIndex = 0
        exerciseStatuses = emptyMap()
    }

    BackHandler(enabled = stage == "main" && screen != "main") {
        PARENT_SCREEN[screen]?.let { parent ->
            if (parent == "stats_menu") statsMenuKey++

            if (parent == "main") {
                resetTrainingState()
                autoOpenTimeDialog = true
            }

            if (screen in TRAINING_SCREENS && parent == "training") {
                resetTrainingState()
                autoOpenTimeDialog = false
            }

            if (screen == "training_summary" && parent == "training") {
                // Из итогов не даём уйти назад — только кнопки на экране
                return@BackHandler
            }

            screen = parent
        }
    }

    if (stage == "loading") {
        LaunchedEffect(Unit) { delay(3000); stage = "main" }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    onTraining = {
                        autoOpenTimeDialog = true
                        screen = "training"
                    },
                    onFreePlay = { screen = "game_select" },
                    onStatsClick = {
                        statsMenuKey++
                        screen = "stats_menu"
                    },
                    onSettingsClick = { screen = "settings" }
                )

                "stats_menu" -> StatsMenuScreen(
                    cricketRepository = cricketRepository,
                    reloadKey = statsMenuKey,
                    onResumeGame501 = { entity ->
                        val restored = Game501Serializer.fromJson(entity.stateBlob)
                        if (restored != null) {
                            game501 = restored
                            game501ExistingId = entity.id
                            screen = "game501_game"
                        }
                    },
                    onResumeCricket = { entity ->
                        val restored = CricketSerializer.fromJson(entity.stateBlob)
                        if (restored != null) {
                            cricketGame = restored
                            cricketExistingId = entity.id
                            screen = "cricket_game"
                        }
                    },
                    onCricket = { screen = "stats_cricket" },
                    on501 = { screen = "stats_501" },
                    onSector = { screen = "sector_stats" },
                    onBigRound = { screen = "biground_stats" },
                    onScoreSet = { screen = "scoreset_stats" },
                    onAroundClock = { screen = "aroundclock_stats" },
                    onBack = { screen = "main" }
                )

                "stats_cricket" -> StatsScreen(
                    repository = cricketRepository,
                    initialTab = 0,
                    showTabs = false,
                    onResumeCricket = { entity ->
                        val restored = CricketSerializer.fromJson(entity.stateBlob)
                        if (restored != null) {
                            cricketGame = restored
                            cricketExistingId = entity.id
                            screen = "cricket_game"
                        }
                    },
                    onBack = {
                        statsMenuKey++
                        screen = "stats_menu"
                    }
                )

                "stats_501" -> StatsScreen(
                    repository = cricketRepository,
                    initialTab = 1,
                    showTabs = false,
                    onResumeGame501 = { entity ->
                        val restored = Game501Serializer.fromJson(entity.stateBlob)
                        if (restored != null) {
                            game501 = restored
                            game501ExistingId = entity.id
                            screen = "game501_game"
                        }
                    },
                    onBack = {
                        statsMenuKey++
                        screen = "stats_menu"
                    }
                )

                "settings" -> SettingsScreen(
                    onBack = { screen = "main" },
                    onFactoryReset = {
                        cricketGame = null
                        cricketExistingId = 0L
                        game501 = null
                        game501ExistingId = 0L
                        sectorToPlay = null
                        aroundClockTarget = null
                        aroundClockOrder = null
                        aroundClockHits = 1
                        bigRoundCategory = null
                        resetTrainingState()
                        autoOpenTimeDialog = true
                        onboardingDone = false
                        screen = "main"
                        stage = "welcome"
                    }
                )

                "training" -> TrainingMenuScreen(
                    playerName = SettingsStorage.getPlayerName(context),
                    repository = trainingRepository,
                    autoOpenTimeDialog = autoOpenTimeDialog,
                    onStartTraining = {
                        // Сбрасываем флаг — на следующем возврате диалог не откроется
                        autoOpenTimeDialog = false

                        trainingMinutes = context
                            .getSharedPreferences("training_prefs", android.content.Context.MODE_PRIVATE)
                            .getInt("session_minutes", SettingsStorage.getTrainingMinutes(context))
                        scope.launch {
                            val session = TrainingSession(
                                dateMillis = System.currentTimeMillis(),
                                plannedMinutes = trainingMinutes,
                                actualMinutes = 0,
                                levelAtStart = 1,
                                mode = TrainingMode.FLEXIBLE,
                                focus = null,
                                isControl = false,
                                isCompleted = false
                            )
                            trainingSessionId = trainingRepository.insertSession(session)
                            trainingElapsedSeconds = 0
                            trainingPaused = false
                            showTimeUpDialog = false
                            mainExercises = defaultMainExercises()
                            currentExerciseIndex = 0
                            exerciseStatuses = emptyMap()
                            isTrainingActive = true
                            screen = "training_warmup"
                        }
                    },
                    onBack = {
                        resetTrainingState()
                        autoOpenTimeDialog = true
                        screen = "main"
                    }
                )

                "training_warmup" -> TrainingWarmupScreen(
                    longWarmup = trainingMinutes >= 100,
                    onFinish = { screen = "training_block" },
                    onBack = {
                        resetTrainingState()
                        autoOpenTimeDialog = false
                        screen = "training"
                    }
                )

                "training_block" -> TrainingBlockScreen(
                    exercises = mainExercises,
                    currentIndex = currentExerciseIndex,
                    onStartCurrent = { screen = "training_exercise" },
                    onSkipCurrent = {
                        // Помечаем текущее как SKIPPED
                        exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                            put(currentExerciseIndex, SummaryStatus.SKIPPED)
                        }
                        if (currentExerciseIndex + 1 < mainExercises.size) {
                            currentExerciseIndex += 1
                        } else {
                            // Все упражнения пройдены или пропущены — на итоги
                            screen = "training_summary"
                        }
                    },
                    onBack = {
                        resetTrainingState()
                        autoOpenTimeDialog = false
                        screen = "training"
                    }
                )

                "training_exercise" -> {
                    val current = mainExercises.getOrNull(currentExerciseIndex)
                    if (current == null) {
                        screen = "training_block"
                    } else {
                        when (current.id) {
                            "aroundclock_doubles" -> AroundClockGameScreen(
                                target = AroundClockTarget.DOUBLE,
                                order = AroundClockOrder.ORDERED,
                                hitsRequired = 1,
                                repository = aroundClockRepository,
                                onFinish = { totalDarts, _, _ ->
                                    scope.launch {
                                        val result = ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.DOUBLES,
                                            exerciseId = current.id,
                                            score = totalDarts,
                                            dartsThrown = totalDarts,
                                            timestamp = System.currentTimeMillis(),
                                            detailsJson = ""
                                        )
                                        trainingRepository.insertExerciseResult(result)
                                    }
                                    // Помечаем как DONE
                                    exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                                        put(currentExerciseIndex, SummaryStatus.DONE)
                                    }
                                    if (currentExerciseIndex + 1 < mainExercises.size) {
                                        currentExerciseIndex += 1
                                        screen = "training_block"
                                    } else {
                                        screen = "training_summary"
                                    }
                                },
                                onBack = { screen = "training_block" }
                            )
                            else -> TrainingExercisePlaceholder(
                                title = current.title,
                                onDone = {
                                    exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                                        put(currentExerciseIndex, SummaryStatus.DONE)
                                    }
                                    if (currentExerciseIndex + 1 < mainExercises.size) {
                                        currentExerciseIndex += 1
                                        screen = "training_block"
                                    } else {
                                        screen = "training_summary"
                                    }
                                },
                                onBack = { screen = "training_block" }
                            )
                        }
                    }
                }

                "training_summary" -> {
                    val summaries = mainExercises.mapIndexed { index, ex ->
                        val status = exerciseStatuses[index] ?: SummaryStatus.NOT_DONE
                        ExerciseSummary(
                            title = ex.title,
                            minutes = ex.minutes,
                            status = status
                        )
                    }

                    TrainingSummaryScreen(
                        playerName = SettingsStorage.getPlayerName(context),
                        planMinutes = trainingMinutes,
                        actualSeconds = trainingElapsedSeconds,
                        exercises = summaries,
                        onRestart = {
                            resetTrainingState()
                            autoOpenTimeDialog = false
                            screen = "training"
                        },
                        onFinish = {
                            resetTrainingState()
                            autoOpenTimeDialog = false
                            screen = "main"
                        }
                    )
                }

                "game_select" -> GameSelectScreen(
                    onCricket = { screen = "cricket_setup" },
                    on501 = { screen = "game501_setup" },
                    onSector = { screen = "sector_setup" },
                    onAroundClock = { screen = "aroundclock_setup" },
                    onBigRound = { screen = "biground_setup" },
                    onScoreSet = { screen = "scoreset_setup" },
                    onBack = { screen = "main" }
                )

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
                        cricketExistingId = 0L
                        screen = "cricket_game"
                    },
                    onBack = { screen = "game_select" }
                )

                "cricket_game" -> {
                    val game = cricketGame
                    if (game != null) {
                        CricketGameScreen(
                            repository = cricketRepository,
                            initialGame = game,
                            existingId = cricketExistingId,
                            onGameFinish = { finished: CricketGame, id: Long ->
                                scope.launch {
                                    cricketRepository.saveGame(finished, existingId = id)
                                }
                                cricketGame = finished
                                cricketExistingId = 0L
                                screen = "cricket_stats"
                            },
                            onBack = {
                                cricketGame = null
                                cricketExistingId = 0L
                                screen = "game_select"
                            }
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
                        game501ExistingId = 0L
                        screen = "game501_game"
                    },
                    onBack = { screen = "game_select" }
                )

                "game501_game" -> {
                    val game = game501
                    if (game != null) {
                        Game501Screen(
                            repository = game501Repository,
                            initialGame = game,
                            existingId = game501ExistingId,
                            onGameFinish = { finished: Game501, id: Long ->
                                scope.launch {
                                    game501Repository.saveGame(finished, existingId = id)
                                }
                                game501 = finished
                                game501ExistingId = 0L
                                screen = "game501_stats"
                            },
                            onBack = {
                                game501 = null
                                game501ExistingId = 0L
                                screen = "game_select"
                            }
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
                    onBack = {
                        statsMenuKey++
                        screen = "stats_menu"
                    }
                )

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
                    onBack = {
                        statsMenuKey++
                        screen = "stats_menu"
                    }
                )

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
                    onBack = {
                        statsMenuKey++
                        screen = "stats_menu"
                    }
                )

                "scoreset_setup" -> ScoreSetSetupScreen(
                    playerName = SettingsStorage.getPlayerName(context),
                    repository = scoreSetRepository,
                    onStartGame = { screen = "scoreset_game" },
                    onOpenStats = { screen = "scoreset_stats" },
                    onBack = { screen = "game_select" }
                )

                "scoreset_game" -> ScoreSetGameScreen(
                    repository = scoreSetRepository,
                    onFinish = { totalScore: Int, approaches: List<Int> ->
                        scope.launch {
                            scoreSetRepository.saveGame(
                                totalScore = totalScore,
                                approaches = approaches
                            )
                        }
                        screen = "game_select"
                    },
                    onBack = { screen = "game_select" }
                )

                "scoreset_stats" -> ScoreSetStatsScreen(
                    repository = scoreSetRepository,
                    onBack = {
                        statsMenuKey++
                        screen = "stats_menu"
                    }
                )
            }
        }

        if (isTrainingActive && screen in TRAINING_SCREENS) {
            TrainingTimerOverlay(
                secondsLeft = trainingSecondsLeft,
                isPaused = trainingPaused,
                onTogglePause = { trainingPaused = !trainingPaused }
            )
        }
    }

    if (showTimeUpDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = TileBgDark,
            title = {
                Text(
                    "⏱ Время тренировки вышло",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Ты потренировался $trainingMinutes минут — это твоя цель на сегодня. " +
                                "Что делаем дальше?",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showTimeUpDialog = false
                    resetTrainingState()
                    autoOpenTimeDialog = false
                    screen = "training"
                }) {
                    Text("Закончить", color = ErrorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTimeUpDialog = false
                    trainingMinutes += 20
                }) {
                    Text("Продолжить +20 мин", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun TrainingExercisePlaceholder(
    title: String,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        title,
                        color = GoldAccent,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Упражнение в разработке",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Accent)
                            .clickable { onDone() }
                            .padding(horizontal = 32.dp, vertical = 14.dp)
                    ) {
                        Text(
                            "Готово",
                            color = Color(0xFF121212),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
