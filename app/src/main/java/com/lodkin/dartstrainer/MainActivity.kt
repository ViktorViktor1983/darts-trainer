package com.lodkin.dartstrainer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.data.training.ExerciseResult
import com.lodkin.dartstrainer.data.training.InventoryStorage
import com.lodkin.dartstrainer.data.training.LevelCalculator // ДОБАВЛЕН ИМПОРТ
import com.lodkin.dartstrainer.data.training.NormativesStateStorage
import com.lodkin.dartstrainer.data.training.ProblemSectorsStorage
import com.lodkin.dartstrainer.data.training.TrainingBlocksStorage
import com.lodkin.dartstrainer.data.training.TrainingBotLevelStorage
import com.lodkin.dartstrainer.data.training.TrainingDatabase
import com.lodkin.dartstrainer.data.training.TrainingMode
import com.lodkin.dartstrainer.data.training.TrainingPlanner
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingSession
import com.lodkin.dartstrainer.data.training.TrainingStateStorage
import com.lodkin.dartstrainer.data.training.TrainingStatsAnalyzer
import com.lodkin.dartstrainer.data.training.TrainingUnfinishedStorage
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark // ДОБАВЛЕН ИМПОРТ
import com.lodkin.dartstrainer.ui.*
import com.lodkin.dartstrainer.ui.about.AboutScreen
import com.lodkin.dartstrainer.ui.online.OnlineLobbyScreen
import com.lodkin.dartstrainer.ui.training.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

private const val TRAINING_AUTO_OK_SECONDS = 3

private val PARENT_SCREEN: Map<String, String> = mapOf(
    "stats_menu" to "main", "stats_cricket" to "stats_menu", "stats_501" to "stats_menu",
    "settings" to "main", "about" to "settings",
    "training" to "main", "training_settings" to "training", "training_progress" to "training",
    "detailed_stats" to "main", "discipline_stats" to "detailed_stats",
    "training_warmup" to "training", "training_block" to "training",
    "training_exercise" to "training_block", "training_summary" to "training",
    "training_block_exercise" to "training_summary", "training_extra_game" to "training_summary",
    "training_free_next" to "main", "training_free_summary" to "main",
    "normatives" to "training", "game_select" to "main",
    "cricket_setup" to "game_select", "cricket_game" to "game_select", "cricket_stats" to "main",
    "game501_setup" to "game_select", "game501_game" to "game_select", "game501_stats" to "main",
    "sector_setup" to "game_select", "sector_game" to "game_select", "sector_stats" to "stats_menu",
    "aroundclock_setup" to "game_select", "aroundclock_game" to "game_select", "aroundclock_stats" to "stats_menu",
    "biground_setup" to "game_select", "biground_game" to "game_select", "biground_stats" to "stats_menu",
    "scoreset_setup" to "game_select", "scoreset_game" to "game_select", "scoreset_stats" to "stats_menu",
    "online_lobby" to "main"
)

private val TRAINING_SCREENS: Set<String> = setOf(
    "training_warmup", "training_block", "training_exercise",
    "training_block_exercise", "training_extra_game", "training_free_next"
)

private enum class TrainingBlockPhase {
    WARMUP, MAIN, PSYCHOLOGY, STRENGTH, FINISHED
}

data class OnlineGameParams(
    val is501: Boolean,
    val level: Int,
    val legs: Int,
    val sets: Int,
    val nickname: String,
    val saveToTraining: Boolean
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

    // ── РЕПОЗИТОРИИ ──
    val cricketRepository = remember { CricketRepository(CricketDatabase.get(context).cricketDao()) }
    val game501Repository = remember { Game501Repository(Game501Database.get(context).game501Dao()) }
    val sectorRepository = remember { SectorRepository(SectorDatabase.get(context).sectorDao()) }
    val aroundClockRepository = remember { AroundClockRepository(AroundClockDatabase.get(context).aroundClockDao()) }
    val bigRoundRepository = remember { BigRoundRepository(BigRoundDatabase.get(context).bigRoundDao()) }
    val scoreSetRepository = remember { ScoreSetRepository(ScoreSetDatabase.get(context).scoreSetDao()) }
    val trainingRepository = remember { TrainingRepository(TrainingDatabase.getInstance(context), context) }
    val freePlayRecordsRepository = remember { FreePlayRecordsRepository(FreePlayRecordsDatabase.get(context)) }

    // ── БАЗОВЫЕ СТЕЙТЫ ──
    val sessionStartTime = remember { System.currentTimeMillis() }
    val sessionForm = remember { 0.85 + Random.nextDouble() * 0.30 }
    var onboardingDone by remember { mutableStateOf(SettingsStorage.isOnboardingDone(context)) }
    var stage by remember { mutableStateOf(if (onboardingDone) "loading" else "welcome") }
    var screen by remember { mutableStateOf("main") }
    
    var selectedDiscipline by remember { mutableStateOf<String?>(null) }
    var statsMenuKey by remember { mutableStateOf(0) }

    // ── СТЕЙТЫ ИГР ──
    var cricketGame by remember { mutableStateOf<CricketGame?>(null) }
    var cricketExistingId by remember { mutableStateOf(0L) }
    var game501 by remember { mutableStateOf<Game501?>(null) }
    var game501ExistingId by remember { mutableStateOf(0L) }
    var sectorToPlay by remember { mutableStateOf<Int?>(null) }
    var aroundClockTarget by remember { mutableStateOf<AroundClockTarget?>(null) }
    var aroundClockOrder by remember { mutableStateOf<AroundClockOrder?>(null) }
    var aroundClockHits by remember { mutableStateOf(1) }
    var bigRoundCategory by remember { mutableStateOf<BigRoundCategory?>(null) }

    // ── СТЕЙТЫ ТРЕНИРОВКИ ──
    var trainingMinutes by remember {
        mutableStateOf(context.getSharedPreferences("training_prefs", Context.MODE_PRIVATE).getInt("session_minutes", SettingsStorage.getTrainingMinutes(context)))
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
    var currentTargetGoals by remember { mutableStateOf<List<String>>(emptyList()) }
    var exerciseStartMillis by remember { mutableStateOf(0L) }

    var isTraining501 by remember { mutableStateOf(false) }
    var isTrainingCricket by remember { mutableStateOf(false) }
    var isOnlineGame by remember { mutableStateOf(false) }
    var isResumingFromStats by remember { mutableStateOf(false) } 

    // ── СТЕЙТЫ ОНЛАЙНА ──
    var pendingOnlineGameParams by remember { mutableStateOf<OnlineGameParams?>(null) }

    // ── СТЕЙТЫ СВОБОДНОЙ ТРЕНИРОВКИ ──
    val prefs = context.getSharedPreferences("free_training_prefs", Context.MODE_PRIVATE)
    var isFreeTraining by remember { mutableStateOf(false) }
    var isSingleExercisePlay by remember { mutableStateOf(false) }
    var isSetupForTraining by remember { mutableStateOf(false) }
    
    var freeNoRepeat by remember { mutableStateOf(prefs.getBoolean("free_no_repeat", false)) }
    var freeChooseMyself by remember { mutableStateOf(prefs.getBoolean("free_choose_myself", false)) }
    
    var freeCompletedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var freeSkippedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var freeCurrentId by remember { mutableStateOf<String?>(null) }
    var freeRouletteKey by remember { mutableStateOf(0) }
    var freeExercisesCompleted by remember { mutableStateOf(0) }
    var showFreeNewRoundDialog by remember { mutableStateOf(false) }

    fun saveFreeNoRepeat(value: Boolean) { prefs.edit().putBoolean("free_no_repeat", value).apply(); freeNoRepeat = value }
    fun saveFreeChooseMyself(value: Boolean) { prefs.edit().putBoolean("free_choose_myself", value).apply(); freeChooseMyself = value }

    // ── СТЕЙТЫ ОТДЫХА ──
    var showRestTimer by remember { mutableStateOf(false) }
    var restTimerSeconds by remember { mutableStateOf(0) }
    var restNextTitle by remember { mutableStateOf<String?>(null) }
    var pendingNextStep by remember { mutableStateOf<(() -> Unit)?>(null) }

    // ── СТЕЙТЫ ДИАЛОГОВ ──
    var showResumeDialog by remember { mutableStateOf(false) }
    var pendingResume by remember { mutableStateOf<TrainingStateStorage.Snapshot?>(null) }
    var showExitTrainingDialog by remember { mutableStateOf(false) }
    var showBullDraw by remember { mutableStateOf(false) }
    var bullDrawExerciseId by remember { mutableStateOf<String?>(null) }
    var examResultMessage by remember { mutableStateOf<String?>(null) }

    // ── СТЕЙТЫ НОРМАТИВОВ ──
    var normativesAvailable by remember { mutableStateOf(false) }
    var normativesActive by remember { mutableStateOf(false) }
    var normativesCompletedSteps by remember { mutableStateOf<Set<String>>(emptySet()) }
    var normatives501Levels by remember { mutableStateOf<List<Int>>(emptyList()) }
    var normatives501PlayerWins by remember { mutableStateOf(0) }
    var normatives501BotWins by remember { mutableStateOf(0) }
    var normatives501CurrentLeg by remember { mutableStateOf(0) }
    var normativesCricketLevels by remember { mutableStateOf<List<Int>>(emptyList()) }
    var normativesCricketPlayerWins by remember { mutableStateOf(0) }
    var normativesCricketBotWins by remember { mutableStateOf(0) }
    var normativesCricketCurrentLeg by remember { mutableStateOf(0) }
    var normativesStartLevel by remember { mutableStateOf(1) }
    var normativesNewLevel by remember { mutableStateOf<Int?>(null) }
    var showResumeNormativesDialog by remember { mutableStateOf(false) }
    var pendingNormativesResume by remember { mutableStateOf<NormativesStateStorage.Snapshot?>(null) }
    var isNormativesExercise by remember { mutableStateOf(false) }

    // Сброс флагов при смене режима
    LaunchedEffect(screen) {
        if (screen == "online_lobby") {
            isOnlineGame = false
            isTraining501 = false
            isTrainingCricket = false
            isResumingFromStats = false
        }
        if (screen == "main" || screen == "game_select") {
            isTraining501 = false
            isTrainingCricket = false
            isResumingFromStats = false
            isOnlineGame = false
        }
        if (screen == "training") {
            isTraining501 = false
            isTrainingCricket = false
            isResumingFromStats = false
            isOnlineGame = false
        }
    }

    // ────────────────────────────────────────────────────────
    // ЛОГИКА (ФУНКЦИИ)
    // ────────────────────────────────────────────────────────

    fun showRestThen(restSeconds: Int, nextTitle: String?, nextStep: () -> Unit) {
        restTimerSeconds = restSeconds; restNextTitle = nextTitle; pendingNextStep = nextStep; showRestTimer = true
    }

    fun pickNextFreeExercise() {
        val all = mainExercises.map { it.id }
        if (all.isEmpty()) { freeCurrentId = null; return }
        if (!freeNoRepeat) { freeCurrentId = all.random(); freeRouletteKey += 1; return }
        val candidates = all.filterNot { it in freeCompletedIds || it in freeSkippedIds }
        if (candidates.isEmpty()) { freeCurrentId = null; showFreeNewRoundDialog = true }
        else { freeCurrentId = candidates.random(); freeRouletteKey += 1 }
    }

    fun botNameFor(level: Int): String = DartsNorms.getLevelName(level.coerceIn(1, 16))

    fun startTrainingGame(exerciseId: String, startingTeamIndex: Int) {
        exerciseStartMillis = System.currentTimeMillis()
        if (exerciseId == "game501") {
            val playerName = SettingsStorage.getPlayerName(context).ifBlank { "Игрок" }
            val botLevel = TrainingBotLevelStorage.getLevel501(context, currentLevel)
            val players = listOf(
                Player501(name = playerName, isBot = false, botLevel = 0, teamIndex = 0),
                Player501(name = botNameFor(botLevel), isBot = true, botLevel = botLevel, teamIndex = 1)
            )
            game501 = Game501Logic.newGame(GameType.X501, OutMode.DOUBLE_OUT, players, 1, 1, false, startingTeamIndex, TRAINING_AUTO_OK_SECONDS, sessionStartTime, sessionForm)
            game501ExistingId = 0L; isTraining501 = true; screen = "game501_game"
        } else if (exerciseId == "cricket") {
            val playerName = SettingsStorage.getPlayerName(context).ifBlank { "Игрок" }
            val botLevel = TrainingBotLevelStorage.getLevelCricket(context, currentLevel)
            val players = listOf(
                CricketPlayer(name = playerName, isBot = false, botLevel = 0, teamIndex = 0),
                CricketPlayer(name = botNameFor(botLevel), isBot = true, botLevel = botLevel, teamIndex = 1)
            )
            cricketGame = CricketLogic.newGame(CricketType.AMERICAN, players, 1, 1, false, startingTeamIndex, TRAINING_AUTO_OK_SECONDS, sessionStartTime, sessionForm)
            cricketExistingId = 0L; isTrainingCricket = true; screen = "cricket_game"
        }
    }

    fun startOnlineBotGame(params: OnlineGameParams, startingTeam: Int) {
        isOnlineGame = true
        scope.launch {
            if (params.saveToTraining) {
                val session = TrainingSession(
                    dateMillis = System.currentTimeMillis(),
                    plannedMinutes = 0,
                    actualMinutes = 0,
                    levelAtStart = params.level,
                    mode = TrainingMode.FLEXIBLE,
                    focus = null,
                    isControl = false,
                    isCompleted = false
                )
                trainingSessionId = trainingRepository.insertSession(session)
                if (params.is501) isTraining501 = true else isTrainingCricket = true
            } else {
                if (params.is501) isTraining501 = false else isTrainingCricket = false
            }

            if (params.is501) {
                val botName = botNameFor(params.level)
                val players = listOf(
                    Player501(name = params.nickname, isBot = false, botLevel = 0, teamIndex = 0),
                    Player501(name = botName, isBot = true, botLevel = params.level, teamIndex = 1)
                )
                game501 = Game501Logic.newGame(GameType.X501, OutMode.DOUBLE_OUT, players, params.legs, params.sets, false, startingTeam, TRAINING_AUTO_OK_SECONDS, sessionStartTime, sessionForm)
                game501ExistingId = 0L
                screen = "game501_game"
            } else {
                val botName = botNameFor(params.level)
                val players = listOf(
                    CricketPlayer(name = params.nickname, isBot = false, botLevel = 0, teamIndex = 0),
                    CricketPlayer(name = botName, isBot = true, botLevel = params.level, teamIndex = 1)
                )
                cricketGame = CricketLogic.newGame(CricketType.AMERICAN, players, params.legs, params.sets, false, startingTeam, TRAINING_AUTO_OK_SECONDS, sessionStartTime, sessionForm)
                cricketExistingId = 0L
                screen = "cricket_game"
            }
        }
    }

    fun launchExercise(id: String) {
        exerciseStartMillis = System.currentTimeMillis()
        when (id) {
            "game501", "cricket" -> { bullDrawExerciseId = id; showBullDraw = true }
            else -> screen = "training_exercise"
        }
    }

    fun saveCurrentTrainingState() {
        if (trainingSessionId <= 0L || isFreeTraining || currentPhase == TrainingBlockPhase.FINISHED) return
        TrainingStateStorage.save(
            context, trainingSessionId, trainingMinutes, trainingElapsedSeconds,
            currentPhase.name, currentExerciseIndex, exerciseStatuses.mapValues { it.value.name },
            mainExercises.map { it.id to it.minutes }, psychologyExercises.isNotEmpty(), strengthExercises.isNotEmpty()
        )
    }

    fun saveNormativesSnapshot() {
        val g501Results = mutableListOf<Boolean>().apply { repeat(normatives501PlayerWins) { add(true) }; repeat(normatives501BotWins) { add(false) } }
        val crResults = mutableListOf<Boolean>().apply { repeat(normativesCricketPlayerWins) { add(true) }; repeat(normativesCricketBotWins) { add(false) } }
        NormativesStateStorage.save(context, normativesCompletedSteps, normatives501Levels, g501Results, normativesCricketLevels, crResults)
    }

    fun buildBotLadder(startLevel: Int): List<Int> {
        return if (startLevel in 2..15) listOf(startLevel - 1, startLevel, startLevel + 1, startLevel - 1, startLevel).map { it.coerceIn(1, 16) }
        else List(5) { startLevel.coerceIn(1, 16) }
    }

    fun startNewNormatives() {
        scope.launch {
            val startLvl = SettingsStorage.getStartLevel(context)
            normativesStartLevel = startLvl

            val session = TrainingSession(
                dateMillis = System.currentTimeMillis(),
                plannedMinutes = 30,
                actualMinutes = 0,
                levelAtStart = startLvl,
                mode = TrainingMode.FLEXIBLE,
                focus = null,
                isControl = true,
                isCompleted = false
            )
            trainingSessionId = trainingRepository.insertSession(session)
            trainingElapsedSeconds = 0; trainingPaused = false

            normativesActive = true; normativesCompletedSteps = emptySet()
            normatives501Levels = buildBotLadder(startLvl); normatives501PlayerWins = 0; normatives501BotWins = 0; normatives501CurrentLeg = 0
            normativesCricketLevels = buildBotLadder(startLvl); normativesCricketPlayerWins = 0; normativesCricketBotWins = 0; normativesCricketCurrentLeg = 0
            normativesNewLevel = null; isNormativesExercise = false; isTrainingActive = true; isFreeTraining = false

            saveNormativesSnapshot(); screen = "normatives"
        }
    }

    fun evaluateAndFinishNormatives() {
        scope.launch {
            // 1. Получаем последние результаты по 5 дисциплинам
            val res501 = withContext(Dispatchers.IO) { trainingRepository.getLatestExerciseResults("game501", 1) }.firstOrNull()
            val resCricket = withContext(Dispatchers.IO) { trainingRepository.getLatestExerciseResults("cricket", 1) }.firstOrNull()
            val resS20 = withContext(Dispatchers.IO) { trainingRepository.getLatestExerciseResults("sector20", 1) }.firstOrNull()
            val resBR = withContext(Dispatchers.IO) { trainingRepository.getLatestExerciseResults("biground", 1) }.firstOrNull()
            val resSS = withContext(Dispatchers.IO) { trainingRepository.getLatestExerciseResults("scoreset", 1) }.firstOrNull()

            // 2. Извлекаем значения
            val ppr = res501?.let { parseDetails(it.detailsJson)["ppr"]?.toFloatOrNull() } ?: 0f
            val mpr = resCricket?.let { parseDetails(it.detailsJson)["mpr"]?.toFloatOrNull() } ?: 0f
            val s20 = resS20?.score ?: 0
            val br = resBR?.score ?: 0
            val ss = resSS?.score ?: 0

            // 3. Считаем уровень по НОВОЙ формуле
            val newLvl = LevelCalculator.calculateExamLevel(ppr, mpr, s20, br, ss)
            val currentLvl = SettingsStorage.getStartLevel(context)
            val failedStreak = SettingsStorage.getFailedExamsStreak(context)
            
            var finalLvl = currentLvl
            var message = ""
            var newStreak = failedStreak

            // 4. Логика повышения / подтверждения / понижения
            if (newLvl > currentLvl) {
                finalLvl = newLvl
                newStreak = 0
                message = "🏆 Отличная работа! Ты сдал экзамен на уровень ${DartsNorms.getLevelName(newLvl)}. Твой новый уровень: $newLvl."
            } else if (newLvl == currentLvl) {
                newStreak = 0
                message = "✅ Экзамен сдан! Ты подтвердил свой уровень: ${DartsNorms.getLevelName(currentLvl)}."
            } else {
                // Сдали хуже
                newStreak += 1
                if (newStreak >= 2) {
                    finalLvl = (currentLvl - 1).coerceAtLeast(1)
                    newStreak = 0
                    message = "⚠️ Это уже второй экзамен подряд ниже твоего уровня. Увы, но мы вынуждены понизить твой уровень до ${DartsNorms.getLevelName(finalLvl)}. Не расстраивайся, ты обязательно вернёшь его обратно!"
                } else {
                    message = "Экзамен сдан ниже твоего текущего уровня. Но мы знаем, что дартс — игра дисперсии. Мы оставляем твой уровень прежним. Но если следующий экзамен будет тоже ниже, мы будем вынуждены понизить его на 1."
                }
            }

            // 5. Сохраняем уровень и стрик
            SettingsStorage.setStartLevel(context, finalLvl)
            SettingsStorage.setFailedExamsStreak(context, newStreak)

            // 6. ВСЕГДА обнуляем статистику (начинаем новый период)
            trainingRepository.clearStatisticsAfterNormatives()
            TrainingStateStorage.clear(context)
            NormativesStateStorage.clear(context)
            ProblemSectorsStorage.clear(context)

            isTrainingActive = false
            trainingSessionId = 0L
            normativesActive = false
            isNormativesExercise = false
            normativesNewLevel = finalLvl
            examResultMessage = message
            screen = "normatives"
        }
    }

    fun launchNormativesStep(step: NormativeStep) {
        when (step) {
            NormativeStep.GAME_501 -> {
                val leg = normatives501CurrentLeg
                val botLevel = normatives501Levels.getOrNull(leg) ?: normativesStartLevel
                val playerName = SettingsStorage.getPlayerName(context).ifBlank { "Игрок" }
                val players = listOf(
                    Player501(name = playerName, isBot = false, botLevel = 0, teamIndex = 0),
                    Player501(name = botNameFor(botLevel), isBot = true, botLevel = botLevel, teamIndex = 1)
                )
                game501 = Game501Logic.newGame(GameType.X501, OutMode.DOUBLE_OUT, players, 1, 1, false, 0, TRAINING_AUTO_OK_SECONDS, sessionStartTime, sessionForm)
                game501ExistingId = 0L; isTraining501 = true; isNormativesExercise = true; exerciseStartMillis = System.currentTimeMillis(); screen = "game501_game"
            }
            NormativeStep.CRICKET -> {
                val leg = normativesCricketCurrentLeg
                val botLevel = normativesCricketLevels.getOrNull(leg) ?: normativesStartLevel
                val playerName = SettingsStorage.getPlayerName(context).ifBlank { "Игрок" }
                val players = listOf(
                    CricketPlayer(name = playerName, isBot = false, botLevel = 0, teamIndex = 0),
                    CricketPlayer(name = botNameFor(botLevel), isBot = true, botLevel = botLevel, teamIndex = 1)
                )
                cricketGame = CricketLogic.newGame(CricketType.AMERICAN, players, 1, 1, false, 0, TRAINING_AUTO_OK_SECONDS, sessionStartTime, sessionForm)
                cricketExistingId = 0L; isTrainingCricket = true; isNormativesExercise = true; exerciseStartMillis = System.currentTimeMillis(); screen = "cricket_game"
            }
            NormativeStep.SECTOR_20, NormativeStep.BIG_ROUND, NormativeStep.SCORE_SET -> {
                isNormativesExercise = true; exerciseStartMillis = System.currentTimeMillis()
                if (step == NormativeStep.SECTOR_20) sectorToPlay = 20
                if (step == NormativeStep.BIG_ROUND) bigRoundCategory = BigRoundCategory.MALE
                screen = "normatives_exercise"
            }
        }
    }

    fun afterNormativesLeg(playerWon: Boolean, isGame501: Boolean) {
        if (isGame501) {
            if (playerWon) normatives501PlayerWins += 1 else normatives501BotWins += 1
            normatives501CurrentLeg += 1
            val finished = normatives501PlayerWins >= 3 || normatives501BotWins >= 3 || normatives501CurrentLeg >= 5
            if (finished) normativesCompletedSteps = normativesCompletedSteps + NormativeStep.GAME_501.id
            saveNormativesSnapshot()
        } else {
            if (playerWon) normativesCricketPlayerWins += 1 else normativesCricketBotWins += 1
            normativesCricketCurrentLeg += 1
            val finished = normativesCricketPlayerWins >= 3 || normativesCricketBotWins >= 3 || normativesCricketCurrentLeg >= 5
            if (finished) normativesCompletedSteps = normativesCompletedSteps + NormativeStep.CRICKET.id
            saveNormativesSnapshot()
        }

        if (normativesCompletedSteps.size == NormativeStep.entries.size) evaluateAndFinishNormatives()
        else screen = "normatives"
    }

    fun cancelNormatives() {
        NormativesStateStorage.clear(context); normativesActive = false; normativesCompletedSteps = emptySet()
        normativesNewLevel = null; isNormativesExercise = false; isTrainingActive = false; trainingSessionId = 0L; screen = "training"
    }

    fun resetTrainingState() {
        isTrainingActive = false; trainingElapsedSeconds = 0; trainingPaused = false; trainingSessionId = 0L
        currentPhase = TrainingBlockPhase.WARMUP; mainExercises = emptyList(); psychologyExercises = emptyList(); strengthExercises = emptyList()
        currentExerciseIndex = 0; exerciseStatuses = emptyMap(); exerciseStartMillis = 0L
        isTraining501 = false; isTrainingCricket = false; showRestTimer = false; restNextTitle = null; pendingNextStep = null
        showExitTrainingDialog = false; isFreeTraining = false; isSingleExercisePlay = false; isSetupForTraining = false
        freeCompletedIds = emptySet(); freeSkippedIds = emptySet(); freeCurrentId = null
        freeRouletteKey = 0; freeExercisesCompleted = 0; showFreeNewRoundDialog = false; showBullDraw = false; bullDrawExerciseId = null
        normativesActive = false; normativesCompletedSteps = emptySet(); normatives501Levels = emptyList()
        normatives501PlayerWins = 0; normatives501BotWins = 0; normatives501CurrentLeg = 0; normativesCricketLevels = emptyList()
        normativesCricketPlayerWins = 0; normativesCricketBotWins = 0; normativesCricketCurrentLeg = 0; normativesNewLevel = null
        isNormativesExercise = false; currentWeakness = null; currentLevel = 1; currentTargetGoals = emptyList()
        isResumingFromStats = false
        examResultMessage = null
    }

    fun activePhases(): List<TrainingBlockPhase> {
        val list = mutableListOf<TrainingBlockPhase>()
        if (TrainingBlocksStorage.isWarmupEnabled(context)) list.add(TrainingBlockPhase.WARMUP)
        if (TrainingBlocksStorage.isPsychologyEnabled(context)) list.add(TrainingBlockPhase.PSYCHOLOGY)
        list.add(TrainingBlockPhase.MAIN)
        if (TrainingBlocksStorage.isStrengthEnabled(context)) list.add(TrainingBlockPhase.STRENGTH)
        return list
    }

    fun nextPhaseAfter(phase: TrainingBlockPhase): TrainingBlockPhase {
        val phases = activePhases()
        val idx = phases.indexOf(phase)
        return if (idx >= 0 && idx < phases.lastIndex) phases[idx + 1] else TrainingBlockPhase.FINISHED
    }

    fun goToNextPhase() {
        val next = nextPhaseAfter(currentPhase)
        currentPhase = next
        currentExerciseIndex = 0
        exerciseStatuses = emptyMap()
        if (next == TrainingBlockPhase.FINISHED) {
            resetTrainingState()
            TrainingStateStorage.clear(context)
            screen = "main"
        } else {
            screen = "training_block"
        }
    }

    fun advanceAfterExercise(finishedExerciseId: String) {
        if (isSingleExercisePlay) {
            isSingleExercisePlay = false; isTrainingActive = false; trainingSessionId = 0L; isFreeTraining = false; screen = "main"; return
        }
        if (isFreeTraining) {
            freeExercisesCompleted += 1; freeCompletedIds = freeCompletedIds + finishedExerciseId; freeCurrentId = null
            showRestThen(TrainingPlanner.restSecondsAfter(finishedExerciseId), null) { pickNextFreeExercise(); screen = "training_free_next" }
        } else {
            val list = if (currentPhase == TrainingBlockPhase.MAIN) mainExercises else if (currentPhase == TrainingBlockPhase.PSYCHOLOGY) psychologyExercises else strengthExercises
            if (currentExerciseIndex + 1 < list.size) {
                showRestThen(TrainingPlanner.restSecondsAfter(finishedExerciseId), list[currentExerciseIndex + 1].title) {
                    currentExerciseIndex += 1; screen = "training_block"
                }
            } else screen = "training_summary"
        }
    }

    fun finishCurrentMainExercise() {
        val finishedId = if (isFreeTraining) freeCurrentId ?: "" else mainExercises.getOrNull(currentExerciseIndex)?.id ?: ""
        if (!isFreeTraining && !isSingleExercisePlay) {
            exerciseStatuses = exerciseStatuses.toMutableMap().apply { put(currentExerciseIndex, SummaryStatus.DONE) }
        }
        advanceAfterExercise(finishedId)
    }

    // ────────────────────────────────────────────────────────
    // ЭФФЕКТЫ
    // ────────────────────────────────────────────────────────
    LaunchedEffect(isTrainingActive, trainingPaused, screen) {
        if (!isTrainingActive || trainingPaused || screen !in TRAINING_SCREENS) return@LaunchedEffect
        while (true) {
            delay(1000L)
            trainingElapsedSeconds += 1
            // Показываем окно ровно за 5 минут (300 секунд) до конца
            if (!isFreeTraining && !normativesActive && trainingElapsedSeconds == trainingMinutes * 60 - 300) {
                showTimeUpDialog = true
            }
        }
    }
    
    LaunchedEffect(trainingSessionId, currentPhase, currentExerciseIndex, exerciseStatuses, mainExercises) {
        if (isTrainingActive && trainingSessionId > 0L && !isFreeTraining && !normativesActive) saveCurrentTrainingState()
    }

    LaunchedEffect(isTrainingActive, screen) {
        if (!isTrainingActive || screen !in TRAINING_SCREENS) return@LaunchedEffect
        while (true) {
            delay(30_000L)
            if (!isFreeTraining && !normativesActive) saveCurrentTrainingState()
        }
    }

    if (stage == "loading") LaunchedEffect(Unit) { delay(3000); stage = "main" }

    // ────────────────────────────────────────────────────────
    // UI И НАВИГ
    // ────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        when (stage) {
            "welcome" -> WelcomeScreen(onStart = { stage = "onboarding" })
            "onboarding" -> OnboardingScreen(onFinish = { result ->
                SettingsStorage.setPlayerName(context, result.name)
                SettingsStorage.setTrainingMinutes(context, result.trainingMinutes)
                SettingsStorage.setTrainingsPerWeek(context, result.trainingsPerWeek)
                SettingsStorage.setTrainingMode(context, result.trainingMode)
                SettingsStorage.setStartMode(context, result.startMode)
                SettingsStorage.setStartLevel(context, result.startLevel)
                SettingsStorage.setOnboardingDone(context)
                onboardingDone = true
                stage = "main"
            })
            "loading" -> LoadingScreen()

            "main" -> when (screen) {
                "main" -> MainMenuScreen(
                    onTraining = {
                        val snapN = NormativesStateStorage.load(context)
                        if (snapN != null) {
                            if (NormativesStateStorage.isExpired(snapN)) NormativesStateStorage.clear(context)
                            else { pendingNormativesResume = snapN; showResumeNormativesDialog = true; autoOpenTimeDialog = false }
                        }
                        val snap = TrainingStateStorage.load(context)
                        if (snap != null && snap.sessionId > 0L) {
                            if (TrainingStateStorage.isExpired(snap)) { TrainingStateStorage.clear(context); resetTrainingState(); autoOpenTimeDialog = true }
                            else { pendingResume = snap; showResumeDialog = true; autoOpenTimeDialog = false }
                        } else autoOpenTimeDialog = true
                        scope.launch { normativesAvailable = TrainingStatsAnalyzer.analyze(trainingRepository).isReadyForNormatives }
                        screen = "training"
                    },
                    onFreePlay = { 
                        isTraining501 = false
                        isTrainingCricket = false
                        isResumingFromStats = false
                        isOnlineGame = false
                        screen = "game_select" 
                    }, 
                    onStatsClick = { statsMenuKey++; screen = "stats_menu" },
                    onSettingsClick = { screen = "settings" }, onOpenDetailedStats = { screen = "detailed_stats" },
                    onOnlineClick = { screen = "online_lobby" }
                )

                "online_lobby" -> {
                    OnlineLobbyScreen(
                        onBack = { screen = "main" },
                        onPlayFriend = {
                            // TODO: Здесь будет логика приглашения друга
                        },
                        onPlayBot = { is501, level, legs, sets, nickname, saveToTraining ->
                            pendingOnlineGameParams = OnlineGameParams(is501, level, legs, sets, nickname, saveToTraining)
                            bullDrawExerciseId = if (is501) "game501" else "cricket"
                            showBullDraw = true
                        }
                    )
                }

                "detailed_stats" -> DetailedStatsScreen(
                    repository = trainingRepository,
                    game501Repository = game501Repository,
                    cricketRepository = cricketRepository,
                    onBack = { screen = "main" },
                    onOpenDiscipline = { selectedDiscipline = it; screen = "discipline_stats" },
                    onResumeGame501 = { entity -> 
                        Game501Serializer.fromJson(entity.stateBlob)?.let { 
                            game501 = it
                            game501ExistingId = entity.id
                            isTraining501 = true 
                            isResumingFromStats = true 
                            scope.launch {
                                val session = TrainingSession(
                                    dateMillis = System.currentTimeMillis(),
                                    plannedMinutes = 0,
                                    actualMinutes = 0,
                                    levelAtStart = currentLevel,
                                    mode = TrainingMode.FLEXIBLE,
                                    focus = null,
                                    isControl = false,
                                    isCompleted = false
                                )
                                trainingSessionId = trainingRepository.insertSession(session)
                                isTrainingActive = true
                                screen = "game501_game"
                            }
                        } 
                    },
                    onResumeCricket = { entity -> 
                        CricketSerializer.fromJson(entity.stateBlob)?.let { 
                            cricketGame = it
                            cricketExistingId = entity.id
                            isTrainingCricket = true
                            isResumingFromStats = true 
                            scope.launch {
                                val session = TrainingSession(
                                    dateMillis = System.currentTimeMillis(),
                                    plannedMinutes = 0,
                                    actualMinutes = 0,
                                    levelAtStart = currentLevel,
                                    mode = TrainingMode.FLEXIBLE,
                                    focus = null,
                                    isControl = false,
                                    isCompleted = false
                                )
                                trainingSessionId = trainingRepository.insertSession(session)
                                isTrainingActive = true
                                screen = "cricket_game"
                            }
                        } 
                    }
                )
                
                "discipline_stats" -> DisciplineStatsScreen(repository = trainingRepository, disciplineId = selectedDiscipline ?: "game501", onBack = { screen = "detailed_stats" })

                "stats_menu", "stats_cricket", "stats_501", "sector_stats", "aroundclock_stats", "biground_stats", "scoreset_stats" -> {
                    StatsRoutes(
                        screen = screen, onScreenChange = { screen = it },
                        onResumeGame501 = { entity -> Game501Serializer.fromJson(entity.stateBlob)?.let { game501 = it; game501ExistingId = entity.id; screen = "game501_game" } },
                        onResumeCricket = { entity -> CricketSerializer.fromJson(entity.stateBlob)?.let { cricketGame = it; cricketExistingId = entity.id; screen = "cricket_game" } },
                        cricketRepository = cricketRepository, game501Repository = game501Repository, sectorRepository = sectorRepository,
                        aroundClockRepository = aroundClockRepository, bigRoundRepository = bigRoundRepository, scoreSetRepository = scoreSetRepository,
                        freePlayRecordsRepository = freePlayRecordsRepository, statsMenuKey = statsMenuKey, onStatsMenuKeyChange = { statsMenuKey = it }
                    )
                }

                "game_select", "cricket_setup", "cricket_game", "cricket_stats", "game501_setup", "game501_game", "game501_stats",
                "sector_setup", "sector_game", "aroundclock_setup", "aroundclock_game", "biground_setup", "biground_game", "scoreset_setup", "scoreset_game" -> {
                    GameRoutes(
                        screen = screen, onScreenChange = { screen = it }, playerName = SettingsStorage.getPlayerName(context), context = context, scope = scope,
                        cricketRepository = cricketRepository, game501Repository = game501Repository, sectorRepository = sectorRepository,
                        aroundClockRepository = aroundClockRepository, bigRoundRepository = bigRoundRepository, scoreSetRepository = scoreSetRepository,
                        freePlayRecordsRepository = freePlayRecordsRepository, trainingRepository = trainingRepository,
                        cricketGame = cricketGame, onCricketGameChange = { cricketGame = it }, cricketExistingId = cricketExistingId, onCricketExistingIdChange = { cricketExistingId = it },
                        game501 = game501, onGame501Change = { game501 = it }, game501ExistingId = game501ExistingId, onGame501ExistingIdChange = { game501ExistingId = it },
                        sectorToPlay = sectorToPlay, onSectorToPlayChange = { sectorToPlay = it }, aroundClockTarget = aroundClockTarget, onAroundClockTargetChange = { aroundClockTarget = it },
                        aroundClockOrder = aroundClockOrder, onAroundClockOrderChange = { aroundClockOrder = it }, aroundClockHits = aroundClockHits, onAroundClockHitsChange = { aroundClockHits = it },
                        bigRoundCategory = bigRoundCategory, onBigRoundCategoryChange = { bigRoundCategory = it },
                        sessionStartTime = sessionStartTime, sessionForm = sessionForm, isTraining501 = isTraining501, isTrainingCricket = isTrainingCricket,
                        isOnlineGame = isOnlineGame, isResumingFromStats = isResumingFromStats, 
                        trainingSessionId = trainingSessionId, exerciseStartMillis = exerciseStartMillis, isNormativesExercise = isNormativesExercise,
                        onTrainingGameFinished = { playerWon, isGame501 ->
                            if (isGame501) {
                                TrainingUnfinishedStorage.removeUnfinished501(context, game501ExistingId)
                            } else {
                                TrainingUnfinishedStorage.removeUnfinishedCricket(context, cricketExistingId)
                            }
                            
                            isTraining501 = false; isTrainingCricket = false; game501 = null; game501ExistingId = 0L; cricketGame = null; cricketExistingId = 0L
                            if (isNormativesExercise) afterNormativesLeg(playerWon, isGame501) else finishCurrentMainExercise()
                        }
                    )
                }

                "training", "training_settings", "training_progress", "training_warmup", "training_block", "training_exercise",
                "training_summary", "training_free_next", "training_free_summary", "normatives", "normatives_exercise" -> {
                    TrainingRoutes(
                        screen = screen, onScreenChange = { screen = it },
                        trainingRepository = trainingRepository, aroundClockRepository = aroundClockRepository, sectorRepository = sectorRepository,
                        scoreSetRepository = scoreSetRepository, bigRoundRepository = bigRoundRepository, playerName = SettingsStorage.getPlayerName(context),
                        context = context, scope = scope, trainingMinutes = trainingMinutes, isTrainingActive = isTrainingActive,
                        trainingElapsedSeconds = trainingElapsedSeconds, trainingPaused = trainingPaused, onTogglePause = { trainingPaused = !trainingPaused },
                        currentPhase = currentPhase.name, 
                        mainExercises = mainExercises, psychologyExercises = psychologyExercises, strengthExercises = strengthExercises,
                        currentExerciseIndex = currentExerciseIndex, onCurrentExerciseIndexChange = { currentExerciseIndex = it }, exerciseStatuses = exerciseStatuses,
                        onExerciseStatusesChange = { exerciseStatuses = it }, currentWeakness = currentWeakness, currentLevel = currentLevel, currentTargetGoals = currentTargetGoals,
                        isFreeTraining = isFreeTraining, isSingleExercisePlay = isSingleExercisePlay, isSetupForTraining = isSetupForTraining, onIsSetupForTrainingChange = { isSetupForTraining = it },
                        freeNoRepeat = freeNoRepeat, onToggleNoRepeat = { saveFreeNoRepeat(it) }, 
                        freeChooseMyself = freeChooseMyself, onToggleChooseMyself = { saveFreeChooseMyself(it) },
                        freeCompletedIds = freeCompletedIds, onFreeCompletedIdsChange = { freeCompletedIds = it }, freeSkippedIds = freeSkippedIds, onFreeSkippedIdsChange = { freeSkippedIds = it },
                        freeCurrentId = freeCurrentId, onFreeCurrentIdChange = { freeCurrentId = it }, freeRouletteKey = freeRouletteKey, freeExercisesCompleted = freeExercisesCompleted,
                        onFreeExercisesCompletedChange = { freeExercisesCompleted = it }, showFreeNewRoundDialog = showFreeNewRoundDialog, onShowFreeNewRoundDialogChange = { showFreeNewRoundDialog = it },
                        onPickNextFreeExercise = { pickNextFreeExercise() },
                        onFreePlayAction = { launchExercise(it ?: "") }, onFreePlayDirect = { id, isFriend ->
                            if (isFriend && (id == "game501" || id == "cricket")) {
                                isSetupForTraining = true; 
                                if (id == "game501") isTraining501 = true else isTrainingCricket = true
                                scope.launch { screen = if (id == "game501") "game501_setup" else "cricket_setup" }
                            } else { freeCurrentId = id; launchExercise(id) }
                        }, onFreeSkip = { id -> if (id != null) freeSkippedIds = freeSkippedIds + id; freeCurrentId = null }, onFreeFinish = { screen = "training_free_summary" },
                        showRestTimer = showRestTimer, restTimerSeconds = restTimerSeconds, restNextTitle = restNextTitle,
                        onSkipRestTimer = { showRestTimer = false; val step = pendingNextStep; pendingNextStep = null; restNextTitle = null; step?.invoke() },
                        normativesAvailable = normativesAvailable, normativesActive = normativesActive, normativesStartLevel = normativesStartLevel,
                        normativesCompletedSteps = normativesCompletedSteps, normativesNewLevel = normativesNewLevel, onStartStep = { launchNormativesStep(it) },
                        onFinishNormatives = { normativesActive = false; normativesNewLevel = null; normativesCompletedSteps = emptySet(); isNormativesExercise = false; isTrainingActive = false; trainingSessionId = 0L; screen = "training" },
                        onCancelNormatives = { cancelNormatives() }, exerciseStartMillis = exerciseStartMillis, trainingSessionId = trainingSessionId,
                        onExerciseFinished = { id, blockType, score, darts, details ->
                            val duration = if (exerciseStartMillis > 0L) ((System.currentTimeMillis() - exerciseStartMillis) / 1000L).toInt() else 0
                            exerciseStartMillis = 0L
                            if (isNormativesExercise) {
                                normativesCompletedSteps = normativesCompletedSteps + id; saveNormativesSnapshot()
                                if (normativesCompletedSteps.size == NormativeStep.entries.size) evaluateAndFinishNormatives()
                                else { isNormativesExercise = false; sectorToPlay = null; bigRoundCategory = null; screen = "normatives" }
                            } else {
                                scope.launch {
                                    trainingRepository.insertExerciseResult(
                                        ExerciseResult(
                                            sessionId = trainingSessionId,
                                            blockType = blockType,
                                            exerciseId = id,
                                            score = score,
                                            dartsThrown = darts,
                                            timestamp = System.currentTimeMillis(),
                                            durationSeconds = duration,
                                            detailsJson = details
                                        )
                                    )
                                    trainingRepository.updateTrainingRecordIfBetter(id, score, darts, id.startsWith("aroundclock_"))
                                }
                                if (!isFreeTraining && !isSingleExercisePlay) exerciseStatuses = exerciseStatuses.toMutableMap().apply { put(currentExerciseIndex, SummaryStatus.DONE) }
                                advanceAfterExercise(id)
                            }
                        },
                        onBackFromNormativesExercise = { isNormativesExercise = false; sectorToPlay = null; bigRoundCategory = null; screen = "normatives" },
                        onStartTrainingClick = {
                            autoOpenTimeDialog = false
                            trainingMinutes = context.getSharedPreferences("training_prefs", Context.MODE_PRIVATE).getInt("session_minutes", SettingsStorage.getTrainingMinutes(context))
                            scope.launch {
                                val analysis = TrainingStatsAnalyzer.analyze(trainingRepository)
                                val completedCount = trainingRepository.getCompletedSessionCount()
                                currentWeakness = analysis.weakestType; currentLevel = analysis.level; currentTargetGoals = analysis.targetPracticeGoals
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
                                trainingElapsedSeconds = 0; trainingPaused = false; showTimeUpDialog = false
                                val warmupMin = if (TrainingBlocksStorage.isWarmupEnabled(context)) 6 else 0
                                val psychoMin = if (TrainingBlocksStorage.isPsychologyEnabled(context)) 8 else 0
                                val strengthMin = if (TrainingBlocksStorage.isStrengthEnabled(context)) 7 else 0
                                val budgetMain = (trainingMinutes - warmupMin - psychoMin - strengthMin - 5).coerceAtLeast(10)
                                val dynamicIds = TrainingPlanner.buildDynamicMainExerciseIds(analysis, budgetMain, completedCount)
                                val baseMap = getDefaultMainExercises().associateBy { it.id }
                                mainExercises = dynamicIds.mapNotNull { d -> baseMap[d.id]?.copy(minutes = d.minutes) }.ifEmpty { listOf(getDefaultMainExercises().first().copy(minutes = budgetMain.coerceIn(5, 20))) }
                                psychologyExercises = if (TrainingBlocksStorage.isPsychologyEnabled(context)) getNextPsychologyExercises(context) else emptyList()
                                strengthExercises = if (TrainingBlocksStorage.isStrengthEnabled(context)) getNextStrengthExercises(context) else emptyList()
                                currentPhase = activePhases().firstOrNull() ?: TrainingBlockPhase.MAIN
                                currentExerciseIndex = 0; exerciseStatuses = emptyMap(); isTrainingActive = true; isFreeTraining = false
                                TrainingStateStorage.save(context, trainingSessionId, trainingMinutes, 0, currentPhase.name, 0, emptyMap(), mainExercises.map { it.id to it.minutes }, psychologyExercises.isNotEmpty(), strengthExercises.isNotEmpty())
                                screen = if (currentPhase == TrainingBlockPhase.WARMUP) "training_warmup" else "training_block"
                            }
                        },
                        onStartFreeTrainingClick = {
                            autoOpenTimeDialog = false
                            scope.launch {
                                val analysis = TrainingStatsAnalyzer.analyze(trainingRepository)
                                val completedCount = trainingRepository.getCompletedSessionCount()
                                currentWeakness = analysis.weakestType; currentLevel = analysis.level; currentTargetGoals = analysis.targetPracticeGoals
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
                                trainingElapsedSeconds = 0; trainingPaused = false; trainingMinutes = 0
                                val dynamicIds = TrainingPlanner.buildDynamicMainExerciseIds(analysis, 999, completedCount, ignoreHardLimit = true)
                                val baseMap = getDefaultMainExercises().associateBy { it.id }
                                mainExercises = dynamicIds.mapNotNull { d -> baseMap[d.id]?.copy(minutes = d.minutes) }.ifEmpty { getDefaultMainExercises() }
                                psychologyExercises = emptyList(); strengthExercises = emptyList()
                                currentPhase = TrainingBlockPhase.MAIN; currentExerciseIndex = 0; exerciseStatuses = emptyMap()
                                isTrainingActive = true; isFreeTraining = true; 
                                freeCompletedIds = emptySet(); freeSkippedIds = emptySet(); freeCurrentId = null; freeRouletteKey = 0; freeExercisesCompleted = 0
                                screen = "training_free_next"
                            }
                        },
                        onStartNormativesClick = { startNewNormatives() }, onOpenSettingsClick = { screen = "training_settings" }, onOpenProgressClick = { screen = "training_progress" },
                        onBackFromTrainingClick = { if (isTrainingActive && trainingSessionId > 0L && !isFreeTraining && !normativesActive) showExitTrainingDialog = true else { screen = "main" } },
                        onWarmupFinish = {
                            val next = nextPhaseAfter(TrainingBlockPhase.WARMUP); currentPhase = next; currentExerciseIndex = 0; exerciseStatuses = emptyMap()
                            screen = if (next == TrainingBlockPhase.FINISHED) "training_summary" else "training_block"
                        },
                        onStartCurrentExercise = { current -> launchExercise(current.id) },
                        onSkipCurrentExercise = { list ->
                            exerciseStatuses = exerciseStatuses.toMutableMap().apply { put(currentExerciseIndex, SummaryStatus.SKIPPED) }
                            if (currentExerciseIndex + 1 < list.size) currentExerciseIndex += 1 else screen = "training_summary"
                        },
                        onBackFromBlock = { showExitTrainingDialog = true }, onFinishFreeTraining = { resetTrainingState(); TrainingStateStorage.clear(context); screen = "main" },
                        showExitTrainingDialog = showExitTrainingDialog, onShowExitTrainingDialogChange = { showExitTrainingDialog = it },
                        onSaveAndPostpone = { showExitTrainingDialog = false; saveCurrentTrainingState(); screen = "main" },
                        onFinishAndClear = { resetTrainingState(); TrainingStateStorage.clear(context); screen = "main" },
                        showResumeDialog = showResumeDialog, onShowResumeDialogChange = { showResumeDialog = it },
                        showTimeUpDialog = showTimeUpDialog, onShowTimeUpDialogChange = { showTimeUpDialog = it },
                        onTimeUpFinish = { showTimeUpDialog = false; goToNextPhase() },
                        onTimeUpContinue = { showTimeUpDialog = false }, restSeconds = 0,
                        onInventoryUpdated = {
                            if (currentPhase == TrainingBlockPhase.STRENGTH) {
                                strengthExercises = getNextStrengthExercises(context)
                            }
                        },
                        isLastPhase = nextPhaseAfter(currentPhase) == TrainingBlockPhase.FINISHED,
                        onContinueToNextPhase = { goToNextPhase() }
                    )
                }

                "settings" -> SettingsScreen(onBack = { screen = "main" }, onOpenAbout = { screen = "about" }, onFactoryReset = {
                    cricketGame = null; cricketExistingId = 0L; game501 = null; game501ExistingId = 0L; sectorToPlay = null
                    aroundClockTarget = null; aroundClockOrder = null; aroundClockHits = 1; bigRoundCategory = null
                    NormativesStateStorage.clear(context); resetTrainingState(); autoOpenTimeDialog = true
                    onboardingDone = false; screen = "main"; stage = "welcome"
                })
                "about" -> AboutScreen(onBack = { screen = "settings" })
            }
        }

        if (isTrainingActive && screen in TRAINING_SCREENS && !showRestTimer && screen != "training_free_summary" && !normativesActive) {
            TrainingTimerOverlay(
                secondsLeft = if (isFreeTraining) trainingElapsedSeconds else (trainingMinutes * 60 - trainingElapsedSeconds),
                isPaused = trainingPaused, onTogglePause = { trainingPaused = !trainingPaused }
            )
        }
        if (showRestTimer) {
            RestTimerOverlay(totalSeconds = restTimerSeconds, nextExerciseTitle = restNextTitle, onSkip = { showRestTimer = false; val step = pendingNextStep; pendingNextStep = null; restNextTitle = null; step?.invoke() })
        }
        
        // Показ результата экзамена
        if (examResultMessage != null) {
            AlertDialog(
                onDismissRequest = { examResultMessage = null },
                containerColor = TileBgDark,
                confirmButton = {
                    TextButton(onClick = { examResultMessage = null }) {
                        Text("ОК", color = Accent, fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("Результат экзамена", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                text = { Text(examResultMessage!!, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp) }
            )
        }
    }

    // ────────────────────────────────────────────────────────
    // ДИАЛОГИ
    // ────────────────────────────────────────────────────────
    val onlineParams = pendingOnlineGameParams
    val botLvlForBull = if (bullDrawExerciseId == "game501") TrainingBotLevelStorage.getLevel501(context, currentLevel) else TrainingBotLevelStorage.getLevelCricket(context, currentLevel)
    val effectiveBotLevel = onlineParams?.level ?: botLvlForBull
    val effectiveBotName = botNameFor(effectiveBotLevel)

    DialogsRoutes(
        context = context, showBullDraw = showBullDraw, bullDrawExerciseId = bullDrawExerciseId, playerName = SettingsStorage.getPlayerName(context).ifBlank { "Игрок" },
        botName = effectiveBotName, botLevel = effectiveBotLevel,
        onBullWinnerChosen = { winner ->
            showBullDraw = false
            if (onlineParams != null) {
                startOnlineBotGame(onlineParams, winner)
                pendingOnlineGameParams = null
            } else {
                startTrainingGame(bullDrawExerciseId ?: "", winner)
            }
            bullDrawExerciseId = null
        },
        onBullCancel = {
            showBullDraw = false
            if (onlineParams != null) {
                startOnlineBotGame(onlineParams, 0)
                pendingOnlineGameParams = null
            } else {
                startTrainingGame(bullDrawExerciseId ?: "", 0)
            }
            bullDrawExerciseId = null
        },
        showExitTrainingDialog = showExitTrainingDialog, onExitFinish = { showExitTrainingDialog = false; resetTrainingState(); TrainingStateStorage.clear(context); screen = "main" },
        onExitPostpone = { showExitTrainingDialog = false; saveCurrentTrainingState(); screen = "main" },
        showTimeUpDialog = showTimeUpDialog, trainingMinutes = trainingMinutes,
        onTimeUpFinish = { showTimeUpDialog = false; goToNextPhase() },
        onTimeUpContinue = { showTimeUpDialog = false },
        showFreeNewRoundDialog = showFreeNewRoundDialog, onNewRound = { showFreeNewRoundDialog = false; freeCompletedIds = emptySet(); freeSkippedIds = emptySet(); pickNextFreeExercise() }, onFinishTraining = { showFreeNewRoundDialog = false; screen = "training_free_summary" },
        showResumeDialog = showResumeDialog, pendingResume = pendingResume,
        onResumeContinue = {
            pendingResume?.let { snap ->
                trainingSessionId = snap.sessionId; trainingMinutes = snap.minutes; trainingElapsedSeconds = snap.elapsedSeconds
                currentPhase = runCatching { TrainingBlockPhase.valueOf(snap.phaseName) }.getOrDefault(TrainingBlockPhase.MAIN)
                currentExerciseIndex = snap.exerciseIndex
                exerciseStatuses = snap.statuses.mapNotNull { (k, v) -> runCatching { k to SummaryStatus.valueOf(v) }.getOrNull() }.toMap()
                mainExercises = if (snap.mainExerciseIds.isNotEmpty()) snap.mainExerciseIds.mapNotNull { (id, m) -> getDefaultMainExercises().associateBy { it.id }[id]?.copy(minutes = m) }.ifEmpty { getDefaultMainExercises() } else getDefaultMainExercises()
                psychologyExercises = if (snap.psychologyEnabled) getNextPsychologyExercises(context) else emptyList()
                strengthExercises = if (snap.strengthEnabled) getNextStrengthExercises(context) else emptyList()
                trainingPaused = false; isTrainingActive = true; isFreeTraining = false
                screen = if (currentPhase == TrainingBlockPhase.WARMUP) "training_warmup" else "training_block"
                pendingResume = null; showResumeDialog = false
            }
        }, onResumeRestart = { TrainingStateStorage.clear(context); resetTrainingState(); showResumeDialog = false; pendingResume = null },
        showResumeNormativesDialog = showResumeNormativesDialog, pendingNormativesResume = pendingNormativesResume,
        onNormativesResumeContinue = {
            pendingNormativesResume?.let { snap ->
                normativesCompletedSteps = snap.completedSteps; normatives501Levels = snap.game501Levels; normativesCricketLevels = snap.cricketLevels
                normatives501PlayerWins = snap.game501Results.count { it }; normatives501BotWins = snap.game501Results.count { !it }; normatives501CurrentLeg = snap.game501Results.size
                normativesCricketPlayerWins = snap.cricketResults.count { it }; normativesCricketBotWins = snap.cricketResults.count { !it }; normativesCricketCurrentLeg = snap.cricketResults.size
                scope.launch { normativesStartLevel = TrainingStatsAnalyzer.analyze(trainingRepository).level.coerceAtLeast(1) }
                normativesActive = true; normativesNewLevel = null; screen = "normatives"
                showResumeNormativesDialog = false; pendingNormativesResume = null
            }
        }, onNormativesResumeRestart = { NormativesStateStorage.clear(context); showResumeNormativesDialog = false; pendingNormativesResume = null }
    )
}

// ─────────────────────────────────────────────
// ВСПОМОГАТЕЛЬНАЯ ФУНКЦИЯ ДЛЯ ПАРСИНГА detailsJson
// ─────────────────────────────────────────────
private fun parseDetails(s: String): Map<String, String> {
    if (s.isBlank()) return emptyMap()
    return s.split(";").mapNotNull { entry ->
        val parts = entry.split("=", limit = 2)
        if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
    }.toMap()
}
