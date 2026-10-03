package com.lodkin.dartstrainer.data.game501

// Тип игры
enum class GameType(val startScore: Int, val label: String) {
    X501(501, "501"),
    X301(301, "301"),
    X1001(1001, "1001")
}

// Формат закрытия
enum class OutMode(val label: String, val shortLabel: String) {
    DOUBLE_OUT("Double Out", "DO"),
    DOUBLE_IN_OUT("Double In / Double Out", "DI/DO"),
    STRAIGHT_OUT("Упрощённый", "SO")
}

// Результат броска
enum class ThrowMultiplier(val value: Int, val label: String) {
    SINGLE(1, "S"),
    DOUBLE(2, "D"),
    TRIPLE(3, "T"),
    MISS(0, "M")
}

// Один бросок
data class Throw501(
    val sector: Int,
    val multiplier: ThrowMultiplier
) {
    val points: Int
        get() = if (multiplier == ThrowMultiplier.MISS) 0 else sector * multiplier.value

    val isDouble: Boolean
        get() = multiplier == ThrowMultiplier.DOUBLE
}

// Снимок одного игрока за один лег
data class LegPlayerSnapshot501(
    val name: String,
    val teamIndex: Int,
    val isBot: Boolean,
    val darts: Int,
    val scoreGained: Int,
    val doublesHit: Int,
    val doublesAttempted: Int,
    val count180: Int = 0,
    val count170plus: Int = 0,
    val count130plus: Int = 0,
    val count90plus: Int = 0,
    val count57plus: Int = 0,
    val count57minus: Int = 0
)

// Снимок одного лега
data class LegSnapshot501(
    val setNumber: Int,
    val legNumber: Int,
    val winningTeam: Int,
    val players: List<LegPlayerSnapshot501>
)

// Один игрок
data class Player501(
    val name: String,
    val isBot: Boolean = false,
    val botLevel: Int = 0,
    val teamIndex: Int = 0,

    // Состояние серии бота
    var botStreak: Double = 1.0,
    var botStreakLeft: Int = 0,

    // Текущее состояние
    var score: Int = 501,
    var turnScore: Int = 0,
    var turnDarts: Int = 0,

    // Накопительные за лег
    var legDarts: Int = 0,
    var legScoreGained: Int = 0,
    var legDoublesHit: Int = 0,
    var legDoublesAttempted: Int = 0,
    var legCount180: Int = 0,
    var legCount170plus: Int = 0,
    var legCount130plus: Int = 0,
    var legCount90plus: Int = 0,
    var legCount57plus: Int = 0,
    var legCount57minus: Int = 0,

    // Накопительные за матч
    var matchDarts: Int = 0,
    var matchScoreGained: Int = 0,
    var matchDoublesHit: Int = 0,
    var matchDoublesAttempted: Int = 0,
    var matchCount180: Int = 0,
    var matchCount170plus: Int = 0,
    var matchCount130plus: Int = 0,
    var matchCount90plus: Int = 0,
    var matchCount57plus: Int = 0,
    var matchCount57minus: Int = 0,

    // Набор без закрытия (остаток > 170)
    var nonCloseScore: Int = 0,
    var nonCloseDarts: Int = 0,

    // Первые 9 дротиков ТЕКУЩЕГО лега (сбрасывается при новом леге)
    var first9Score: Int = 0,
    var first9Darts: Int = 0,

    // Списки для усреднения по легам
    val listOfCloseValues: MutableList<Int> = mutableListOf(),
    val listOfLegDarts: MutableList<Int> = mutableListOf(),
    val listOfLegPpr: MutableList<Double> = mutableListOf(),
    // Список PPR первых 9 дротиков по каждому легу
    val listOfFirst9Ppr: MutableList<Double> = mutableListOf(),

    // Леги/сеты
    var legsInCurrentSet: Int = 0,
    var setsWon: Int = 0
)

// Состояние игры
data class Game501(
    val gameType: GameType = GameType.X501,
    val outMode: OutMode = OutMode.DOUBLE_OUT,
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

    val lastLegStartingTeam: Int = 0,

    val autoOkSeconds: Int = 0,

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

    val modeLabel: String
        get() = "${gameType.label} ${outMode.label}"
}
