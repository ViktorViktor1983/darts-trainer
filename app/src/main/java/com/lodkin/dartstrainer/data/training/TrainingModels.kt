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
 * По нему планировщик понимает, что за упражнение сейчас и куда идти дальше.
 */
enum class BlockType {
    WARMUP,     // Разминка (4 фазы)
    DOUBLES,    // Блок на удвоения
    SCORE,      // Блок на набор очков
    SECTOR,     // Блок на точность в сектор (Кругосветка-Сектора)
    TREBLES,    // Блок на утроения (Кругосветка-Утроения)
    CHECKOUT,   // Блок чекаутов
    GAME_501,   // Партия 501
    CRICKET,    // Партия Крикет
    STRENGTH    // Силовые упражнения
}

/**
 * Режим тренировок. Отличается только способом напоминаний,
 * программа тренировок везде одинаковая.
 */
enum class TrainingMode {
    SCHEDULED,  // По расписанию (пн/ср/пт)
    FLEXIBLE,   // Гибкий — N тренировок в неделю, когда угодно
    FREE        // Свободный — без расписания и целей
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
 * Хранит общую информацию. Результаты отдельных упражнений — в ExerciseResult.
 */
data class TrainingSession(
    val id: Long = 0L,
    val dateMillis: Long,           // Когда начата (System.currentTimeMillis())
    val plannedMinutes: Int,        // Сколько планировалось (30..120)
    val actualMinutes: Int,         // Сколько реально заняло (0 если ещё не завершена)
    val levelAtStart: Int,          // Уровень игрока на момент начала (1..16)
    val mode: TrainingMode,         // Режим тренировок
    val focus: WeaknessType?,       // Главное слабое место (может быть null, если всё в норме)
    val isControl: Boolean,         // Это контрольная тренировка?
    val isCompleted: Boolean        // Завершена полностью?
)

/**
 * Результат одного упражнения внутри тренировки.
 * Например: «Сектор 20 — 520 очков за 30 дротиков».
 */
data class ExerciseResult(
    val id: Long = 0L,
    val sessionId: Long,            // К какой тренировке относится
    val blockType: BlockType,       // Какой блок
    val exerciseId: String,         // Идентификатор упражнения, например "sector20", "bigRound"
    val score: Int,                 // Основной результат (очки, попадания и т.п.)
    val dartsThrown: Int,           // Сколько дротиков потрачено
    val timestamp: Long,            // Когда выполнено
    val detailsJson: String = ""    // Дополнительные данные (разбивка по секторам и т.п.)
)

/**
 * Отложенный дабл.
 * Появляется, когда игрок не уложился в лимит дротиков на точечной тренировке удвоения.
 * Возвращается в начале следующей тренировки.
 */
data class PostponedDouble(
    val id: Long = 0L,
    val sessionId: Long,            // Где был отложен
    val doubleNumber: Int,          // 1..20 = D1..D20, 21 = Bull
    val postponedCount: Int,        // Сколько раз откладывался всего
    val lastPostponedAt: Long,      // Когда отложен последний раз
    val isResolved: Boolean         // Уже выполнен на следующей тренировке?
)

/**
 * Снимок слабого места на момент тренировки.
 * Нужен, чтобы видеть историю: «в сентябре хромали удвоения, в октябре — точность».
 */
data class WeaknessSnapshot(
    val id: Long = 0L,
    val sessionId: Long,
    val type: WeaknessType,
    val currentValue: Float,        // Текущее значение игрока
    val normValue: Float,           // Порог уровня
    val priority: Int               // 1 = главная слабость, 2 = вторая, 3 = третья
)

/**
 * Снимок уровня и потенциала на момент тренировки.
 * Позволяет видеть, как менялись уровень и потенциал во времени.
 *
 * Потенциал = максимум из уровней по четырём параметрам.
 * Слабый (минимальный) параметр — то, что нужно подтянуть.
 */
data class PotentialSnapshot(
    val id: Long = 0L,
    val sessionId: Long,
    val level: Int,                 // Текущий уровень игрока (1..16)
    val potential: Int,             // Уровень, который игрок достигнет, подтянув слабое
    val weakestType: WeaknessType,  // Что мешает реализовать потенциал
    val scoreValue: Float,          // Текущее значение параметра «Набор очков»
    val doublesValue: Float,        // Текущее значение параметра «Удвоения»
    val accuracyValue: Float,       // Текущее значение параметра «Точность в сектор»
    val treblesValue: Float,        // Текущее значение параметра «Утроения»
    val timestamp: Long
)

/**
 * Результат одного контрольного матча.
 * Это часть контрольной тренировки — игра 501 или Крикет против бота.
 * Хранится отдельно от обычных матчей, чтобы «Игра под давлением» не смешивалась с тренировками.
 */
data class ControlMatch(
    val id: Long = 0L,
    val sessionId: Long,
    val gameType: ControlGameType,
    val botLevel: Int,              // Уровень бота (1..16)
    val playerWon: Boolean,
    val playerScore: Float,         // PPR для 501, MPR для Крикета
    val botScore: Float,
    val legsPlayed: Int,
    val timestamp: Long
)
