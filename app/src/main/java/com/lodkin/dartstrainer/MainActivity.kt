package com.lodkin.dartstrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.lodkin.dartstrainer.data.freerecords.FreePlayRecordsDatabase
import com.lodkin.dartstrainer.data.freerecords.FreePlayRecordsRepository
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
import com.lodkin.dartstrainer.data.training.TrainingBlocksStorage
import com.lodkin.dartstrainer.data.training.TrainingBotLevelStorage
import com.lodkin.dartstrainer.data.training.TrainingDatabase
import com.lodkin.dartstrainer.data.training.TrainingMode
import com.lodkin.dartstrainer.data.training.TrainingPlanner
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingSession
import com.lodkin.dartstrainer.data.training.TrainingStateStorage
import com.lodkin.dartstrainer.data.training.TrainingStatsAnalyzer
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
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
import com.lodkin.dartstrainer.ui.training.BlockExerciseItem
import com.lodkin.dartstrainer.ui.training.ExerciseSummary
import com.lodkin.dartstrainer.ui.training.PlannedExercise
import com.lodkin.dartstrainer.ui.training.SummaryBlockType
import com.lodkin.dartstrainer.ui.training.SummaryStatus
import com.lodkin.dartstrainer.ui.training.TrainingBlockScreen
import com.lodkin.dartstrainer.ui.training.TrainingMenuScreen
import com.lodkin.dartstrainer.ui.training.TrainingProgressScreen
import com.lodkin.dartstrainer.ui.training.TrainingSettingsScreen
import com.lodkin.dartstrainer.ui.training.TrainingSummaryScreen
import com.lodkin.dartstrainer.ui.training.TrainingTimerOverlay
import com.lodkin.dartstrainer.ui.training.TrainingWarmupScreen
import com.lodkin.dartstrainer.ui.training.defaultMainBlockItems
import com.lodkin.dartstrainer.ui.training.defaultMainExercises
import com.lodkin.dartstrainer.ui.training.defaultPsychologyExercises
import com.lodkin.dartstrainer.ui.training.defaultStrengthExercises
import com.lodkin.dartstrainer.ui.training.warmupBlockItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val PARENT_SCREEN: Map<String, String> = mapOf(
    "stats_menu" to "main",
    "stats_cricket" to "stats_menu",
    "stats_501" to "stats_menu",
    "settings" to "main",
    "training" to "main",
    "training_settings" to "training",
    "training_progress" to "training",
    "training_warmup" to "training",
    "training_block" to "training",
    "training_exercise" to "training_block",
    "training_summary" to "training",
    "training_block_exercise" to "training_summary",
    "training_extra_game" to "training_summary",
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
    "training_exercise",
    "training_block_exercise",
    "training_extra_game"
)

private enum class TrainingBlockPhase {
    WARMUP,
    MAIN,
    PSYCHOLOGY,
    STRENGTH,
    FINISHED
}

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

    val cricketRepository = remember { CricketRepository(CricketDatabase.get(context).cricketDao()) }
    val game501Repository = remember { Game501Repository(Game501Database.get(context).game501Dao()) }
    val sectorRepository = remember { SectorRepository(SectorDatabase.get(context).sectorDao()) }
    val aroundClockRepository = remember { AroundClockRepository(AroundClockDatabase.get(context).aroundClockDao()) }
    val bigRoundRepository = remember { BigRoundRepository(BigRoundDatabase.get(context).bigRoundDao()) }
    val scoreSetRepository = remember { ScoreSetRepository(ScoreSetDatabase.get(context).scoreSetDao()) }
    val trainingRepository = remember { TrainingRepository(TrainingDatabase.getInstance(context)) }
    val freePlayRecordsRepository = remember {
        FreePlayRecordsRepository(FreePlayRecordsDatabase.get(context))
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
    var autoOpenTimeDialog by remember { mutableStateOf(true) }

    var currentPhase by remember { mutableStateOf(TrainingBlockPhase.WARMUP) }
    var mainExercises by remember { mutableStateOf<List<PlannedExercise>>(emptyList()) }
    var psychologyExercises by remember { mutableStateOf<List<PlannedExercise>>(emptyList()) }
    var strengthExercises by remember { mutableStateOf<List<PlannedExercise>>(emptyList()) }
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var exerciseStatuses by remember { mutableStateOf<Map<Int, SummaryStatus>>(emptyMap()) }
    var currentWeakness by remember { mutableStateOf<WeaknessType?>(null) }
    var currentLevel by remember { mutableStateOf(1) }

    var blockExerciseId by remember { mutableStateOf<String?>(null) }
    var extraGameId by remember { mutableStateOf<String?>(null) }

    var exerciseStartMillis by remember { mutableStateOf(0L) }

    var isTraining501 by remember { mutableStateOf(false) }
    var isTrainingCricket by remember { mutableStateOf(false) }

    var showResumeDialog by remember { mutableStateOf(false) }
    var pendingResume by remember { mutableStateOf<TrainingStateStorage.Snapshot?>(null) }

    fun saveCurrentTrainingState() {
        if (trainingSessionId <= 0L) return
        if (currentPhase == TrainingBlockPhase.FINISHED) return
        TrainingStateStorage.save(
            context = context,
            sessionId = trainingSessionId,
            minutes = trainingMinutes,
            elapsedSeconds = trainingElapsedSeconds,
            phaseName = currentPhase.name,
            exerciseIndex = currentExerciseIndex,
            statuses = exerciseStatuses.mapValues { it.value.name }
        )
    }

    LaunchedEffect(isTrainingActive, trainingPaused, screen) {
        if (!isTrainingActive || trainingPaused || screen !in TRAINING_SCREENS) return@LaunchedEffect
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

    LaunchedEffect(trainingSessionId, currentPhase, currentExerciseIndex, exerciseStatuses) {
        if (isTrainingActive && trainingSessionId > 0L) {
            saveCurrentTrainingState()
        }
    }

    LaunchedEffect(isTrainingActive, screen) {
        if (!isTrainingActive || screen !in TRAINING_SCREENS) return@LaunchedEffect
        while (true) {
            delay(30_000L)
            saveCurrentTrainingState()
        }
    }

    fun resetTrainingState() {
        isTrainingActive = false
        trainingElapsedSeconds = 0
        trainingPaused = false
        trainingSessionId = 0L
        currentPhase = TrainingBlockPhase.WARMUP
        mainExercises = emptyList()
        psychologyExercises = emptyList()
        strengthExercises = emptyList()
        currentExerciseIndex = 0
        exerciseStatuses = emptyMap()
        blockExerciseId = null
        extraGameId = null
        exerciseStartMillis = 0L
        isTraining501 = false
        isTrainingCricket = false
    }

    fun activePhases(): List<TrainingBlockPhase> {
        val list = mutableListOf<TrainingBlockPhase>()
        if (TrainingBlocksStorage.isWarmupEnabled(context)) list.add(TrainingBlockPhase.WARMUP)
        list.add(TrainingBlockPhase.MAIN)
        if (TrainingBlocksStorage.isPsychologyEnabled(context)) list.add(TrainingBlockPhase.PSYCHOLOGY)
        if (TrainingBlocksStorage.isStrengthEnabled(context)) list.add(TrainingBlockPhase.STRENGTH)
        return list
    }

    fun exercisesForPhase(phase: TrainingBlockPhase): List<PlannedExercise> = when (phase) {
        TrainingBlockPhase.MAIN -> mainExercises
        TrainingBlockPhase.PSYCHOLOGY -> psychologyExercises
        TrainingBlockPhase.STRENGTH -> strengthExercises
        else -> emptyList()
    }

    fun nextPhaseAfter(phase: TrainingBlockPhase): TrainingBlockPhase {
        val phases = activePhases()
        val idx = phases.indexOf(phase)
        return if (idx >= 0 && idx < phases.lastIndex) phases[idx + 1] else TrainingBlockPhase.FINISHED
    }

    fun isLastActivePhase(phase: TrainingBlockPhase): Boolean {
        val phases = activePhases()
        return phases.isNotEmpty() && phases.last() == phase
    }

    fun goToNextPhase() {
        val next = nextPhaseAfter(currentPhase)
        currentPhase = next
        currentExerciseIndex = 0
        exerciseStatuses = emptyMap()
        if (next == TrainingBlockPhase.FINISHED) {
            screen = "training_summary"
        } else {
            screen = "training_block"
        }
    }

    fun blockTypeForPhase(phase: TrainingBlockPhase): SummaryBlockType = when (phase) {
        TrainingBlockPhase.WARMUP -> SummaryBlockType.WARMUP
        TrainingBlockPhase.MAIN -> SummaryBlockType.MAIN
        TrainingBlockPhase.PSYCHOLOGY -> SummaryBlockType.PSYCHOLOGY
        TrainingBlockPhase.STRENGTH -> SummaryBlockType.STRENGTH
        TrainingBlockPhase.FINISHED -> SummaryBlockType.STRENGTH
    }

    fun blockItemsForPhase(phase: TrainingBlockPhase, recommendedIds: Set<String>): List<BlockExerciseItem> = when (phase) {
        TrainingBlockPhase.WARMUP -> warmupBlockItems()
        TrainingBlockPhase.MAIN -> defaultMainBlockItems(recommendedIds)
        TrainingBlockPhase.PSYCHOLOGY -> psychologyExercises.map { BlockExerciseItem(it.id, it.title) }
        TrainingBlockPhase.STRENGTH -> strengthExercises.map { BlockExerciseItem(it.id, it.title) }
        TrainingBlockPhase.FINISHED -> defaultMainBlockItems(recommendedIds)
    }

    fun finishCurrentMainExercise() {
        exerciseStatuses = exerciseStatuses.toMutableMap().apply {
            put(currentExerciseIndex, SummaryStatus.DONE)
        }
        val list = exercisesForPhase(TrainingBlockPhase.MAIN)
        if (currentExerciseIndex + 1 < list.size) {
            currentExerciseIndex += 1
            screen = "training_block"
        } else {
            screen = "training_summary"
        }
    }

    BackHandler(enabled = stage == "main" && screen != "main") {
        PARENT_SCREEN[screen]?.let { parent ->
            if (parent == "stats_menu") statsMenuKey++
            if (parent == "main") { resetTrainingState(); TrainingStateStorage.clear(context); autoOpenTimeDialog = true }
            if (screen == "training_summary") return@BackHandler
            if (screen in TRAINING_SCREENS && parent == "training") {
                autoOpenTimeDialog = false
            }
            screen = parent
        }
    }

    if (stage == "loading") LaunchedEffect(Unit) { delay(3000); stage = "main" }

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
                        val snap = TrainingStateStorage.load(context)
                        if (snap != null && snap.sessionId > 0L) {
                            pendingResume = snap
                            showResumeDialog = true
                            autoOpenTimeDialog = false
                        } else {
                            autoOpenTimeDialog = true
                        }
                        screen = "training"
                    },
                    onFreePlay = { screen = "game_select" },
                    onStatsClick = { statsMenuKey++; screen = "stats_menu" },
                    onSettingsClick = { screen = "settings" }
                )

                "stats_menu" -> StatsMenuScreen(
                    cricketRepository = cricketRepository,
                    reloadKey = statsMenuKey,
                    onResumeGame501 = { entity ->
                        val r = Game501Serializer.fromJson(entity.stateBlob)
                        if (r != null) { game501 = r; game501ExistingId = entity.id; screen = "game501_game" }
                    },
                    onResumeCricket = { entity ->
                        val r = CricketSerializer.fromJson(entity.stateBlob)
                        if (r != null) { cricketGame = r; cricketExistingId = entity.id; screen = "cricket_game" }
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
                    repository = cricketRepository, initialTab = 0, showTabs = false,
                    onResumeCricket = { entity ->
                        val r = CricketSerializer.fromJson(entity.stateBlob)
                        if (r != null) { cricketGame = r; cricketExistingId = entity.id; screen = "cricket_game" }
                    },
                    onBack = { statsMenuKey++; screen = "stats_menu" }
                )

                "stats_501" -> StatsScreen(
                    repository = cricketRepository, initialTab = 1, showTabs = false,
                    onResumeGame501 = { entity ->
                        val r = Game501Serializer.fromJson(entity.stateBlob)
                        if (r != null) { game501 = r; game501ExistingId = entity.id; screen = "game501_game" }
                    },
                    onBack = { statsMenuKey++; screen = "stats_menu" }
                )

                "settings" -> SettingsScreen(
                    onBack = { screen = "main" },
                    onFactoryReset = {
                        cricketGame = null; cricketExistingId = 0L
                        game501 = null; game501ExistingId = 0L
                        sectorToPlay = null
                        aroundClockTarget = null; aroundClockOrder = null; aroundClockHits = 1
                        bigRoundCategory = null
                        resetTrainingState(); autoOpenTimeDialog = true
                        onboardingDone = false; screen = "main"; stage = "welcome"
                    }
                )

                "training" -> TrainingMenuScreen(
                    playerName = SettingsStorage.getPlayerName(context),
                    repository = trainingRepository,
                    autoOpenTimeDialog = autoOpenTimeDialog,
                    onStartTraining = {
                        autoOpenTimeDialog = false
                        trainingMinutes = context
                            .getSharedPreferences("training_prefs", android.content.Context.MODE_PRIVATE)
                            .getInt("session_minutes", SettingsStorage.getTrainingMinutes(context))
                        scope.launch {
                            val analysis = TrainingStatsAnalyzer.analyze(trainingRepository)
                            val completedCount = trainingRepository.getCompletedSessionCount()
                            currentWeakness = analysis.weakestType
                            currentLevel = analysis.level

                            val session = TrainingSession(
                                dateMillis = System.currentTimeMillis(),
                                plannedMinutes = trainingMinutes,
                                actualMinutes = 0,
                                levelAtStart = analysis.level,
                                mode = TrainingMode.FLEXIBLE,
                                focus = analysis.weakestType,
                                isControl = false,
                                isCompleted = false
                            )
                            trainingSessionId = trainingRepository.insertSession(session)
                            trainingElapsedSeconds = 0
                            trainingPaused = false
                            showTimeUpDialog = false

                            mainExercises = if (analysis.hasEnoughData) {
                                val budgetMain = (trainingMinutes * 0.75).toInt().coerceAtLeast(15)
                                val dynamicIds = TrainingPlanner.buildDynamicMainExerciseIds(
                                    analysis = analysis,
                                    budgetMinutes = budgetMain,
                                    completedTrainingCount = completedCount
                                )
                                val baseMap = defaultMainExercises().associateBy { it.id }
                                dynamicIds
                                    .mapNotNull { d -> baseMap[d.id]?.copy(minutes = d.minutes) }
                                    .ifEmpty { defaultMainExercises() }
                            } else {
                                defaultMainExercises()
                            }

                            psychologyExercises = if (TrainingBlocksStorage.isPsychologyEnabled(context))
                                defaultPsychologyExercises() else emptyList()
                            strengthExercises = if (TrainingBlocksStorage.isStrengthEnabled(context))
                                defaultStrengthExercises() else emptyList()

                            val first = activePhases().firstOrNull() ?: TrainingBlockPhase.MAIN
                            currentPhase = first
                            currentExerciseIndex = 0
                            exerciseStatuses = emptyMap()
                            blockExerciseId = null
                            extraGameId = null
                            exerciseStartMillis = 0L
                            isTraining501 = false
                            isTrainingCricket = false
                            isTrainingActive = true

                            TrainingStateStorage.save(
                                context = context,
                                sessionId = trainingSessionId,
                                minutes = trainingMinutes,
                                elapsedSeconds = 0,
                                phaseName = first.name,
                                exerciseIndex = 0,
                                statuses = emptyMap()
                            )

                            screen = if (first == TrainingBlockPhase.WARMUP) "training_warmup" else "training_block"
                        }
                    },
                    onOpenSettings = {
                        autoOpenTimeDialog = false
                        screen = "training_settings"
                    },
                    onOpenProgress = {
                        autoOpenTimeDialog = false
                        screen = "training_progress"
                    },
                    onBack = {
                        resetTrainingState()
                        TrainingStateStorage.clear(context)
                        autoOpenTimeDialog = true
                        screen = "main"
                    }
                )

                "training_settings" -> TrainingSettingsScreen(
                    repository = trainingRepository,
                    onBack = { screen = "training" }
                )

                "training_progress" -> TrainingProgressScreen(
                    repository = trainingRepository,
                    onBack = { screen = "training" }
                )

                "training_warmup" -> TrainingWarmupScreen(
                    longWarmup = trainingMinutes >= 100,
                    onFinish = {
                        val next = nextPhaseAfter(TrainingBlockPhase.WARMUP)
                        currentPhase = next
                        currentExerciseIndex = 0
                        exerciseStatuses = emptyMap()
                        if (next == TrainingBlockPhase.FINISHED) {
                            screen = "training_summary"
                        } else {
                            screen = "training_block"
                        }
                    },
                    onBack = { screen = "training" }
                )

                "training_block" -> {
                    val list = exercisesForPhase(currentPhase)
                    TrainingBlockScreen(
                        exercises = list,
                        currentIndex = currentExerciseIndex,
                        onStartCurrent = {
                            exerciseStartMillis = System.currentTimeMillis()
                            val current = list.getOrNull(currentExerciseIndex)
                            if (currentPhase == TrainingBlockPhase.MAIN && current != null) {
                                when (current.id) {
                                    "game501" -> {
                                        val playerName = SettingsStorage.getPlayerName(context)
                                            .ifBlank { "Игрок" }
                                        val botLevel = TrainingBotLevelStorage.getLevel501(context, currentLevel)
                                        val players = listOf(
                                            Player501(name = playerName, isBot = false, botLevel = 0, teamIndex = 0),
                                            Player501(name = "Бот $botLevel", isBot = true, botLevel = botLevel, teamIndex = 1)
                                        )
                                        game501 = Game501Logic.newGame(
                                            gameType = GameType.X501,
                                            outMode = OutMode.DOUBLE_OUT,
                                            players = players,
                                            legsPerSet = 1,
                                            setsPerMatch = 1,
                                            isPairGame = false,
                                            startingTeamIndex = 0,
                                            autoOkSeconds = 0,
                                            sessionStartTime = sessionStartTime,
                                            sessionForm = sessionForm
                                        )
                                        game501ExistingId = 0L
                                        isTraining501 = true
                                        screen = "game501_game"
                                    }
                                    "cricket" -> {
                                        val playerName = SettingsStorage.getPlayerName(context)
                                            .ifBlank { "Игрок" }
                                        val botLevel = TrainingBotLevelStorage.getLevelCricket(context, currentLevel)
                                        val players = listOf(
                                            CricketPlayer(name = playerName, isBot = false, botLevel = 0, teamIndex = 0),
                                            CricketPlayer(name = "Бот $botLevel", isBot = true, botLevel = botLevel, teamIndex = 1)
                                        )
                                        cricketGame = CricketLogic.newGame(
                                            type = CricketType.AMERICAN,
                                            players = players,
                                            legsPerSet = 1,
                                            setsPerMatch = 1,
                                            isPairGame = false,
                                            startingTeamIndex = 0,
                                            autoOkSeconds = 0,
                                            sessionStartTime = sessionStartTime,
                                            sessionForm = sessionForm
                                        )
                                        cricketExistingId = 0L
                                        isTrainingCricket = true
                                        screen = "cricket_game"
                                    }
                                    else -> screen = "training_exercise"
                                }
                            } else {
                                screen = "training_exercise"
                            }
                        },
                        onSkipCurrent = {
                            exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                                put(currentExerciseIndex, SummaryStatus.SKIPPED)
                            }
                            if (currentExerciseIndex + 1 < list.size) currentExerciseIndex += 1
                            else screen = "training_summary"
                        },
                        onBack = { screen = "training" }
                    )
                }

                "training_exercise" -> {
                    val list = exercisesForPhase(currentPhase)
                    val current = list.getOrNull(currentExerciseIndex)
                    if (current == null) {
                        screen = "training_block"
                    } else if (currentPhase != TrainingBlockPhase.MAIN) {
                        TrainingExercisePlaceholder(
                            title = current.title,
                            description = current.whyImportant,
                            onDone = {
                                val duration = if (exerciseStartMillis > 0L)
                                    ((System.currentTimeMillis() - exerciseStartMillis) / 1000L).toInt()
                                else 0
                                exerciseStartMillis = 0L

                                if (currentPhase == TrainingBlockPhase.PSYCHOLOGY) {
                                    scope.launch {
                                        trainingRepository.insertExerciseResult(
                                            ExerciseResult(
                                                sessionId = trainingSessionId,
                                                blockType = BlockType.WARMUP,
                                                exerciseId = current.id,
                                                score = 0,
                                                dartsThrown = 0,
                                                timestamp = System.currentTimeMillis(),
                                                durationSeconds = duration,
                                                detailsJson = ""
                                            )
                                        )
                                    }
                                }

                                exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                                    put(currentExerciseIndex, SummaryStatus.DONE)
                                }
                                if (currentExerciseIndex + 1 < list.size) {
                                    currentExerciseIndex += 1; screen = "training_block"
                                } else screen = "training_summary"
                            },
                            onBack = { screen = "training_block" }
                        )
                    } else {
                        MainExerciseRunner(
                            exerciseId = current.id,
                            playerLevel = currentLevel,
                            aroundClockRepository = aroundClockRepository,
                            sectorRepository = sectorRepository,
                            scoreSetRepository = scoreSetRepository,
                            bigRoundRepository = bigRoundRepository,
                            trainingRepository = trainingRepository,
                            onFinish = { blockType, score, darts ->
                                val duration = if (exerciseStartMillis > 0L)
                                    ((System.currentTimeMillis() - exerciseStartMillis) / 1000L).toInt()
                                else 0
                                exerciseStartMillis = 0L

                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = blockType,
                                            exerciseId = current.id,
                                            score = score,
                                            dartsThrown = darts,
                                            timestamp = System.currentTimeMillis(),
                                            durationSeconds = duration,
                                            detailsJson = ""
                                        )
                                    )
                                    // Обновляем тренировочный рекорд.
                                    val lowerDartsBetter = current.id.startsWith("aroundclock_")
                                    trainingRepository.updateTrainingRecordIfBetter(
                                        exerciseId = current.id,
                                        score = score,
                                        darts = darts,
                                        lowerDartsBetter = lowerDartsBetter
                                    )
                                }
                                exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                                    put(currentExerciseIndex, SummaryStatus.DONE)
                                }
                                if (currentExerciseIndex + 1 < list.size) {
                                    currentExerciseIndex += 1; screen = "training_block"
                                } else screen = "training_summary"
                            },
                            onBack = { screen = "training_block" }
                        )
                    }
                }

                "training_summary" -> {
                    val list = exercisesForPhase(currentPhase)
                    val summaries = list.mapIndexed { index, ex ->
                        ExerciseSummary(
                            title = ex.title,
                            minutes = ex.minutes,
                            status = exerciseStatuses[index] ?: SummaryStatus.NOT_DONE
                        )
                    }
                    val recommendedIds = recommendedGameIds(currentWeakness)
                    val isLast = isLastActivePhase(currentPhase)

                    TrainingSummaryScreen(
                        playerName = SettingsStorage.getPlayerName(context),
                        planMinutes = trainingMinutes,
                        actualSeconds = trainingElapsedSeconds,
                        exercises = summaries,
                        blockType = blockTypeForPhase(currentPhase),
                        blockExercises = blockItemsForPhase(currentPhase, recommendedIds),
                        isLastBlock = isLast,
                        recommendedIds = recommendedIds,
                        onContinue = {
                            if (isLast) {
                                resetTrainingState()
                                TrainingStateStorage.clear(context)
                                autoOpenTimeDialog = false
                                screen = "main"
                            } else {
                                goToNextPhase()
                            }
                        },
                        onPlayExercise = { id ->
                            if (id == "game501") {
                                val playerName = SettingsStorage.getPlayerName(context)
                                    .ifBlank { "Игрок" }
                                val botLevel = TrainingBotLevelStorage.getLevel501(context, currentLevel)
                                val players = listOf(
                                    Player501(name = playerName, isBot = false, botLevel = 0, teamIndex = 0),
                                    Player501(name = "Бот $botLevel", isBot = true, botLevel = botLevel, teamIndex = 1)
                                )
                                game501 = Game501Logic.newGame(
                                    gameType = GameType.X501,
                                    outMode = OutMode.DOUBLE_OUT,
                                    players = players,
                                    legsPerSet = 1,
                                    setsPerMatch = 1,
                                    isPairGame = false,
                                    startingTeamIndex = 0,
                                    autoOkSeconds = 0,
                                    sessionStartTime = sessionStartTime,
                                    sessionForm = sessionForm
                                )
                                game501ExistingId = 0L
                                isTraining501 = true
                                exerciseStartMillis = System.currentTimeMillis()
                                screen = "game501_game"
                            } else if (id == "cricket") {
                                val playerName = SettingsStorage.getPlayerName(context)
                                    .ifBlank { "Игрок" }
                                val botLevel = TrainingBotLevelStorage.getLevelCricket(context, currentLevel)
                                val players = listOf(
                                    CricketPlayer(name = playerName, isBot = false, botLevel = 0, teamIndex = 0),
                                    CricketPlayer(name = "Бот $botLevel", isBot = true, botLevel = botLevel, teamIndex = 1)
                                )
                                cricketGame = CricketLogic.newGame(
                                    type = CricketType.AMERICAN,
                                    players = players,
                                    legsPerSet = 1,
                                    setsPerMatch = 1,
                                    isPairGame = false,
                                    startingTeamIndex = 0,
                                    autoOkSeconds = 0,
                                    sessionStartTime = sessionStartTime,
                                    sessionForm = sessionForm
                                )
                                cricketExistingId = 0L
                                isTrainingCricket = true
                                exerciseStartMillis = System.currentTimeMillis()
                                screen = "cricket_game"
                            } else {
                                blockExerciseId = id
                                exerciseStartMillis = System.currentTimeMillis()
                                screen = "training_block_exercise"
                            }
                        },
                        onPlayGame = { id ->
                            extraGameId = id
                            exerciseStartMillis = System.currentTimeMillis()
                            screen = "training_extra_game"
                        },
                        onFinishTraining = {
                            resetTrainingState()
                            TrainingStateStorage.clear(context)
                            autoOpenTimeDialog = false
                            screen = "main"
                        }
                    )
                }

                "training_block_exercise" -> {
                    val id = blockExerciseId
                    if (id == null) {
                        screen = "training_summary"
                    } else {
                        val title = blockItemsForPhase(currentPhase, recommendedGameIds(currentWeakness))
                            .firstOrNull { it.id == id }?.title ?: "Упражнение"
                        TrainingExercisePlaceholder(
                            title = title,
                            description = "",
                            onDone = {
                                exerciseStartMillis = 0L
                                blockExerciseId = null
                                screen = "training_summary"
                            },
                            onBack = {
                                exerciseStartMillis = 0L
                                blockExerciseId = null
                                screen = "training_summary"
                            }
                        )
                    }
                }

                "training_extra_game" -> {
                    val gameId = extraGameId
                    if (gameId == null) screen = "training_summary"
                    else {
                        MainExerciseRunner(
                            exerciseId = gameId,
                            playerLevel = currentLevel,
                            aroundClockRepository = aroundClockRepository,
                            sectorRepository = sectorRepository,
                            scoreSetRepository = scoreSetRepository,
                            bigRoundRepository = bigRoundRepository,
                            trainingRepository = trainingRepository,
                            onFinish = { blockType, score, darts ->
                                val duration = if (exerciseStartMillis > 0L)
                                    ((System.currentTimeMillis() - exerciseStartMillis) / 1000L).toInt()
                                else 0
                                exerciseStartMillis = 0L

                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = blockType,
                                            exerciseId = "extra_" + gameId,
                                            score = score,
                                            dartsThrown = darts,
                                            timestamp = System.currentTimeMillis(),
                                            durationSeconds = duration,
                                            detailsJson = ""
                                        )
                                    )
                                    // Обновляем рекорд с тем же ключом, что и основное упражнение.
                                    val lowerDartsBetter = gameId.startsWith("aroundclock_")
                                    trainingRepository.updateTrainingRecordIfBetter(
                                        exerciseId = gameId,
                                        score = score,
                                        darts = darts,
                                        lowerDartsBetter = lowerDartsBetter
                                    )
                                }
                                extraGameId = null
                                screen = "training_summary"
                            },
                            onBack = {
                                exerciseStartMillis = 0L
                                extraGameId = null
                                screen = "training_summary"
                            }
                        )
                    }
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
                    onStartGame = { type, players, legsPerSet, setsPerMatch, isPairGame, startingTeam, autoOkSeconds ->
                        cricketGame = CricketLogic.newGame(
                            type, players, legsPerSet, setsPerMatch, isPairGame,
                            startingTeam, autoOkSeconds, sessionStartTime, sessionForm
                        )
                        cricketExistingId = 0L
                        isTrainingCricket = false
                        screen = "cricket_game"
                    },
                    onBack = { screen = "game_select" }
                )

                "cricket_game" -> {
                    val g = cricketGame
                    if (g != null) CricketGameScreen(
                        repository = cricketRepository, initialGame = g, existingId = cricketExistingId,
                        onGameFinish = { f, id ->
                            if (isTrainingCricket) {
                                val playerWon = f.winnerIndex == 0
                                val player = f.players.getOrNull(0)
                                val mpr = player?.let { p ->
                                    if (p.matchDartsThrown > 0) {
                                        val marks = p.matchHits.values.sum().toFloat()
                                        marks / (p.matchDartsThrown / 3f)
                                    } else 0f
                                } ?: 0f
                                val dur = if (exerciseStartMillis > 0L)
                                    ((System.currentTimeMillis() - exerciseStartMillis) / 1000L).toInt()
                                else 0
                                exerciseStartMillis = 0L
                                val botLevel = g.players.getOrNull(1)?.botLevel ?: 1
                                TrainingBotLevelStorage.updateLevelCricket(context, botLevel, playerWon)
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.CRICKET,
                                            exerciseId = "cricket",
                                            score = mpr.toInt(),
                                            dartsThrown = player?.matchDartsThrown ?: 0,
                                            timestamp = System.currentTimeMillis(),
                                            durationSeconds = dur,
                                            detailsJson = "won=$playerWon"
                                        )
                                    )
                                }
                                isTrainingCricket = false
                                cricketGame = null
                                cricketExistingId = 0L
                                finishCurrentMainExercise()
                            } else {
                                scope.launch { cricketRepository.saveGame(f, existingId = id) }
                                cricketGame = f; cricketExistingId = 0L; screen = "cricket_stats"
                            }
                        },
                        onBack = {
                            isTrainingCricket = false
                            cricketGame = null; cricketExistingId = 0L
                            screen = "game_select"
                        }
                    ) else screen = "cricket_setup"
                }

                "cricket_stats" -> {
                    val g = cricketGame
                    if (g != null) CricketStatsScreen(
                        game = g,
                        onPlayAgain = { screen = "cricket_setup" },
                        onBackToMenu = { cricketGame = null; screen = "main" }
                    ) else screen = "main"
                }

                "game501_setup" -> Game501SetupScreen(
                    playerName = SettingsStorage.getPlayerName(context),
                    onStartGame = { gameType, outMode, players, legsPerSet, setsPerMatch, isPairGame, startingTeam, autoOkSeconds ->
                        game501 = Game501Logic.newGame(
                            gameType, outMode, players, legsPerSet, setsPerMatch, isPairGame,
                            startingTeam, autoOkSeconds, sessionStartTime, sessionForm
                        )
                        game501ExistingId = 0L
                        isTraining501 = false
                        screen = "game501_game"
                    },
                    onBack = { screen = "game_select" }
                )

                "game501_game" -> {
                    val g = game501
                    if (g != null) Game501Screen(
                        repository = game501Repository,
                        freePlayRecordsRepository = freePlayRecordsRepository,
                        initialGame = g,
                        existingId = game501ExistingId,
                        isTrainingMode = isTraining501,
                        trainingRepository = trainingRepository,
                        onGameFinish = { f, id ->
                            if (isTraining501) {
                                val playerWon = f.winnerIndex == 0
                                val player = f.players.getOrNull(0)
                                val ppr = player?.let { p -> Game501Logic.matchPpr(p) } ?: 0.0
                                val dur = if (exerciseStartMillis > 0L)
                                    ((System.currentTimeMillis() - exerciseStartMillis) / 1000L).toInt()
                                else 0
                                exerciseStartMillis = 0L
                                val botLevel = g.players.getOrNull(1)?.botLevel ?: 1
                                TrainingBotLevelStorage.updateLevel501(context, botLevel, playerWon)
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.GAME_501,
                                            exerciseId = "game501",
                                            score = ppr.toInt(),
                                            dartsThrown = player?.matchDarts ?: 0,
                                            timestamp = System.currentTimeMillis(),
                                            durationSeconds = dur,
                                            detailsJson = "won=$playerWon"
                                        )
                                    )
                                }
                                isTraining501 = false
                                game501 = null
                                game501ExistingId = 0L
                                finishCurrentMainExercise()
                            } else {
                                scope.launch { game501Repository.saveGame(f, existingId = id) }
                                game501 = f; game501ExistingId = 0L; screen = "game501_stats"
                            }
                        },
                        onBack = {
                            isTraining501 = false
                            game501 = null; game501ExistingId = 0L
                            screen = "game_select"
                        }
                    ) else screen = "game501_setup"
                }

                "game501_stats" -> {
                    val g = game501
                    if (g != null) Game501StatsScreen(
                        game = g,
                        onPlayAgain = { screen = "game501_setup" },
                        onBackToMenu = { game501 = null; screen = "main" }
                    ) else screen = "main"
                }

                "sector_setup" -> SectorSetupScreen(
                    sectorRepository = sectorRepository,
                    onStartGame = { s -> sectorToPlay = s; screen = "sector_game" },
                    onOpenStats = { screen = "sector_stats" },
                    onBack = { screen = "game_select" }
                )

                "sector_game" -> {
                    val s = sectorToPlay
                    if (s != null) SectorGameScreen(
                        sector = s, repository = sectorRepository,
                        isTrainingMode = false,
                        onFinish = { _ -> sectorToPlay = null; screen = "game_select" },
                        onBack = { screen = "game_select" }
                    ) else screen = "sector_setup"
                }

                "sector_stats" -> SectorStatsScreen(
                    repository = sectorRepository,
                    onBack = { statsMenuKey++; screen = "stats_menu" }
                )

                "aroundclock_setup" -> AroundClockSetupScreen(
                    playerName = SettingsStorage.getPlayerName(context),
                    repository = aroundClockRepository,
                    onStartGame = { t, o, h -> aroundClockTarget = t; aroundClockOrder = o; aroundClockHits = h; screen = "aroundclock_game" },
                    onOpenStats = { screen = "aroundclock_stats" },
                    onBack = { screen = "game_select" }
                )

                "aroundclock_game" -> {
                    val t = aroundClockTarget; val o = aroundClockOrder
                    if (t != null && o != null) AroundClockGameScreen(
                        target = t, order = o, hitsRequired = aroundClockHits,
                        repository = aroundClockRepository,
                        onFinish = { td, c, sr ->
                            scope.launch {
                                aroundClockRepository.saveGame(t, o, aroundClockHits, td, c, sr)
                            }
                            aroundClockTarget = null; aroundClockOrder = null; aroundClockHits = 1
                            screen = "game_select"
                        },
                        onBack = { screen = "game_select" }
                    ) else screen = "aroundclock_setup"
                }

                "aroundclock_stats" -> AroundClockStatsScreen(
                    repository = aroundClockRepository,
                    onBack = { statsMenuKey++; screen = "stats_menu" }
                )

                "biground_setup" -> BigRoundSetupScreen(
                    playerName = SettingsStorage.getPlayerName(context),
                    repository = bigRoundRepository,
                    onStartGame = { c -> bigRoundCategory = c; screen = "biground_game" },
                    onOpenStats = { screen = "biground_stats" },
                    onBack = { screen = "game_select" }
                )

                "biground_game" -> {
                    val c = bigRoundCategory
                    if (c != null) BigRoundGameScreen(
                        category = c, repository = bigRoundRepository,
                        isTrainingMode = false,
                        onFinish = { _ -> bigRoundCategory = null; screen = "game_select" },
                        onBack = { screen = "game_select" }
                    ) else screen = "biground_setup"
                }

                "biground_stats" -> BigRoundStatsScreen(
                    repository = bigRoundRepository,
                    onBack = { statsMenuKey++; screen = "stats_menu" }
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
                    isTrainingMode = false,
                    onFinish = { ts, ap ->
                        scope.launch { scoreSetRepository.saveGame(ts, ap) }
                        screen = "game_select"
                    },
                    onBack = { screen = "game_select" }
                )

                "scoreset_stats" -> ScoreSetStatsScreen(
                    repository = scoreSetRepository,
                    onBack = { statsMenuKey++; screen = "stats_menu" }
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

    if (showResumeDialog && pendingResume != null) {
        val snap = pendingResume!!
        val minutesAgo = TrainingStateStorage.minutesSinceLastUpdate(context)
        AlertDialog(
            onDismissRequest = { },
            containerColor = TileBgDark,
            title = {
                Text(
                    "Незавершённая тренировка",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "У тебя есть незавершённая тренировка. " +
                                if (minutesAgo > 0) "Прошло $minutesAgo мин." else "Только что прервана.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Продолжить с того места, где остановился, или начать заново?",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Text(
                    "Продолжить",
                    color = Color(0xFF121212),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent)
                        .clickable {
                            trainingSessionId = snap.sessionId
                            trainingMinutes = snap.minutes
                            trainingElapsedSeconds = snap.elapsedSeconds
                            currentPhase = runCatching {
                                TrainingBlockPhase.valueOf(snap.phaseName)
                            }.getOrDefault(TrainingBlockPhase.MAIN)
                            currentExerciseIndex = snap.exerciseIndex
                            exerciseStatuses = snap.statuses.mapNotNull { (k, v) ->
                                runCatching { k to SummaryStatus.valueOf(v) }.getOrNull()
                            }.toMap()

                            mainExercises = defaultMainExercises()
                            psychologyExercises = if (TrainingBlocksStorage.isPsychologyEnabled(context))
                                defaultPsychologyExercises() else emptyList()
                            strengthExercises = if (TrainingBlocksStorage.isStrengthEnabled(context))
                                defaultStrengthExercises() else emptyList()

                            trainingPaused = false
                            isTrainingActive = true
                            autoOpenTimeDialog = false
                            showResumeDialog = false
                            exerciseStartMillis = 0L
                            isTraining501 = false
                            isTrainingCricket = false

                            screen = when (currentPhase) {
                                TrainingBlockPhase.WARMUP -> "training_warmup"
                                else -> "training_block"
                            }
                            pendingResume = null
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            },
            dismissButton = {
                Text(
                    "Начать заново",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            TrainingStateStorage.clear(context)
                            resetTrainingState()
                            autoOpenTimeDialog = true
                            showResumeDialog = false
                            pendingResume = null
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        )
    }

    if (showTimeUpDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = TileBgDark,
            title = { Text("⏱ Время тренировки вышло", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Column { Text(
                "Ты потренировался $trainingMinutes минут — это твоя цель на сегодня. Что делаем дальше?",
                color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp
            ) } },
            confirmButton = { TextButton(onClick = {
                showTimeUpDialog = false
                resetTrainingState()
                TrainingStateStorage.clear(context)
                autoOpenTimeDialog = false
                screen = "training"
            }) { Text("Закончить", color = ErrorColor, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = {
                showTimeUpDialog = false; trainingMinutes += 20
            }) { Text("Продолжить +20 мин", color = GoldAccent, fontWeight = FontWeight.Bold) } }
        )
    }
}

@Composable
private fun MainExerciseRunner(
    exerciseId: String,
    playerLevel: Int,
    aroundClockRepository: AroundClockRepository,
    sectorRepository: SectorRepository,
    scoreSetRepository: ScoreSetRepository,
    bigRoundRepository: BigRoundRepository,
    trainingRepository: TrainingRepository?,
    onFinish: (BlockType, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val isTraining = trainingRepository != null

    when (exerciseId) {
        "aroundclock_doubles" -> AroundClockGameScreen(
            target = AroundClockTarget.DOUBLE,
            order = AroundClockOrder.ORDERED,
            hitsRequired = 1,
            repository = aroundClockRepository,
            sectorStart = 1,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "aroundclock_doubles" else null,
            onFinish = { totalDarts, _, _ -> onFinish(BlockType.DOUBLES, totalDarts, totalDarts) },
            onBack = onBack
        )
        "aroundclock_sectors" -> AroundClockGameScreen(
            target = AroundClockTarget.SINGLE,
            order = AroundClockOrder.ORDERED,
            hitsRequired = 1,
            repository = aroundClockRepository,
            sectorStart = 1,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "aroundclock_sectors" else null,
            onFinish = { totalDarts, _, _ -> onFinish(BlockType.SECTOR, totalDarts, totalDarts) },
            onBack = onBack
        )
        "aroundclock_trebles" -> {
            val sectorStart = if (playerLevel >= 9) 1 else 10
            AroundClockGameScreen(
                target = AroundClockTarget.TRIPLE,
                order = AroundClockOrder.ORDERED,
                hitsRequired = 1,
                repository = aroundClockRepository,
                sectorStart = sectorStart,
                trainingRepository = trainingRepository,
                trainingRecordKey = if (isTraining) "aroundclock_trebles" else null,
                onFinish = { totalDarts, _, _ -> onFinish(BlockType.TREBLES, totalDarts, totalDarts) },
                onBack = onBack
            )
        }
        "sector20" -> SectorGameScreen(
            sector = 20,
            repository = sectorRepository,
            isTrainingMode = isTraining,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "sector20" else null,
            onFinish = { score -> onFinish(BlockType.SCORE, score, 30) },
            onBack = onBack
        )
        "scoreset" -> ScoreSetGameScreen(
            repository = scoreSetRepository,
            isTrainingMode = isTraining,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "scoreset" else null,
            onFinish = { totalScore, _ -> onFinish(BlockType.SCORE, totalScore, 30) },
            onBack = onBack
        )
        "biground" -> BigRoundGameScreen(
            category = BigRoundCategory.MALE,
            repository = bigRoundRepository,
            isTrainingMode = isTraining,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "biground" else null,
            onFinish = { score -> onFinish(BlockType.SECTOR, score, 63) },
            onBack = onBack
        )
        else -> TrainingExercisePlaceholder(
            title = "Упражнение",
            description = "Это упражнение пока в разработке.",
            onDone = { onFinish(BlockType.SCORE, 0, 0) },
            onBack = onBack
        )
    }
}

private fun recommendedGameIds(weakness: WeaknessType?): Set<String> = when (weakness) {
    WeaknessType.DOUBLES -> setOf("aroundclock_doubles", "game501")
    WeaknessType.SCORE -> setOf("sector20", "scoreset")
    WeaknessType.ACCURACY -> setOf("aroundclock_sectors", "biground")
    WeaknessType.TREBLES -> setOf("aroundclock_trebles", "biground", "cricket")
    null -> emptySet()
}

@Composable
private fun TrainingExercisePlaceholder(
    title: String,
    description: String,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark)
                    .clickable { onBack() }.padding(horizontal = 14.dp, vertical = 10.dp)
            ) { Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium) }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(title, color = GoldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (description.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(description, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Упражнение в разработке",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                            .background(Accent).clickable { onDone() }
                            .padding(horizontal = 32.dp, vertical = 14.dp)
                    ) { Text("Готово", color = Color(0xFF121212), fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
