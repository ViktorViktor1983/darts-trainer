package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.aroundclock.AroundClockOrder
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.aroundclock.AroundClockTarget
import com.lodkin.dartstrainer.data.biground.BigRoundCategory
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.data.training.BlockType
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBgDark
import com.lodkin.dartstrainer.ui.aroundclock.AroundClockGameScreen
import com.lodkin.dartstrainer.ui.biground.BigRoundGameScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetGameScreen
import com.lodkin.dartstrainer.ui.sector.SectorGameScreen

/**
 * Запуск упражнения основной части. Используется и в обычной,
 * и в свободной тренировке.
 *
 * onFinish передаёт: (blockType, score, darts).
 */
@Composable
internal fun MainExerciseRunner(
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

/**
 * Рекомендуемые игры по слабому месту.
 */
internal fun recommendedGameIds(weakness: WeaknessType?): Set<String> = when (weakness) {
    WeaknessType.DOUBLES -> setOf("aroundclock_doubles", "game501")
    WeaknessType.SCORE -> setOf("sector20", "scoreset")
    WeaknessType.ACCURACY -> setOf("aroundclock_sectors", "biground")
    WeaknessType.TREBLES -> setOf("aroundclock_trebles", "biground", "cricket")
    null -> emptySet()
}

/**
 * Заглушка для упражнения в разработке.
 */
@Composable
internal fun TrainingExercisePlaceholder(
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
