package com.lodkin.dartstrainer.data.game501

// ─────────────────────────────────────────────
// Режим игры
// ─────────────────────────────────────────────
enum class Game501Mode(val label: String, val startScore: Int) {
    X501_DOUBLE_OUT("501 Double Out", 501),
    X501_DOUBLE_IN_OUT("501 Double In / Out", 501),
    X301("301", 301),
    STRAIGHT_OUT("Упрощённый", 501)
}

// ─────────────────────────────────────────────
// Результат броска
// ─────────────────────────────────────────────
enum class ThrowMultiplier(val value: Int, val label: String) {
    SINGLE(1, "S"),
    DOUBLE(2, "D"),
    TRIPLE(3, "T"),
    MISS(0, "M")
}

// Один бросок
data class Throw501(
    val sector: Int,                    // 0..20 или 25 (Bull)
    val multiplier: ThrowMultiplier
) {
    val points: Int
        get() = if (multiplier == ThrowMultiplier.MISS) 0 else sector * multiplier.value

    val isDouble: Boolean
        get() = multiplier == ThrowMultiplier.DOUBLE
}

// ─────────────────────────────────────────────
// Снимок одного игрока за один лег
// ─────────────────────────────────────────────
data class LegPlayerSnapshot501(
    val name: String,
    val teamIndex: Int,
    val isBot: Boolean,
    val darts: Int,             // дротиков за лег
    val scoreGained: Int,       // очков набрано за лег
    val doublesHit: Int,        // попаданий в удвоения
    val doublesAttempted: Int   // попыток в удвоения
)

// Снимок одного лега
data class LegSnapshot501(
    val setNumber: Int,
    val legNumber: Int,
    val winningTeam: Int,
    val players: List<LegPlayerSnapshot501>
)

// ─────────────────────────────────────────────
// Один игрок
// ─────────────────────────────────────────────
data class Player501(
    val name: String,
    val isBot: Boolean = false,
    val botLevel: Int = 0,
    val teamIndex: Int = 0,

    // ── Состояние серии бота ──
    var botStreak: Double = 1.0,
    var botStreakLeft: Int = 0,

    // ── Текущее состояние ──
    var score: Int = 501,           // текущий остаток
    var turnScore: Int = 0,         // набрано за текущий подход
    var turnDarts: Int = 0,         // дротиков в текущем подходе

    // ── Накопительные за лег ──
    var legDarts: Int = 0,
    var legScoreGained: Int = 0,
    var legDoublesHit: Int = 0,
    var legDoublesAttempted: Int = 0,

    // ── Накопительные за матч ──
    var matchDarts: Int = 0,
    var matchScoreGained: Int = 0,
    var matchDoublesHit: Int = 0,
    var matchDoublesAttempted: Int = 0,

    // ── Леги/сеты ──
    var legsInCurrentSet: Int = 0,
    var setsWon: Int = 0
)

// ─────────────────────────────────────────────
// Состояние игры
// ─────────────────────────────────────────────
data class Game501(
    val mode: Game501Mode = Game501Mode.X501_DOUBLE_OUT,
    val players: List<Player501> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val isFinished: Boolean = false,
    val winnerIndex: Int? = null,

    val legsPerSet: Int = 1,
    val setsPerMatch: Int = 1,
    val currentLegNumber: Int = 1,
    val currentSetNumber: Int = 1,

    val lastLegWinnerIndex: Int? = null,
    val lastSetWinnerIndex: Int? = null,

    // Кто начинал текущий лег
    val lastLegStartingTeam: Int = 0,

    val autoOkSeconds: Int = 0,

    // Сессия (общие на весь запуск приложения)
    val sessionStartTime: Long = 0L,
    val sessionForm: Double = 1.0,

    val legHistory: List<LegSnapshot501> = emptyList(),

    val isPairGame: Boolean = false,
    val teamCount: Int = 2,
    val playersPerTeam: Int = 1
) {
    val currentPlayer: Player501?
        get() = players.getOrNull(currentPlayerIndex)

    fun playersOfTeam(team: Int): List<Player501> =
        players.filter { it.teamIndex == team }
}
