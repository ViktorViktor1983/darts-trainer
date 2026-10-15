package com.lodkin.dartstrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.lodkin.dartstrainer.ui.training.BullDrawDialog
import com.lodkin.dartstrainer.ui.training.ExerciseSummary
import com.lodkin.dartstrainer.ui.training.ExitTrainingDialog
import com.lodkin.dartstrainer.ui.training.FreeRouletteScreen
import com.lodkin.dartstrainer.ui.training.FreeTrainingSummaryScreen
import com.lodkin.dartstrainer.ui.training.MainExerciseRunner
import com.lodkin.dartstrainer.ui.training.PlannedExercise
import com.lodkin.dartstrainer.ui.training.RestTimerOverlay
import com.lodkin.dartstrainer.ui.training.ResumeTrainingDialog
import com.lodkin.dartstrainer.ui.training.SummaryBlockType
import com.lodkin.dartstrainer.ui.training.SummaryStatus
import com.lodkin.dartstrainer.ui.training.TimeUpDialog
import com.lodkin.dartstrainer.ui.training.TrainingBlockScreen
import com.lodkin.dartstrainer.ui.training.TrainingExercisePlaceholder
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
import com.lodkin.dartstrainer.ui.training.recommendedGameIds
import com.lodkin.dartstrainer.ui.training.warmupBlockItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

/**
 * Авто-OK в тренировочных партиях 501 и Крикета.
 * Всегда включён, чтобы игрок не тратил время на подтверждение каждого подхода.
 */
private const val TRAINING_AUTO_OK_SECONDS = 3

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
    "training_free_next" to "main",
    "training_free_summary" to "main",
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
    "training_extra_game",
    "training_free_next"
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
    var showExitTrainingDialog by remember { mutableStateOf(false) }

    var isFreeTraining by remember { mutableStateOf(false) }
    var freeNoRepeat by remember { mutableStateOf(false) }
    var freeCompletedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var freeSkippedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var freeCurrentId by remember { mutableStateOf<String?>(null) }
    var freeRouletteKey by remember { mutableStateOf(0) }
    var freeExercisesCompleted by remember { mutableStateOf(0) }
    var showFreeNewRoundDialog by remember { mutableStateOf(false) }

    var showRestTimer by remember { mutableStateOf(false) }
    var restTimerSeconds by remember { mutableStateOf(0) }
    var restNextTitle by remember { mutableStateOf<String?>(null) }
    var pendingNextStep by remember { mutableStateOf<(() -> Unit)?>(null) }

    // ── Розыгрыш Bull перед тренировочной партией ──
    var showBullDraw by remember { mutableStateOf(false) }
    var bullDrawExerciseId by remember { mutableStateOf<String?>(null) }

    fun showRestThen(restSeconds: Int, nextTitle: String?, nextStep: () -> Unit) {
        restTimerSeconds = restSeconds
        restNextTitle = nextTitle
        pendingNextStep = nextStep
        showRestTimer = true
    }

    fun pickNextFreeExercise() {
        val all = mainExercises.map { it.id }
        if (all.isEmpty()) {
            freeCurrentId = null
            return
        }
        if (!freeNoRepeat) {
            freeCurrentId = all.random()
            freeRouletteKey += 1
            return
        }
        val candidates = all.filterNot { it in freeCompletedIds || it in freeSkippedIds }
        if (candidates.isEmpty()) {
            freeCurrentId = null
            showFreeNewRoundDialog = true
        } else {
            freeCurrentId = candidates.random()
            freeRouletteKey += 1
        }
    }

    /**
     * Запуск тренировочной партии 501 или Крикета с указанным
     * победителем розыгрыша (startingTeamIndex).
     */
    fun startTrainingGame(exerciseId: String, startingTeamIndex: Int) {
        exerciseStartMillis = System.currentTimeMillis()
        if (exerciseId == "game501") {
            val playerName = SettingsStorage.getPlayerName(context).ifBlank { "Игрок" }
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
                startingTeamIndex = startingTeamIndex,
                autoOkSeconds = TRAINING_AUTO_OK_SECONDS,
                sessionStartTime = sessionStartTime,
                sessionForm = sessionForm
            )
            game501ExistingId = 0L
            isTraining501 = true
            screen = "game501_game"
        } else if (exerciseId == "cricket") {
            val playerName = SettingsStorage.getPlayerName(context).ifBlank { "Игрок" }
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
                startingTeamIndex = startingTeamIndex,
                autoOkSeconds = TRAINING_AUTO_OK_SECONDS,
                sessionStartTime = sessionStartTime,
                sessionForm = sessionForm
            )
            cricketExistingId = 0L
            isTrainingCricket = true
            screen = "cricket_game"
        }
    }

    fun launchExercise(id: String) {
        exerciseStartMillis = System.currentTimeMillis()
        when (id) {
            "game501", "cricket" -> {
                // Перед партией — розыгрыш Bull.
                bullDrawExerciseId = id
                showBullDraw = true
            }
            else -> screen = "training_exercise"
        }
    }

    fun saveCurrentTrainingState() {
        if (trainingSessionId <= 0L) return
        if (isFreeTraining) return
        if (currentPhase == TrainingBlockPhase.FINISHED) return
        TrainingStateStorage.save(
            context = context,
            sessionId = trainingSessionId,
            minutes = trainingMinutes,
            elapsedSeconds = trainingElapsedSeconds,
            phaseName = currentPhase.name,
            exerciseIndex = currentExerciseIndex,
            statuses = exerciseStatuses.mapValues { it.value.name },
            mainExerciseIds = mainExercises.map { it.id to it.minutes },
            psychologyEnabled = psychologyExercises.isNotEmpty(),
            strengthEnabled = strengthExercises.isNotEmpty()
        )
    }

    LaunchedEffect(isTrainingActive, trainingPaused, screen) {
        if (!isTrainingActive || trainingPaused || screen !in TRAINING_SCREENS) return@LaunchedEffect
        while (true) {
            delay(1000L)
            trainingElapsedSeconds += 1
            if (!isFreeTraining && trainingElapsedSeconds >= trainingMinutes * 60) {
                showTimeUpDialog = true
                break
            }
        }
    }
    val trainingSecondsLeft = if (isFreeTraining) trainingElapsedSeconds
        else (trainingMinutes * 60 - trainingElapsedSeconds).coerceAtLeast(0)

    LaunchedEffect(
        trainingSessionId, currentPhase, currentExerciseIndex, exerciseStatuses, mainExercises
    ) {
        if (isTrainingActive && trainingSessionId > 0L && !isFreeTraining) {
            saveCurrentTrainingState()
        }
    }

    LaunchedEffect(isTrainingActive, screen) {
        if (!isTrainingActive || screen !in TRAINING_SCREENS) return@LaunchedEffect
        while (true) {
            delay(30_000L)
            if (!isFreeTraining) saveCurrentTrainingState()
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
        showRestTimer = false
        restNextTitle = null
        pendingNextStep = null
        showExitTrainingDialog = false
        isFreeTraining = false
        freeNoRepeat = false
        freeCompletedIds = emptySet()
        freeSkippedIds = emptySet()
        freeCurrentId = null
        freeRouletteKey = 0
        freeExercisesCompleted = 0
        showFreeNewRoundDialog = false
        showBullDraw = false
        bullDrawExerciseId = null
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

    fun advanceAfterExercise(finishedExerciseId: String) {
        if (isFreeTraining) {
            freeExercisesCompleted += 1
            freeCompletedIds = freeCompletedIds + finishedExerciseId
            freeCurrentId = null
            val restSec = TrainingPlanner.restSecondsAfter(finishedExerciseId)
            showRestThen(restSec, null) {
                screen = "training_free_next"
            }
        } else {
            val list = exercisesForPhase(TrainingBlockPhase.MAIN)
            if (currentExerciseIndex + 1 < list.size) {
                val nextExercise = list[currentExerciseIndex + 1]
                val restSec = TrainingPlanner.restSecondsAfter(finishedExerciseId)
                showRestThen(restSec, nextExercise.title) {
                    currentExerciseIndex += 1
                    screen = "training_block"
                }
            } else {
                screen = "training_summary"
            }
        }
    }

    fun finishCurrentMainExercise() {
        val finishedId = if (isFreeTraining) {
            freeCurrentId ?: ""
        } else {
            mainExercises.getOrNull(currentExerciseIndex)?.id ?: ""
        }
        if (!isFreeTraining) {
            exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                put(currentExerciseIndex, SummaryStatus.DONE)
            }
        }
        advanceAfterExercise(finishedId)
    }

    BackHandler(
        enabled = stage == "main" && screen != "main" && !showRestTimer && !showExitTrainingDialog && !showBullDraw
    ) {
        val isTrainingScreen = screen in TRAINING_SCREENS || screen == "training"

        when {
            isFreeTraining -> {
                if (screen == "training_free_summary") {
                    resetTrainingState()
                    TrainingStateStorage.clear(context)
                    autoOpenTimeDialog = false
                    screen = "main"
                } else {
                    screen = "training_free_summary"
                }
            }
            isTrainingScreen && isTrainingActive && trainingSessionId > 0L && screen != "training_summary" -> {
                showExitTrainingDialog = true
            }
            else -> {
                PARENT_SCREEN[screen]?.let { parent ->
                    if (parent == "stats_menu") statsMenuKey++
                    if (parent == "main") {
                        resetTrainingState()
                        TrainingStateStorage.clear(context)
                        autoOpenTimeDialog = true
                    }
                    if (screen == "training_summary") return@BackHandler
                    if (screen in TRAINING_SCREENS && parent == "training") {
                        autoOpenTimeDialog = false
                    }
                    screen = parent
                }
            }
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
                            if (TrainingStateStorage.isExpired(snap)) {
                                TrainingStateStorage.clear(context)
                                resetTrainingState()
                                autoOpenTimeDialog = true
                            } else {
                                pendingResume = snap
                                showResumeDialog = true
                                autoOpenTimeDialog = false
                            }
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
                    repository = cricketRepository,
                    freePlayRecordsRepository = freePlayRecordsRepository,
                    initialTab = 0, showTabs = false,
                    onResumeCricket = { entity ->
                        val r = CricketSerializer.fromJson(entity.stateBlob)
                        if (r != null) { cricketGame = r; cricketExistingId = entity.id; screen = "cricket_game" }
                    },
                    onBack = { statsMenuKey++; screen = "stats_menu" }
                )

                "stats_501" -> StatsScreen(
                    repository = cricketRepository,
                    freePlayRecordsRepository = freePlayRecordsRepository,
                    initialTab = 1, showTabs = false,
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

                            val warmupMin = if (TrainingBlocksStorage.isWarmupEnabled(context)) 6 else 0
                            val psychoMin = if (TrainingBlocksStorage.isPsychologyEnabled(context)) 8 else 0
                            val strengthMin = if (TrainingBlocksStorage.isStrengthEnabled(context)) 7 else 0
                            val reserveMin = 5
                            val budgetMain = (trainingMinutes - warmupMin - psychoMin - strengthMin - reserveMin)
                                .coerceAtLeast(10)

                            val dynamicIds = TrainingPlanner.buildDynamicMainExerciseIds(
                                analysis = analysis,
                                budgetMinutes = budgetMain,
                                completedTrainingCount = completedCount
                            )
                            val baseMap = defaultMainExercises().associateBy { it.id }
                            val fromPlanner = dynamicIds
                                .mapNotNull { d -> baseMap[d.id]?.copy(minutes = d.minutes) }

                            mainExercises = if (fromPlanner.isNotEmpty()) {
                                fromPlanner
                            } else {
                                val fallback = defaultMainExercises().first()
                                listOf(fallback.copy(minutes = budgetMain.coerceIn(5, 20)))
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
                            isFreeTraining = false
                            showRestTimer = false
                            pendingNextStep = null

                            TrainingStateStorage.save(
                                context = context,
                                sessionId = trainingSessionId,
                                minutes = trainingMinutes,
                                elapsedSeconds = 0,
                                phaseName = first.name,
                                exerciseIndex = 0,
                                statuses = emptyMap(),
                                mainExerciseIds = mainExercises.map { it.id to it.minutes },
                                psychologyEnabled = psychologyExercises.isNotEmpty(),
                                strengthEnabled = strengthExercises.isNotEmpty()
                            )

                            screen = if (first == TrainingBlockPhase.WARMUP) "training_warmup" else "training_block"
                        }
                    },
                    onStartFreeTraining = {
                        autoOpenTimeDialog = false
                        scope.launch {
                            val analysis = TrainingStatsAnalyzer.analyze(trainingRepository)
                            val completedCount = trainingRepository.getCompletedSessionCount()
                            currentWeakness = analysis.weakestType
                            currentLevel = analysis.level

                            val session = TrainingSession(
                                dateMillis = System.currentTimeMillis(),
                                plannedMinutes = 0,
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
                            trainingMinutes = 0

                            val dynamicIds = TrainingPlanner.buildDynamicMainExerciseIds(
                                analysis = analysis,
                                budgetMinutes = 999,
                                completedTrainingCount = completedCount,
                                ignoreHardLimit = true
                            )
                            val baseMap = defaultMainExercises().associateBy { it.id }
                            mainExercises = dynamicIds
                                .mapNotNull { d -> baseMap[d.id]?.copy(minutes = d.minutes) }
                                .ifEmpty { defaultMainExercises() }

                            psychologyExercises = emptyList()
                            strengthExercises = emptyList()

                            currentPhase = TrainingBlockPhase.MAIN
                            currentExerciseIndex = 0
                            exerciseStatuses = emptyMap()
                            blockExerciseId = null
                            extraGameId = null
                            exerciseStartMillis = 0L
                            isTraining501 = false
                            isTrainingCricket = false
                            isTrainingActive = true
                            isFreeTraining = true
                            freeNoRepeat = false
                            freeCompletedIds = emptySet()
                            freeSkippedIds = emptySet()
                            freeCurrentId = null
                            freeRouletteKey = 0
                            freeExercisesCompleted = 0
                            showFreeNewRoundDialog = false
                            showRestTimer = false
                            pendingNextStep = null

                            screen = "training_free_next"
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
                        if (isTrainingActive && trainingSessionId > 0L && !isFreeTraining) {
                            showExitTrainingDialog = true
                        } else {
                            autoOpenTimeDialog = true
                            screen = "main"
                        }
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
                            val current = list.getOrNull(currentExerciseIndex)
                            if (current != null) launchExercise(current.id)
                        },
                        onSkipCurrent = {
                            exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                                put(currentExerciseIndex, SummaryStatus.SKIPPED)
                            }
                            if (currentExerciseIndex + 1 < list.size) currentExerciseIndex += 1
                            else screen = "training_summary"
                        },
                        onBack = { showExitTrainingDialog = true }
                    )
                }

                "training_free_next" -> {
                    val current = mainExercises.firstOrNull { it.id == freeCurrentId }

                    if (current == null) {
                        LaunchedEffect(freeCurrentId) {
                            if (freeCurrentId == null && !showFreeNewRoundDialog) {
                                pickNextFreeExercise()
                            }
                        }
                        Box(modifier = Modifier.fillMaxSize())
                    } else {
                        key(freeRouletteKey) {
                            FreeRouletteScreen(
                                finalExercise = current,
                                allExercises = mainExercises,
                                completedCount = freeExercisesCompleted,
                                noRepeat = freeNoRepeat,
                                onToggleNoRepeat = { newValue ->
                                    freeNoRepeat = newValue
                                    if (newValue) {
                                        freeSkippedIds = emptySet()
                                    } else {
                                        freeSkippedIds = emptySet()
                                        freeCompletedIds = emptySet()
                                    }
                                },
                                onPlay = { launchExercise(current.id) },
                                onSkip = {
                                    freeSkippedIds = freeSkippedIds + current.id
                                    freeCurrentId = null
                                },
                                onFinish = { screen = "training_free_summary" }
                            )
                        }
                    }
                }

                "training_free_summary" -> {
                    FreeTrainingSummaryScreen(
                        playerName = SettingsStorage.getPlayerName(context),
                        sessionId = trainingSessionId,
                        actualSeconds = trainingElapsedSeconds,
                        repository = trainingRepository,
                        onFinishTraining = {
                            resetTrainingState()
                            TrainingStateStorage.clear(context)
                            autoOpenTimeDialog = false
                            screen = "main"
                        }
                    )
                }

                "training_exercise" -> {
                    val list = exercisesForPhase(currentPhase)
                    val current = if (isFreeTraining) {
                        mainExercises.firstOrNull { it.id == freeCurrentId }
                    } else {
                        list.getOrNull(currentExerciseIndex)
                    }
                    if (current == null) {
                        screen = if (isFreeTraining) "training_free_next" else "training_block"
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
                            onBack = {
                                screen = if (isFreeTraining) "training_free_next" else "training_block"
                            }
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
                                    val lowerDartsBetter = current.id.startsWith("aroundclock_")
                                    trainingRepository.updateTrainingRecordIfBetter(
                                        exerciseId = current.id,
                                        score = score,
                                        darts = darts,
                                        lowerDartsBetter = lowerDartsBetter
                                    )
                                }
                                if (!isFreeTraining) {
                                    exerciseStatuses = exerciseStatuses.toMutableMap().apply {
                                        put(currentExerciseIndex, SummaryStatus.DONE)
                                    }
                                }
                                advanceAfterExercise(current.id)
                            },
                            onBack = {
                                screen = if (isFreeTraining) "training_free_next" else "training_block"
                            }
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
                        onPlayExercise = { id -> launchExercise(id) },
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
                        repository = cricketRepository,
                        freePlayRecordsRepository = freePlayRecordsRepository,
                        initialGame = g,
                        existingId = cricketExistingId,
                        isTrainingMode = isTrainingCricket,
                        trainingRepository = trainingRepository,
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
                                val mprStr = String.format(Locale.US, "%.1f", mpr)
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
                                            detailsJson = "won=$playerWon;mpr=$mprStr"
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
                            val wasTraining = isTrainingCricket
                            isTrainingCricket = false
                            cricketGame = null; cricketExistingId = 0L
                            screen = if (wasTraining) {
                                if (isFreeTraining) "training_free_next" else "training_block"
                            } else "game_select"
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
                                val dbl = player?.let { p -> Game501Logic.doublesAccuracy(p) } ?: 0.0
                                val pprStr = String.format(Locale.US, "%.1f", ppr)
                                val dblStr = dbl.toInt().toString()
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
                                            detailsJson = "won=$playerWon;ppr=$pprStr;dbl=$dblStr"
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
                            val wasTraining = isTraining501
                            isTraining501 = false
                            game501 = null; game501ExistingId = 0L
                            screen = if (wasTraining) {
                                if (isFreeTraining) "training_free_next" else "training_block"
                            } else "game_select"
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

        if (isTrainingActive && screen in TRAINING_SCREENS && !showRestTimer && screen != "training_free_summary") {
            TrainingTimerOverlay(
                secondsLeft = if (isFreeTraining) trainingElapsedSeconds else trainingSecondsLeft,
                isPaused = trainingPaused,
                onTogglePause = { trainingPaused = !trainingPaused }
            )
        }

        if (showRestTimer) {
            RestTimerOverlay(
                totalSeconds = restTimerSeconds,
                nextExerciseTitle = restNextTitle,
                onSkip = {
                    showRestTimer = false
                    val step = pendingNextStep
                    pendingNextStep = null
                    restNextTitle = null
                    step?.invoke()
                }
            )
        }
    }

    // ── Диалог розыгрыша Bull перед тренировочной партией ──
    if (showBullDraw && bullDrawExerciseId != null) {
        val id = bullDrawExerciseId!!
        val playerName = SettingsStorage.getPlayerName(context).ifBlank { "Игрок" }
        val botLevel = if (id == "game501") {
            TrainingBotLevelStorage.getLevel501(context, currentLevel)
        } else {
            TrainingBotLevelStorage.getLevelCricket(context, currentLevel)
        }
        BullDrawDialog(
            playerName = playerName,
            botName = "Бот $botLevel",
            botLevel = botLevel,
            onWinnerChosen = { winner ->
                showBullDraw = false
                startTrainingGame(id, winner)
                bullDrawExerciseId = null
            },
            onCancel = {
                showBullDraw = false
                bullDrawExerciseId = null
                // Отмену розыгрыша трактуем как «просто начать без розыгрыша».
                startTrainingGame(id, 0)
            }
        )
    }

    if (showExitTrainingDialog) {
        ExitTrainingDialog(
            onFinish = {
                showExitTrainingDialog = false
                resetTrainingState()
                TrainingStateStorage.clear(context)
                autoOpenTimeDialog = false
                screen = "main"
            },
            onPostpone = {
                showExitTrainingDialog = false
                saveCurrentTrainingState()
                autoOpenTimeDialog = false
                screen = "main"
            }
        )
    }

    if (showFreeNewRoundDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = TileBgDark,
            title = {
                Text(
                    "Все упражнения пройдены",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Ты прошёл все 8 упражнений. Что делаем дальше?",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showFreeNewRoundDialog = false
                    freeCompletedIds = emptySet()
                    freeSkippedIds = emptySet()
                    pickNextFreeExercise()
                }) {
                    Text("Новый круг", color = Accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFreeNewRoundDialog = false
                    screen = "training_free_summary"
                }) {
                    Text("Закончить тренировку", color = Color.White.copy(alpha = 0.75f))
                }
            }
        )
    }

    if (showResumeDialog && pendingResume != null) {
        val snap = pendingResume!!
        val minutesAgo = TrainingStateStorage.minutesSinceLastUpdate(context)
        ResumeTrainingDialog(
            minutesAgo = minutesAgo,
            onContinue = {
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

                mainExercises = if (snap.mainExerciseIds.isNotEmpty()) {
                    val baseMap = defaultMainExercises().associateBy { it.id }
                    snap.mainExerciseIds
                        .mapNotNull { (id, m) -> baseMap[id]?.copy(minutes = m) }
                        .ifEmpty { defaultMainExercises() }
                } else {
                    defaultMainExercises()
                }
                psychologyExercises = if (snap.psychologyEnabled)
                    defaultPsychologyExercises() else emptyList()
                strengthExercises = if (snap.strengthEnabled)
                    defaultStrengthExercises() else emptyList()

                trainingPaused = false
                isTrainingActive = true
                isFreeTraining = false
                autoOpenTimeDialog = false
                showResumeDialog = false
                exerciseStartMillis = 0L
                isTraining501 = false
                isTrainingCricket = false
                showRestTimer = false
                pendingNextStep = null

                screen = when (currentPhase) {
                    TrainingBlockPhase.WARMUP -> "training_warmup"
                    else -> "training_block"
                }
                pendingResume = null
            },
            onRestart = {
                TrainingStateStorage.clear(context)
                resetTrainingState()
                autoOpenTimeDialog = true
                showResumeDialog = false
                pendingResume = null
            }
        )
    }

    if (showTimeUpDialog) {
        TimeUpDialog(
            trainingMinutes = trainingMinutes,
            onFinish = {
                showTimeUpDialog = false
                resetTrainingState()
                TrainingStateStorage.clear(context)
                autoOpenTimeDialog = false
                screen = "training"
            },
            onContinuePlus20 = {
                showTimeUpDialog = false
                trainingMinutes += 20
            }
        )
    }
}
