package com.lodkin.dartstrainer.ui

import androidx.compose.runtime.Composable
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.data.training.BlockType
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.ui.training.*
import kotlinx.coroutines.CoroutineScope

/**
 * Маршрутизатор для экранов тренировки и нормативов.
 * Огромный кусок логики, вынесенный из MainActivity.
 */
@Composable
fun TrainingRoutes(
    screen: String,
    onScreenChange: (String) -> Unit,
    
    // Репозитории
    trainingRepository: TrainingRepository,
    aroundClockRepository: AroundClockRepository,
    sectorRepository: SectorRepository,
    scoreSetRepository: ScoreSetRepository,
    bigRoundRepository: BigRoundRepository,
    playerName: String,
    context: android.content.Context,
    scope: CoroutineScope,

    // --- СОСТОЯНИЕ ТРЕНИРОВКИ ---
    trainingMinutes: Int,
    isTrainingActive: Boolean,
    trainingElapsedSeconds: Int,
    trainingPaused: Boolean,
    onTogglePause: () -> Unit,
    
    currentPhase: String, // Передаем название фазы строкой
    mainExercises: List<PlannedExercise>,
    psychologyExercises: List<PlannedExercise>,
    strengthExercises: List<PlannedExercise>,
    currentExerciseIndex: Int,
    onCurrentExerciseIndexChange: (Int) -> Unit,
    exerciseStatuses: Map<Int, SummaryStatus>,
    onExerciseStatusesChange: (Map<Int, SummaryStatus>) -> Unit,
    currentWeakness: WeaknessType?,
    currentLevel: Int,
    currentTargetGoals: List<String>,

    // --- СВОБОДНАЯ ТРЕНИРОВКА ---
    isFreeTraining: Boolean,
    isSingleExercisePlay: Boolean,
    isSetupForTraining: Boolean,
    onIsSetupForTrainingChange: (Boolean) -> Unit,
    freeNoRepeat: Boolean,
    onToggleNoRepeat: (Boolean) -> Unit,
    freeChooseMyself: Boolean,
    onToggleChooseMyself: (Boolean) -> Unit,
    freeCompletedIds: Set<String>,
    onFreeCompletedIdsChange: (Set<String>) -> Unit,
    freeSkippedIds: Set<String>,
    onFreeSkippedIdsChange: (Set<String>) -> Unit,
    freeCurrentId: String?,
    onFreeCurrentIdChange: (String?) -> Unit,
    freeRouletteKey: Int,
    freeExercisesCompleted: Int,
    onFreeExercisesCompletedChange: (Int) -> Unit,
    showFreeNewRoundDialog: Boolean,
    onShowFreeNewRoundDialogChange: (Boolean) -> Unit,
    onFreePlayAction: (String?) -> Unit,
    onFreePlayDirect: (String, Boolean) -> Unit,
    onFreeSkip: (String?) -> Unit,
    onFreeFinish: () -> Unit,

    // --- ТАЙМЕР ОТДЫХА ---
    showRestTimer: Boolean,
    restTimerSeconds: Int,
    restNextTitle: String?,
    onSkipRestTimer: () -> Unit,

    // --- ЭКЗАМЕН (НОРМАТИВЫ) ---
    normativesAvailable: Boolean,
    normativesActive: Boolean,
    normativesStartLevel: Int,
    normativesCompletedSteps: Set<String>,
    normativesNewLevel: Int?,
    onStartStep: (NormativeStep) -> Unit,
    onFinishNormatives: () -> Unit,
    onCancelNormatives: () -> Unit,
    exerciseStartMillis: Long,
    trainingSessionId: Long,
    onExerciseFinished: (exerciseId: String, blockType: BlockType, score: Int, darts: Int, details: String) -> Unit,
    onBackFromNormativesExercise: () -> Unit,

    // --- ВЫЗОВЫ К ЛОГИКЕ ---
    onStartTrainingClick: () -> Unit,
    onStartFreeTrainingClick: () -> Unit,
    onStartNormativesClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onOpenProgressClick: () -> Unit,
    onBackFromTrainingClick: () -> Unit,
    onWarmupFinish: () -> Unit,
    onStartCurrentExercise: (PlannedExercise) -> Unit,
    onSkipCurrentExercise: (List<PlannedExercise>) -> Unit,
    onBackFromBlock: () -> Unit,
    onFinishFreeTraining: () -> Unit,
    
    // --- ДИАЛОГИ ---
    showExitTrainingDialog: Boolean,
    onShowExitTrainingDialogChange: (Boolean) -> Unit,
    onSaveAndPostpone: () -> Unit,
    onFinishAndClear: () -> Unit,
    showResumeDialog: Boolean,
    onShowResumeDialogChange: (Boolean) -> Unit,
    showTimeUpDialog: Boolean,
    onShowTimeUpDialogChange: (Boolean) -> Unit,
    onTimeUpFinish: () -> Unit,
    onTimeUpContinue: () -> Unit,
    restSeconds: Int
) {
    val trainingPhase = currentPhase

    when (screen) {
        "training" -> TrainingMenuScreen(
            playerName = playerName,
            repository = trainingRepository,
            autoOpenTimeDialog = false, // Управляется извне
            normativesAvailable = normativesAvailable,
            onStartTraining = onStartTrainingClick,
            onStartFreeTraining = onStartFreeTrainingClick,
            onStartNormatives = onStartNormativesClick,
            onOpenSettings = onOpenSettingsClick,
            onOpenProgress = onOpenProgressClick,
            onBack = onBackFromTrainingClick
        )

        "training_settings" -> TrainingSettingsScreen(
            repository = trainingRepository,
            onBack = { onScreenChange("training") }
        )

        "training_progress" -> TrainingProgressScreen(
            repository = trainingRepository,
            onBack = { onScreenChange("training") }
        )

        "training_warmup" -> TrainingWarmupScreen(
            longWarmup = trainingMinutes >= 100,
            onFinish = onWarmupFinish,
            onBack = { onScreenChange("training") }
        )

        "training_block" -> {
            val list = when (trainingPhase) {
                "WARMUP" -> mainExercises
                "PSYCHOLOGY" -> psychologyExercises
                "STRENGTH" -> strengthExercises
                else -> mainExercises
            }
            TrainingBlockScreen(
                exercises = list,
                currentIndex = currentExerciseIndex,
                onStartCurrent = {
                    val current = list.getOrNull(currentExerciseIndex)
                    if (current != null) onStartCurrentExercise(current)
                },
                onSkipCurrent = { onSkipCurrentExercise(list) },
                onBack = onBackFromBlock
            )
        }

        "training_free_next" -> {
            if (freeCurrentId != null || freeChooseMyself) {
                FreeRouletteScreen(
                    finalExercise = mainExercises.firstOrNull { it.id == freeCurrentId },
                    allExercises = mainExercises,
                    completedCount = freeExercisesCompleted,
                    noRepeat = freeNoRepeat,
                    onToggleNoRepeat = onToggleNoRepeat,
                    chooseMyself = freeChooseMyself,
                    onToggleChooseMyself = onToggleChooseMyself,
                    onPlay = { onFreePlayAction(freeCurrentId) },
                    onPlayDirect = onFreePlayDirect,
                    onSkip = { onFreeSkip(freeCurrentId) },
                    onFinish = onFreeFinish
                )
            }
        }

        "training_free_summary" -> {
            FreeTrainingSummaryScreen(
                playerName = playerName,
                sessionId = trainingSessionId,
                actualSeconds = trainingElapsedSeconds,
                repository = trainingRepository,
                onFinishTraining = onFinishFreeTraining
            )
        }

        "training_exercise" -> {
            val current = if (isFreeTraining) {
                mainExercises.firstOrNull { it.id == freeCurrentId }
            } else {
                val list = when (trainingPhase) {
                    "WARMUP" -> mainExercises
                    "PSYCHOLOGY" -> psychologyExercises
                    "STRENGTH" -> strengthExercises
                    else -> mainExercises
                }
                list.getOrNull(currentExerciseIndex)
            }

            if (current != null) {
                if (trainingPhase != "MAIN") {
                    TrainingExercisePlaceholder(
                        title = current.title,
                        description = current.whyImportant,
                        onDone = {
                            onExerciseStatusesChange(exerciseStatuses.toMutableMap().apply { put(currentExerciseIndex, SummaryStatus.DONE) })
                            if (currentExerciseIndex + 1 < mainExercises.size) {
                                onCurrentExerciseIndexChange(currentExerciseIndex + 1)
                                onScreenChange("training_block")
                            } else {
                                onScreenChange("training_summary")
                            }
                        },
                        onBack = { onScreenChange(if (isFreeTraining) "training_free_next" else "training_block") }
                    )
                } else {
                    if (current.id == "targeted_practice") {
                        TargetedPracticeScreen(
                            targets = currentTargetGoals,
                            dartsPerTarget = 15,
                            onFinish = { hits, totalDarts ->
                                onExerciseFinished(current.id, BlockType.SCORE, hits, totalDarts, "")
                            },
                            onBack = { onScreenChange(if (isFreeTraining) "training_free_next" else "training_block") }
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
                            onFinish = { blockType, score, darts, details ->
                                onExerciseFinished(current.id, blockType, score, darts, details)
                            },
                            onBack = { onScreenChange(if (isFreeTraining) "training_free_next" else "training_block") }
                        )
                    }
                }
            }
        }

        "training_summary" -> {
            val list = when (trainingPhase) {
                "WARMUP" -> mainExercises
                "PSYCHOLOGY" -> psychologyExercises
                "STRENGTH" -> strengthExercises
                else -> mainExercises
            }
            val summaries = list.mapIndexed { index, ex ->
                ExerciseSummary(
                    title = ex.title,
                    minutes = ex.minutes,
                    status = exerciseStatuses[index] ?: SummaryStatus.NOT_DONE
                )
            }
            val recommendedIds = recommendedGameIds(currentWeakness)
            
            TrainingSummaryScreen(
                playerName = playerName,
                planMinutes = trainingMinutes,
                actualSeconds = trainingElapsedSeconds,
                exercises = summaries,
                blockType = SummaryBlockType.MAIN,
                blockExercises = emptyList(),
                isLastBlock = true,
                recommendedIds = recommendedIds,
                onContinue = onFinishAndClear,
                onPlayExercise = { id -> onFreePlayAction(id) },
                onPlayGame = { id -> onFreePlayAction(id) },
                onFinishTraining = onFinishAndClear
            )
        }

        "normatives" -> {
            NormativesScreen(
                playerName = playerName,
                currentLevel = normativesStartLevel,
                completedSteps = normativesCompletedSteps,
                currentStep = NormativeStep.entries.firstOrNull { it.id !in normativesCompletedSteps },
                newLevel = normativesNewLevel,
                onStartStep = onStartStep,
                onFinish = onFinishNormatives,
                onCancel = onCancelNormatives
            )
        }

        "normatives_exercise" -> {
            val step = NormativeStep.entries.firstOrNull { it.id !in normativesCompletedSteps }
            if (step != null) {
                NormativesExerciseScreen(
                    step = step,
                    startMillis = exerciseStartMillis,
                    trainingSessionId = trainingSessionId,
                    trainingRepository = trainingRepository,
                    sectorRepository = sectorRepository,
                    bigRoundRepository = bigRoundRepository,
                    scoreSetRepository = scoreSetRepository,
                    onExerciseFinished = { stepId ->
                        // Передаем наверх, чтобы MainActivity сохранила
                        onExerciseFinished(stepId, BlockType.SCORE, 0, 0, "")
                    },
                    onBack = onBackFromNormativesExercise
                )
            }
        }
    }
}
