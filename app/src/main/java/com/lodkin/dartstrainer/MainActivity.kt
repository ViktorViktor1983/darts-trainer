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
import com.lodkin.dartstrainer.data.training.TrainingDatabase
import com.lodkin.dartstrainer.data.training.TrainingMode
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingSession
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

    var blockExerciseId by remember { mutableStateOf<String?>(null) }
    var extraGameId by remember { mutableStateOf<String?>(null) }

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
    }

    /** Активные фазы тренировки (с учётом настроек блоков). */
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

    fun nextPhaseTitle(phase: TrainingBlockPhase): String? {
        val next = nextPhaseAfter(phase)
        return when (next) {
            TrainingBlockPhase.MAIN -> "Основная часть"
            TrainingBlockPhase.PSYCHOLOGY -> "Психология"
            TrainingBlockPhase.STRENGTH -> "Силовая"
            else -> null
        }
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

    BackHandler(enabled = stage == "main" && screen != "main") {
        PARENT_SCREEN[screen]?.let { parent ->
            if (parent == "stats_menu") statsMenuKey++
            if (parent == "main") { resetTrainingState(); autoOpenTimeDialog = true }
            if (screen == "training_summary") return@BackHandler
            if (screen in TRAINING_SCREENS && parent == "training") {
                resetTrainingState(); autoOpenTimeDialog = false
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
                    onTraining = { autoOpenTimeDialog = true; screen = "training" },
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
                            val snap = trainingRepository.getLastPotential()
                            currentWeakness = snap?.weakestType
                            val session = TrainingSession(
                                dateMillis = System.currentTimeMillis(),
                                plannedMinutes = trainingMinutes,
                                actualMinutes = 0,
                                levelAtStart = snap?.level ?: 1,
                                mode = TrainingMode.FLEXIBLE,
                                focus = snap?.weakestType,
                                isControl = false,
                                isCompleted = false
                            )
                            trainingSessionId = trainingRepository.insertSession(session)
                            trainingElapsedSeconds = 0
                            trainingPaused = false
                            showTimeUpDialog = false

                            mainExercises = defaultMainExercises()
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
                            isTrainingActive = true

                            screen = if (first == TrainingBlockPhase.WARMUP) "training_warmup" else "training_block"
                        }
                    },
                    onOpenSettings = { screen = "training_settings" },
                    onBack = { resetTrainingState(); autoOpenTimeDialog = true; screen = "main" }
                )

                "training_settings" -> TrainingSettingsScreen(
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
                    onBack = { resetTrainingState(); autoOpenTimeDialog = false; screen = "training" }
                )

                "training_block" -> {
                    val list = exercisesForPhase(currentPhase)
                    TrainingBlockScreen(
                        exercises = list,
                        currentIndex = currentExerciseIndex,
                        onStartCurrent = { screen = "training_exercise" },
                        onSkipCurrent = {
                            exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                                put(currentExerciseIndex, SummaryStatus.SKIPPED)
                            }
                            if (currentExerciseIndex + 1 < list.size) currentExerciseIndex += 1
                            else screen = "training_summary"
                        },
                        onBack = { resetTrainingState(); autoOpenTimeDialog = false; screen = "training" }
                    )
                }

                "training_exercise" -> {
                    val list = exercisesForPhase(currentPhase)
                    val current = list.getOrNull(currentExerciseIndex)
                    if (current == null) {
                        screen = "training_block"
                    } else if (currentPhase == TrainingBlockPhase.MAIN && current.id == "aroundclock_doubles") {
                        AroundClockGameScreen(
                            target = AroundClockTarget.DOUBLE,
                            order = AroundClockOrder.ORDERED,
                            hitsRequired = 1,
                            repository = aroundClockRepository,
                            onFinish = { totalDarts, _, _ ->
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.DOUBLES,
                                            exerciseId = current.id,
                                            score = totalDarts,
                                            dartsThrown = totalDarts,
                                            timestamp = System.currentTimeMillis(),
                                            detailsJson = ""
                                        )
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
                    } else {
                        TrainingExercisePlaceholder(
                            title = current.title,
                            description = current.whyImportant,
                            onDone = {
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
                                resetTrainingState(); autoOpenTimeDialog = false; screen = "main"
                            } else {
                                goToNextPhase()
                            }
                        },
                        onPlayExercise = { id ->
                            blockExerciseId = id
                            screen = "training_block_exercise"
                        },
                        onPlayGame = { id ->
                            extraGameId = id
                            screen = "training_extra_game"
                        },
                        onFinishTraining = {
                            resetTrainingState()
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
                                blockExerciseId = null
                                screen = "training_summary"
                            },
                            onBack = {
                                blockExerciseId = null
                                screen = "training_summary"
                            }
                        )
                    }
                }

                "training_extra_game" -> {
                    val gameId = extraGameId
                    if (gameId == null) screen = "training_summary"
                    else when (gameId) {
                        "aroundclock_doubles" -> AroundClockGameScreen(
                            target = AroundClockTarget.DOUBLE,
                            order = AroundClockOrder.ORDERED,
                            hitsRequired = 1,
                            repository = aroundClockRepository,
                            onFinish = { totalDarts, _, _ ->
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.DOUBLES,
                                            exerciseId = "extra_" + gameId,
                                            score = totalDarts,
                                            dartsThrown = totalDarts,
                                            timestamp = System.currentTimeMillis(),
                                            detailsJson = ""
                                        )
                                    )
                                }
                                extraGameId = null
                                screen = "training_summary"
                            },
                            onBack = { extraGameId = null; screen = "training_summary" }
                        )
                        "aroundclock_sectors" -> AroundClockGameScreen(
                            target = AroundClockTarget.SINGLE,
                            order = AroundClockOrder.ORDERED,
                            hitsRequired = 1,
                            repository = aroundClockRepository,
                            onFinish = { totalDarts, _, _ ->
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.SECTOR,
                                            exerciseId = "extra_" + gameId,
                                            score = totalDarts,
                                            dartsThrown = totalDarts,
                                            timestamp = System.currentTimeMillis(),
                                            detailsJson = ""
                                        )
                                    )
                                }
                                extraGameId = null; screen = "training_summary"
                            },
                            onBack = { extraGameId = null; screen = "training_summary" }
                        )
                        "aroundclock_trebles" -> AroundClockGameScreen(
                            target = AroundClockTarget.TRIPLE,
                            order = AroundClockOrder.ORDERED,
                            hitsRequired = 1,
                            repository = aroundClockRepository,
                            onFinish = { totalDarts, _, _ ->
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.TREBLES,
                                            exerciseId = "extra_" + gameId,
                                            score = totalDarts,
                                            dartsThrown = totalDarts,
                                            timestamp = System.currentTimeMillis(),
                                            detailsJson = ""
                                        )
                                    )
                                }
                                extraGameId = null; screen = "training_summary"
                            },
                            onBack = { extraGameId = null; screen = "training_summary" }
                        )
                        "sector20" -> SectorGameScreen(
                            sector = 20,
                            repository = sectorRepository,
                            onFinish = { score ->
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.SCORE,
                                            exerciseId = "extra_sector20",
                                            score = score,
                                            dartsThrown = 30,
                                            timestamp = System.currentTimeMillis(),
                                            detailsJson = ""
                                        )
                                    )
                                }
                                extraGameId = null
                                screen = "training_summary"
                            },
                            onBack = { extraGameId = null; screen = "training_summary" }
                        )
                        "scoreset" -> ScoreSetGameScreen(
                            repository = scoreSetRepository,
                            onFinish = { totalScore, _ ->
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.SCORE,
                                            exerciseId = "extra_scoreset",
                                            score = totalScore,
                                            dartsThrown = 30,
                                            timestamp = System.currentTimeMillis(),
                                            detailsJson = ""
                                        )
                                    )
                                }
                                extraGameId = null
                                screen = "training_summary"
                            },
                            onBack = { extraGameId = null; screen = "training_summary" }
                        )
                        "biground" -> BigRoundGameScreen(
                            category = BigRoundCategory.MALE,
                            repository = bigRoundRepository,
                            onFinish = { score ->
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = BlockType.SECTOR,
                                            exerciseId = "extra_biground",
                                            score = score,
                                            dartsThrown = 63,
                                            timestamp = System.currentTimeMillis(),
                                            detailsJson = ""
                                        )
                                    )
                                }
                                extraGameId = null
                                screen = "training_summary"
                            },
                            onBack = { extraGameId = null; screen = "training_summary" }
                        )
                        else -> TrainingExercisePlaceholder(
                            title = "Игра",
                            description = "",
                            onDone = { extraGameId = null; screen = "training_summary" },
                            onBack = { extraGameId = null; screen = "training_summary" }
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
                        cricketExistingId = 0L; screen = "cricket_game"
                    },
                    onBack = { screen = "game_select" }
                )

                "cricket_game" -> {
                    val g = cricketGame
                    if (g != null) CricketGameScreen(
                        repository = cricketRepository, initialGame = g, existingId = cricketExistingId,
                        onGameFinish = { f, id ->
                            scope.launch { cricketRepository.saveGame(f, existingId = id) }
                            cricketGame = f; cricketExistingId = 0L; screen = "cricket_stats"
                        },
                        onBack = { cricketGame = null; cricketExistingId = 0L; screen = "game_select" }
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
                        game501ExistingId = 0L; screen = "game501_game"
                    },
                    onBack = { screen = "game_select" }
                )

                "game501_game" -> {
                    val g = game501
                    if (g != null) Game501Screen(
                        repository = game501Repository, initialGame = g, existingId = game501ExistingId,
                        onGameFinish = { f, id ->
                            scope.launch { game501Repository.saveGame(f, existingId = id) }
                            game501 = f; game501ExistingId = 0L; screen = "game501_stats"
                        },
                        onBack = { game501 = null; game501ExistingId = 0L; screen = "game_select" }
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
                showTimeUpDialog = false; resetTrainingState(); autoOpenTimeDialog = false; screen = "training"
            }) { Text("Закончить", color = ErrorColor, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = {
                showTimeUpDialog = false; trainingMinutes += 20
            }) { Text("Продолжить +20 мин", color = GoldAccent, fontWeight = FontWeight.Bold) } }
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
