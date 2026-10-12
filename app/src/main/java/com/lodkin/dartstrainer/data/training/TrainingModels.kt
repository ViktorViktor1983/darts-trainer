package com.lodkin.dartstrainer.data.training

/**
 * Модели тренировочного раздела.
 *
 * Здесь только структуры данных — никакой логики и никакого Room.
 * Всё, что касается хранения в БД, будет отдельно, в TrainingDatabase.kt.
 */

// ============================================================
// ПЕРЕЧИСЛЕНИЯ (ENUMS)
// ============================================================

/**
 * Четыре ключевых параметра игрока.
 * Используются при определении слабого места, уровня и потенциала.
 */
enum class WeaknessType {
    SCORE,      // Набор очков
    DOUBLES,    // Удвоения
    ACCURACY,   // Точность в сектор
    TREBLES     // Точность в утроения
}

/**
 * Тип блока в тренировке.
 */
enum class BlockType {
    WARMUP,     // Разминка (4 фазы)
    DOUBLES,    // Блок на удвоения
    SCORE,      // Блок на набор очков
    SECTOR,     // Блок на точность в сектор
    TREBLES,    // Блок на утроения
    CHECKOUT,   // Блок чекаутов
    GAME_501,   // Партия 501
    CRICKET,    // Партия Крикет
    STRENGTH    // Силовые упражнения
}

/**
 * Режим тренировок.
 */
enum class TrainingMode {
    SCHEDULED,  // По расписанию
    FLEXIBLE,   // Гибкий
    FREE        // Свободный
}

/**
 * Что за игра в контрольном матче.
 */
enum class ControlGameType {
    GAME_501,
    CRICKET
}

// ============================================================
// ОСНОВНЫЕ СУЩНОСТИ
// ============================================================

/**
 * Одна тренировка целиком.
 */
data class TrainingSession(
    val id: Long = 0L,
    val dateMillis: Long,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val levelAtStart: Int,
    val mode: TrainingMode,
    val focus: WeaknessType?,
    val isControl: Boolean,
    val isCompleted: Boolean
)

/**
 * Результат одного упражнения внутри тренировки.
 *
 * @param durationSeconds  сколько секунд заняло упражнение.
 *                         0 — если данные не сохранились (старые записи).
 */
data class ExerciseResult(
    val id: Long = 0L,
    val sessionId: Long,
    val blockType: BlockType,
    val exerciseId: String,
    val score: Int,
    val dartsThrown: Int,
    val timestamp: Long,
    val durationSeconds: Int = 0,
    val detailsJson: String = ""
)

/**
 * Отложенный дабл.
 */
data class PostponedDouble(
    val id: Long = 0L,
    val sessionId: Long,
    val doubleNumber: Int,
    val postponedCount: Int,
    val lastPostponedAt: Long,
    val isResolved: Boolean
)

/**
 * Снимок слабого места на момент тренировки.
 */
data class WeaknessSnapshot(
    val id: Long = 0L,
    val sessionId: Long,
    val type: WeaknessType,
    val currentValue: Float,
    val normValue: Float,
    val priority: Int
)

/**
 * Снимок уровня и потенциала на момент тренировки.
 */
data class PotentialSnapshot(
    val id: Long = 0L,
    val sessionId: Long,
    val level: Int,
    val potential: Int,
    val weakestType: WeaknessType,
    val scoreValue: Float,
    val doublesValue: Float,
    val accuracyValue: Float,
    val treblesValue: Float,
    val timestamp: Long
)

/**
 * Результат одного контрольного матча.
 */
data class ControlMatch(
    val id: Long = 0L,
    val sessionId: Long,
    val gameType: ControlGameType,
    val botLevel: Int,
    val playerWon: Boolean,
    val playerScore: Float,
    val botScore: Float,
    val legsPlayed: Int,
    val timestamp: Long
)
