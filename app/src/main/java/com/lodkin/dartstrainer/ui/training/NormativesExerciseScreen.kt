package com.lodkin.dartstrainer.ui.training

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.lodkin.dartstrainer.data.biground.BigRoundCategory
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.data.training.BlockType
import com.lodkin.dartstrainer.data.training.ExerciseResult
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.ui.biground.BigRoundGameScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetGameScreen
import com.lodkin.dartstrainer.ui.sector.SectorGameScreen
import kotlinx.coroutines.launch

/**
 * Экран «нормативы — упражнение».
 *
 * Используется только для трёх шагов сдачи нормативов:
 *   • Сектор 20 (SECTOR_20)
 *   • Большой раунд (BIG_ROUND)
 *   • Набор очков (SCORE_SET)
 *
 * 501 и Крикет идут другим путём (через обычные игровые экраны),
 * поэтому в этот Composable они не попадают.
 *
 * Задача этого экрана — показать нужный игровой экран, по завершении
 * записать результат в базу тренировок и сообщить наверх (onExerciseFinished)
 * id завершённого шага. Всё остальное (снапшоты, флаги, переходы) делает MainActivity.
 *
 * @param step какой шаг сдаём (SECTOR_20 / BIG_ROUND / SCORE_SET).
 * @param startMillis время старта упражнения (ставится в MainActivity при запуске шага).
 * @param trainingSessionId id текущей тренировочной сессии (в неё пишем результат).
 * @param trainingRepository репозиторий тренировок.
 * @param sectorRepository репозиторий упражнения «Сектор 20».
 * @param bigRoundRepository репозиторий «Большого раунда».
 * @param scoreSetRepository репозиторий «Набора очков».
 * @param onExerciseFinished колбэк: шаг успешно пройден, результат уже в БД.
 * @param onBack колбэк «Назад» (игрок вышел, не завершив упражнение).
 */
@Composable
fun NormativesExerciseScreen(
    step: NormativeStep,
    startMillis: Long,
    trainingSessionId: Long,
    trainingRepository: TrainingRepository,
    sectorRepository: SectorRepository,
    bigRoundRepository: BigRoundRepository,
    scoreSetRepository: ScoreSetRepository,
    onExerciseFinished: (stepId: String) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    fun elapsedSeconds(): Int =
        if (startMillis > 0L) {
            ((System.currentTimeMillis() - startMillis) / 1000L).toInt()
        } else {
            0
        }

    when (step) {
        NormativeStep.SECTOR_20 -> {
            SectorGameScreen(
                sector = 20,
                repository = sectorRepository,
                isTrainingMode = true,
                trainingRepository = trainingRepository,
                trainingRecordKey = "sector20",
                onFinish = { score ->
                    val duration = elapsedSeconds()
                    scope.launch {
                        trainingRepository.insertExerciseResult(
                            ExerciseResult(
                                sessionId = trainingSessionId,
                                blockType = BlockType.SCORE,
                                exerciseId = "sector20",
                                score = score,
                                dartsThrown = 30,
                                timestamp = System.currentTimeMillis(),
                                durationSeconds = duration,
                                detailsJson = ""
                            )
                        )
                        trainingRepository.updateTrainingRecordIfBetter(
                            exerciseId = "sector20",
                            score = score,
                            darts = 30,
                            lowerDartsBetter = false
                        )
                        onExerciseFinished(NormativeStep.SECTOR_20.id)
                    }
                },
                onBack = onBack
            )
        }

        NormativeStep.BIG_ROUND -> {
            BigRoundGameScreen(
                category = BigRoundCategory.MALE,
                repository = bigRoundRepository,
                isTrainingMode = true,
                trainingRepository = trainingRepository,
                trainingRecordKey = "biground",
                onFinish = { score ->
                    val duration = elapsedSeconds()
                    scope.launch {
                        trainingRepository.insertExerciseResult(
                            ExerciseResult(
                                sessionId = trainingSessionId,
                                blockType = BlockType.SECTOR,
                                exerciseId = "biground",
                                score = score,
                                dartsThrown = 63,
                                timestamp = System.currentTimeMillis(),
                                durationSeconds = duration,
                                detailsJson = ""
                            )
                        )
                        trainingRepository.updateTrainingRecordIfBetter(
                            exerciseId = "biground",
                            score = score,
                            darts = 63,
                            lowerDartsBetter = false
                        )
                        onExerciseFinished(NormativeStep.BIG_ROUND.id)
                    }
                },
                onBack = onBack
            )
        }

        NormativeStep.SCORE_SET -> {
            ScoreSetGameScreen(
                repository = scoreSetRepository,
                isTrainingMode = true,
                trainingRepository = trainingRepository,
                trainingRecordKey = "scoreset",
                onFinish = { totalScore, approaches ->
                    val duration = elapsedSeconds()
                    val dartsCount = approaches.size * 3
                    scope.launch {
                        trainingRepository.insertExerciseResult(
                            ExerciseResult(
                                sessionId = trainingSessionId,
                                blockType = BlockType.SCORE,
                                exerciseId = "scoreset",
                                score = totalScore,
                                dartsThrown = dartsCount,
                                timestamp = System.currentTimeMillis(),
                                durationSeconds = duration,
                                detailsJson = ""
                            )
                        )
                        trainingRepository.updateTrainingRecordIfBetter(
                            exerciseId = "scoreset",
                            score = totalScore,
                            darts = dartsCount,
                            lowerDartsBetter = false
                        )
                        onExerciseFinished(NormativeStep.SCORE_SET.id)
                    }
                },
                onBack = onBack
            )
        }

        // 501 и Крикет в этот экран не заходят — у них отдельные маршруты.
        // На всякий случай: если вдруг попали сюда — просто возвращаемся.
        else -> {
            onBack()
        }
    }
}
